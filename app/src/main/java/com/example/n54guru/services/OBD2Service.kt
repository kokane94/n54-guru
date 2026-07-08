package com.example.n54guru.services

import android.content.Context
import android.hardware.usb.UsbManager
import com.github.mik3y.usbserial.driver.UsbSerialPort
import com.github.mik3y.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.*
import com.example.n54guru.models.OBDParameter
import com.example.n54guru.models.ElectricalParameter
import com.example.n54guru.models.OBDLog
import com.example.n54guru.data.AppDatabase
import com.example.n54guru.data.OBDLogDao
import java.io.IOException

class OBD2Service(private val context: Context) {
    private var port: UsbSerialPort? = null
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val obdLogDao: OBDLogDao

    init {
        obdLogDao = AppDatabase.getDatabase(context).obdLogDao()
    }

    fun connect(callback: (Boolean) -> Unit) {
        scope.launch {
            try {
                val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
                val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)

                if (availableDrivers.isEmpty()) {
                    callback(false)
                    return@launch
                }

                val driver = availableDrivers[0]
                val connection = manager.openDevice(driver.device)
                port = driver.ports[0]
                port?.open(connection)
                port?.setParameters(38400, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
                callback(true)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }

    /**
     * Write a command to the adapter and read one response frame.
     * Returns the decoded ASCII string, or empty on failure.
     */
    private fun obdExchange(command: String, timeoutMs: Int = 1000): String {
        val p = port ?: return ""
        return try {
            p.write(command.toByteArray(Charsets.US_ASCII), timeoutMs)
            val buf = ByteArray(256)
            val bytesRead = p.read(buf, timeoutMs)
            if (bytesRead <= 0) "" else String(buf, 0, bytesRead, Charsets.US_ASCII)
        } catch (e: IOException) {
            ""
        }
    }

    fun readEngineData(): Map<String, OBDParameter> {
        val data = mutableMapOf<String, OBDParameter>()

        obdExchange("010C\r")?.let { resp ->
            parseOBDResponse("010C", resp)?.let { v ->
                data["RPM"] = OBDParameter("010C", v, "rpm")
            }
        }
        obdExchange("0105\r")?.let { resp ->
            parseOBDResponse("0105", resp)?.let { v ->
                data["CoolantTemp"] = OBDParameter("0105", v, "°C")
            }
        }
        obdExchange("010B\r")?.let { resp ->
            parseOBDResponse("010B", resp)?.let { v ->
                data["Boost"] = OBDParameter("010B", v, "psi")
            }
        }
        obdExchange("010A\r")?.let { resp ->
            parseOBDResponse("010A", resp)?.let { v ->
                data["HPFPPressure"] = OBDParameter("010A", v, "kPa")
            }
        }
        obdExchange("015C\r")?.let { resp ->
            parseOBDResponse("015C", resp)?.let { v ->
                data["OilTemp"] = OBDParameter("015C", v, "°C")
            }
        }

        // Persist to DB off the call thread
        scope.launch {
            data.values.forEach { param ->
                obdLogDao.insert(OBDLog(pid = param.pid, value = param.value, unit = param.unit, timestamp = param.timestamp))
            }
            obdLogDao.deleteOldLogs(System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000))
        }

        return data
    }

    fun readElectricalData(): Map<String, ElectricalParameter> {
        val data = mutableMapOf<String, ElectricalParameter>()
        obdExchange("0142\r")?.let { resp ->
            parseOBDResponse("0142", resp)?.let { v ->
                data["BatteryVoltage"] = ElectricalParameter("0142", v, "V")
            }
        }
        return data
    }

    /**
     * Parse a single-frame OBD-II PID response.
     * Caller already passed the raw ASCII frame; we strip the "41 xx" header.
     */
    private fun parseOBDResponse(pid: String, response: String): Float? {
        val cleaned = response.replace("[\r\n>]".toRegex(), "").trim()
        if (cleaned.length < 4) return null
        // Standard OBD-II response echoes 2 bytes of service+PID then data bytes.
        val data = when (pid) {
            "010C" -> { // RPM: A*256+B / 4
                val a = cleaned.hexByteAt(2) ?: return null
                val b = cleaned.hexByteAt(3) ?: return null
                (a * 256 + b) / 4f
            }
            "0105", "015C" -> { // °C: A - 40
                val a = cleaned.hexByteAt(2) ?: return null
                a - 40f
            }
            "010B" -> { // MAP kPa
                val a = cleaned.hexByteAt(2) ?: return null
                a.toFloat()
            }
            "010A", "012E" -> { // HPFP kPa (A * 3 per spec)
                val a = cleaned.hexByteAt(2) ?: return null
                a * 3f
            }
            "0142" -> { // Module voltage: A*256+B / 1000
                val a = cleaned.hexByteAt(2) ?: return null
                val b = cleaned.hexByteAt(3) ?: return null
                (a * 256 + b) / 1000f
            }
            else -> return null
        }
        return data
    }

    private fun String.hexByteAt(byteIndex: Int): Int? = try {
        // byteIndex is 0-based against the cleaned hex payload.
        // We skip the leading 4 hex chars (2 bytes: service + PID echo).
        val start = (byteIndex + 2) * 2
        if (start + 2 > length) null
        else substring(start, start + 2).toInt(16)
    } catch (e: NumberFormatException) { null }

    suspend fun getHistoricalData(): List<OBDLog> {
        return obdLogDao.getRecentLogs()
    }

    fun disconnect() {
        port?.close()
        scope.cancel()
    }
}
