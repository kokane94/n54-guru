package com.example.n54guru.protocol

import android.content.Context
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialProber
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ELM327 / K-CAN cable driver.
 *
 * The ELM327 is a ubiquitous OBD-II to USB/Serial bridge chip used in
 * cheap OBD2 adapters. The K-CAN cable is a BMW-specific variant that
 * exposes the body CAN bus via USB.
 *
 * Both present as USB serial devices to Android, and we talk to them
 * using the AT command protocol:
 *   ATZ    - reset
 *   ATE0   - echo off
 *   ATL0   - linefeed off
 *   ATS0   - spaces off
 *   ATH0   - headers off
 *   ATAT0  - adaptive timing off
 *   ATSP0  - protocol auto
 *   ATDP   - describe protocol
 *
 * Once initialized, we use the ISO 15765 (CAN 11-bit 500kbps) protocol
 * to talk UDS to the ECUs.
 *
 * The [IsotpTransport.CanBus] interface is implemented by this driver.
 * Frame format: AT command "AT SH xxx" sets the header, then we send
 * raw bytes that get wrapped in the right ISO-TP frame format.
 */
class Elm327Driver(
    private val context: Context,
    private val adapterType: AdapterType = AdapterType.AUTO_DETECT
) : IsotpTransport.CanBus {

    enum class AdapterType { AUTO_DETECT, ELM327_GENERIC, KCAN_CABLE, OBDLINK_MX }

    private var port: UsbSerialPort? = null
    private var currentArbId: Int = 0x7DF
    private var headerSet: Boolean = false
    private var useExtAddressing: Boolean = false

    private val prober = UsbSerialProber.getDefaultProber()

    /**
     * Open the first compatible USB serial device. Returns true on success.
     * The caller is responsible for requesting USB permission first.
     */
    suspend fun connect(device: android.hardware.usb.UsbDevice): Boolean = withContext(Dispatchers.IO) {
        val driver: UsbSerialDriver = prober.probeDevice(device) ?: run {
            Log.w(TAG, "No USB serial driver for device ${device.productName}")
            return@withContext false
        }
        try {
            val rawPort = driver.ports[0]
            rawPort.open(2000)
            val p = rawPort
            // Most ELM327 clones: 38400 8N1. Some K-CAN cables: 115200.
            val baud = when (adapterType) {
                AdapterType.OBDLINK_MX -> 2000000
                AdapterType.KCAN_CABLE -> 115200
                else -> 38400
            }
            p.setParameters(baud, 8, 1, 0)
            port = UsbSerialPort(p)
            initializeElm()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open USB device", e)
            false
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        port?.close()
        port = null
    }

    private fun initializeElm() {
        // Reset
        sendCommand("ATZ")
        Thread.sleep(2000)  // ELM327 reboots and prints chip version
        drainInput()

        // Turn off everything that adds noise
        sendCommand("ATE0")  // echo off
        sendCommand("ATL0")  // linefeed off
        sendCommand("ATS0")  // spaces off
        sendCommand("ATH0")  // headers off (we'll set them per-request)
        sendCommand("ATAT0") // adaptive timing off (deterministic latency)
        sendCommand("ATCAF0") // CAN auto formatting off
        sendCommand("ATST FF") // set timeout to longest
    }

    /**
     * Switch protocol to ISO 15765 CAN 500kbps. This is what the N54 DME
     * uses on D-CAN (PT-CAN).
     */
    fun switchToDCan() {
        sendCommand("ATSP6")  // ISO 15765-4 CAN 11-bit 500kbps
        // Send a single PID 0x00 to ensure the protocol is actually active
        sendCommand("0100")
        drainInput()
        headerSet = false
    }

    /**
     * Set the CAN header for subsequent messages. ELM327 will use this
     * for both request and response (it auto-fills the response ID with
     * request_id + 8 per OBD-II spec, but for UDS we manage IDs ourselves).
     */
    fun setHeader(arbId: Int) {
        if (headerSet && currentArbId == arbId) return
        sendCommand("AT SH %03X".format(arbId))
        currentArbId = arbId
        headerSet = true
    }

    /**
     * Implement [IsotpTransport.CanBus] using the ELM327.
     * The trick: ELM327 does ISO-TP segmentation for us when we send
     * raw payload bytes, as long as the protocol is set to ISO 15765.
     * So we just write the UDS payload to the serial port.
     */
    override fun sendFrame(arbId: Int, data: ByteArray): Boolean {
        if (data.size > 8) {
            // We never need to send raw 8-byte ISO-TP frames through the ELM327;
            // the library handles segmentation when we just send the UDS payload.
            Log.w(TAG, "sendFrame called with multi-frame data — should not happen with ELM327 transport")
            return false
        }
        setHeader(arbId)
        return port?.write(data) ?: false
    }

    override fun receiveFrame(timeoutMs: Int): IsotpTransport.CanFrame? {
        val response = readElmResponse(timeoutMs) ?: return null
        // ELM327 with headers on returns lines starting with the response
        // arb ID. With H0 (headers off), we get raw bytes which is what we want.
        return IsotpTransport.CanFrame(
            arbId = currentArbId + 0x40,  // standard OBD-II: response is req + 0x08
            data = response
        )
    }

    override fun setTiming(blockSize: Int, stMinMs: Int, timeoutMs: Int) {
        // The ELM327 has its own timing parameters; nothing to do here for now.
    }

    // ---------- Low-level ELM327 helpers ----------

    private fun sendCommand(cmd: String): Boolean {
        val p = port ?: return false
        val data = (cmd + "\r").toByteArray()
        return try {
            p.write(data)
            true
        } catch (e: Exception) {
            Log.e(TAG, "sendCommand failed: $cmd", e)
            false
        }
    }

    private fun readElmResponse(timeoutMs: Int): ByteArray? {
        val p = port ?: return null
        val deadline = System.currentTimeMillis() + timeoutMs
        val buffer = ByteArray(1024)
        var idx = 0
        while (System.currentTimeMillis() < deadline) {
            val n = try {
                p.read(buffer, idx, minOf(buffer.size - idx, 256), 100)
            } catch (e: Exception) { -1 }
            if (n > 0) {
                idx += n
                // ELM327 terminates responses with '>' character for prompts
                if (buffer[idx - 1] == '>'.code.toByte()) break
            }
        }
        if (idx == 0) return null
        // Strip the trailing '>'
        val payload = if (buffer[idx - 1] == '>'.code.toByte())
            buffer.copyOfRange(0, idx - 1)
        else
            buffer.copyOfRange(0, idx)
        // Convert ASCII hex string to bytes
        val hex = String(payload, Charsets.US_ASCII).replace(" ", "").trim()
        if (hex.isEmpty()) return null
        // Drop the "OK" or echo responses
        val cleanHex = hex.replace("\r", "").replace("\n", "")
        // Some clones return "NO DATA" or "?" on no reply
        if (cleanHex.startsWith("NO") || cleanHex.startsWith("?")) return null
        return hexToBytes(cleanHex)
    }

    private fun drainInput() {
        val p = port ?: return
        val buf = ByteArray(256)
        while (true) {
            val n = try { p.read(buf, 0, buf.size, 50) } catch (e: Exception) { -1 }
            if (n <= 0) break
        }
    }

    private fun hexToBytes(hex: String): ByteArray {
        val clean = hex.uppercase().replace(Regex("[^0-9A-F]"), "")
        if (clean.length % 2 != 0) return ByteArray(0)
        return ByteArray(clean.length / 2) { i ->
            ((Character.digit(clean[i * 2], 16) shl 4) or Character.digit(clean[i * 2 + 1], 16)).toByte()
        }
    }

    companion object {
        private const val TAG = "Elm327Driver"

        fun listAvailableDevices(context: Context): List<android.hardware.usb.UsbDevice> {
            val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            val prober = UsbSerialProber.getDefaultProber()
            return usbManager.deviceList.values.filter { prober.probeDevice(it) != null }
        }
    }
}

/**
 * Lightweight wrapper around UsbSerialPort to give us a stable interface.
 * The mik3y library's UsbSerialPort has the actual I/O methods.
 */
class UsbSerialPort(private val port: com.hoho.android.usbserial.driver.UsbSerialPort) {
    fun close() = port.close()
    fun setParameters(baud: Int, dataBits: Int, stopBits: Int, parity: Int) {
        port.setParameters(baud, dataBits, stopBits, parity)
    }
    fun write(data: ByteArray): Boolean = try { port.write(data, 1000); true } catch (e: Exception) { false }
    fun read(buffer: ByteArray, offset: Int, count: Int, timeoutMs: Int): Int = try { port.read(buffer, offset, count, timeoutMs) } catch (e: Exception) { -1 }
}
