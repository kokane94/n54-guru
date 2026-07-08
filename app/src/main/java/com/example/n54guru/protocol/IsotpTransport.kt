package com.example.n54guru.protocol

import android.util.Log

/**
 * ISO 15765-2 (ISO-TP) transport layer for sending multi-frame UDS messages
 * over CAN. CAN frames have a max payload of 8 bytes. UDS requests and
 * responses can be longer than 8 bytes, so they need to be segmented
 * (or, for short messages, fit in a single frame).
 *
 * Frame types (PCI byte, first nibble):
 *   0x0 = Single Frame (SF) — payload 1-7 bytes
 *   0x1 = First Frame (FF)  — start of multi-frame
 *   0x2 = Consecutive Frame (CF) — continuation
 *   0x3 = Flow Control (FC) — flow control from receiver
 *
 * This is a transport layer over an abstract [CanBus]. The [CanBus]
 * implementation is what talks to the actual USB-to-CAN adapter (ELM327,
 * K-CAN cable, etc.) over the serial port.
 */
class IsotpTransport(private val canBus: CanBus) {

    interface CanBus {
        /** Send a single CAN frame with 11-bit arbitration ID and up to 8 bytes payload. */
        fun sendFrame(arbId: Int, data: ByteArray): Boolean

        /** Receive a single CAN frame. Returns null if timeout. */
        fun receiveFrame(timeoutMs: Int): CanFrame?

        /** Set timing parameters (used for Flow Control responses). */
        fun setTiming(blockSize: Int, stMinMs: Int, timeoutMs: Int = 1000)
    }

    data class CanFrame(val arbId: Int, val data: ByteArray, val timestamp: Long = System.currentTimeMillis())

    // Flow control parameters we use when we're the receiver
    private val blockSize: Int = 0   // 0 = send all CFs back-to-back, no flow control pause
    private val stMin: Int = 0        // Separation time minimum (ms)
    private val txTimeout: Int = 1000
    private val rxTimeout: Int = 1000

    /**
     * Send a UDS message. Returns true on success, false on transport error.
     * Multi-frame messages are handled automatically.
     */
    fun send(arbId: Int, payload: ByteArray): Boolean {
        if (payload.isEmpty()) return false
        if (payload.size <= 7) {
            // Single frame: PCI = 0x0N where N = length
            val frame = ByteArray(8)
            frame[0] = (0x00 or payload.size).toByte()
            System.arraycopy(payload, 0, frame, 1, payload.size)
            return canBus.sendFrame(arbId, frame)
        }

        // Multi-frame: First Frame announces total length, then Consecutive Frames
        val firstFrame = ByteArray(8)
        firstFrame[0] = (0x10 or ((payload.size shr 8) and 0x0F)).toByte()
        firstFrame[1] = (payload.size and 0xFF).toByte()
        System.arraycopy(payload, 0, firstFrame, 2, 6)
        if (!canBus.sendFrame(arbId, firstFrame)) return false

        // Wait for Flow Control from the receiver
        val fcFrame = waitForFlowControl(arbId + 0x40, timeoutMs = rxTimeout) ?: return false
        if (fcFrame[0].toInt() and 0xF0 != 0x30) return false
        val rxBlockSize = fcFrame[1].toInt() and 0xFF
        val rxStMin = when (val raw = fcFrame[2].toInt() and 0xFF) {
            0 -> 0
            in 1..127 -> raw
            in 241..249 -> (raw - 240) * 100  // 100-900us
            else -> 127
        }

        var sent = 6
        var sequence = 1
        while (sent < payload.size) {
            val chunkSize = minOf(7, payload.size - sent)
            val cfFrame = ByteArray(8)
            cfFrame[0] = (0x20 or (sequence and 0x0F)).toByte()
            System.arraycopy(payload, sent, cfFrame, 1, chunkSize)
            if (!canBus.sendFrame(arbId, cfFrame)) return false
            sent += chunkSize
            sequence = (sequence + 1) and 0x0F

            // Honor block size: pause and wait for FC every rxBlockSize frames
            if (rxBlockSize > 0 && sequence % rxBlockSize == 0 && sent < payload.size) {
                waitForFlowControl(arbId + 0x40, timeoutMs = rxTimeout) ?: return false
            }
            // Separation time minimum
            if (rxStMin > 0) Thread.sleep(rxStMin.toLong())
        }
        return true
    }

    /**
     * Receive a UDS message. Handles both single-frame and multi-frame
     * reception, with Flow Control transmission.
     */
    fun receive(arbId: Int, timeoutMs: Int = rxTimeout): ByteArray? {
        val firstFrame = canBus.receiveFrame(timeoutMs) ?: return null
        if (firstFrame.arbId != arbId) {
            Log.w(TAG, "Unexpected response ID: got 0x${"%03X".format(firstFrame.arbId)} expected 0x${"%03X".format(arbId)}")
            return null
        }
        val pciType = firstFrame.data[0].toInt() and 0xF0
        return when (pciType) {
            0x00 -> {
                // Single frame
                val length = firstFrame.data[0].toInt() and 0x0F
                firstFrame.data.copyOfRange(1, 1 + length)
            }
            0x10 -> {
                // First frame of multi-frame
                val totalLength = ((firstFrame.data[0].toInt() and 0x0F) shl 8) or
                    (firstFrame.data[1].toInt() and 0xFF)
                val result = ByteArray(totalLength)
                System.arraycopy(firstFrame.data, 2, result, 0, 6)
                // Send Flow Control
                val fcFrame = ByteArray(8)
                fcFrame[0] = 0x30
                fcFrame[1] = blockSize.toByte()
                fcFrame[2] = stMin.toByte()
                canBus.sendFrame(arbId - 0x40, fcFrame)

                var received = 6
                var expectedSeq = 1
                while (received < totalLength) {
                    val cf = canBus.receiveFrame(timeoutMs) ?: return null
                    val cfPciType = cf.data[0].toInt() and 0xF0
                    if (cfPciType != 0x20) return null
                    val seq = cf.data[0].toInt() and 0x0F
                    if (seq != expectedSeq) return null
                    val chunkSize = minOf(7, totalLength - received)
                    System.arraycopy(cf.data, 1, result, received, chunkSize)
                    received += chunkSize
                    expectedSeq = (expectedSeq + 1) and 0x0F
                    if (blockSize > 0 && expectedSeq % blockSize == 0 && received < totalLength) {
                        // Send another FC to keep the sender going
                        canBus.sendFrame(arbId - 0x40, fcFrame)
                    }
                }
                result
            }
            else -> {
                Log.w(TAG, "Unexpected PCI type 0x${"%02X".format(pciType)}")
                null
            }
        }
    }

    private fun waitForFlowControl(arbId: Int, timeoutMs: Int): ByteArray? {
        val frame = canBus.receiveFrame(timeoutMs) ?: return null
        if (frame.arbId != arbId) return null
        return frame.data
    }

    companion object {
        private const val TAG = "IsotpTransport"
    }
}
