package com.example.n54guru.protocol

import android.util.Log
import kotlinx.coroutines.delay
import java.nio.charset.StandardCharsets

/**
 * High-level UDS client. Wraps an [IsotpTransport] with a typed API
 * for the services defined in [UdsProtocol].
 *
 * Pattern reference: kokane94/python-udsoncan — reimplemented in Kotlin
 * to fit the Android platform. No code copied; message structure and
 * service definitions follow ISO 14229.
 *
 * All requests/responses are ByteArray until they're parsed into typed
 * results. Errors throw [UdsException].
 */
class UdsClient(
    private val transport: IsotpTransport,
    private val testerPresentIntervalMs: Int = 2000
) {

    /**
     * Send a raw UDS request and return the response payload (without
     * the response SID). Returns null on timeout. Throws on negative
     * response with the NRC.
     */
    suspend fun request(
        arbId: Int,
        responseArbId: Int,
        data: ByteArray,
        timeoutMs: Int = 1000
    ): ByteArray {
        if (!transport.send(arbId, data)) throw UdsException("Transport send failed")
        val response = transport.receive(responseArbId, timeoutMs)
            ?: throw UdsException("Timeout waiting for response")
        if (response.isEmpty()) throw UdsException("Empty response")

        if (response[0] == UdsProtocol.ServiceId.NEGATIVE_RESPONSE) {
            val nrc = if (response.size >= 3) response[2] else 0.toByte()
            val originalSid = if (response.size >= 2) response[1] else 0.toByte()
            throw NegativeResponseException(originalSid, nrc, nrcToString(nrc))
        }

        // Strip the positive response SID (response[0] - 0x40 == request SID)
        return response.copyOfRange(1, response.size)
    }

    // ---------- UDS Service: DiagnosticSessionControl (0x10) ----------
    suspend fun diagnosticSessionControl(arbId: Int, responseArbId: Int, session: Byte): Boolean {
        val data = byteArrayOf(UdsProtocol.ServiceId.DIAGNOSTIC_SESSION_CONTROL, session)
        val response = request(arbId, responseArbId, data, timeoutMs = 2000)
        return response.size >= 2 && response[0] == session
    }

    // ---------- UDS Service: ReadDataByIdentifier (0x22) ----------
    /**
     * Read a single DID. Returns the raw payload after the DID bytes.
     */
    suspend fun readDataByIdentifier(arbId: Int, responseArbId: Int, did: Int): ByteArray {
        val data = byteArrayOf(
            UdsProtocol.ServiceId.READ_DATA_BY_IDENTIFIER,
            (did shr 8).toByte(),
            (did and 0xFF).toByte()
        )
        return request(arbId, responseArbId, data, timeoutMs = 2000)
    }

    // ---------- UDS Service: WriteDataByIdentifier (0x2E) ----------
    suspend fun writeDataByIdentifier(arbId: Int, responseArbId: Int, did: Int, payload: ByteArray): Boolean {
        val data = ByteArray(3 + payload.size)
        data[0] = UdsProtocol.ServiceId.WRITE_DATA_BY_IDENTIFIER
        data[1] = (did shr 8).toByte()
        data[2] = (did and 0xFF).toByte()
        System.arraycopy(payload, 0, data, 3, payload.size)
        val response = request(arbId, responseArbId, data, timeoutMs = 2000)
        return response.size >= 3 &&
            response[0] == UdsProtocol.ServiceId.WRITE_DATA_BY_IDENTIFIER &&
            response[1] == (did shr 8).toByte() &&
            response[2] == (did and 0xFF).toByte()
    }

    // ---------- UDS Service: ReadDTCInformation (0x19) ----------
    /**
     * Read DTCs by status mask. Common mask values:
     * 0x01 = test failed
     * 0x08 = confirmed (stored)
     * 0x0F = all
     *
     * Returns a list of [Dtc] entries.
     */
    suspend fun readDtcByStatusMask(arbId: Int, responseArbId: Int, statusMask: Byte): List<Dtc> {
        val data = byteArrayOf(
            UdsProtocol.ServiceId.READ_DTC_INFORMATION,
            UdsProtocol.ReadDtcSubfunction.REPORT_DTC_BY_STATUS_MASK,
            statusMask
        )
        val response = request(arbId, responseArbId, data, timeoutMs = 2000)
        // Response: 0x59, subfunc, statusAvailability, [DTC entries]
        if (response.size < 3) return emptyList()
        val statusAvailability = response[2]
        val dtcs = mutableListOf<Dtc>()
        var i = 3
        while (i + 3 < response.size) {
            val dtcBytes = response.copyOfRange(i, i + 3)
            val status = response[i + 3]
            val dtc = Dtc.fromBytes(dtcBytes, status)
            dtcs.add(dtc)
            i += 4
        }
        return dtcs
    }

    // ---------- UDS Service: TesterPresent (0x3E) ----------
    /**
     * Send a TesterPresent to keep the diagnostic session alive. Many ECUs
     * drop out of non-default sessions after a timeout if they don't see
     * TesterPresent within 2-3 seconds.
     */
    suspend fun testerPresent(arbId: Int, responseArbId: Int): Boolean {
        val data = byteArrayOf(
            UdsProtocol.ServiceId.TESTER_PRESENT,
            0x00  // sub-function: zero = no positive response required, but we want one
        )
        return try {
            val response = request(arbId, responseArbId, data, timeoutMs = 500)
            response.isNotEmpty()
        } catch (e: Exception) {
            Log.w(TAG, "TesterPresent failed: ${e.message}")
            false
        }
    }

    // ---------- UDS Service: ClearDiagnosticInformation (0x14) ----------
    /**
     * Clear all stored DTCs. groupOfDTC = 0xFFFFFF means "all".
     * This is the service used to turn off the check engine light.
     */
    suspend fun clearDtc(arbId: Int, responseArbId: Int, groupOfDtc: Int = 0xFFFFFF): Boolean {
        val data = byteArrayOf(
            UdsProtocol.ServiceId.CLEAR_DTC,  // 0x14
            ((groupOfDtc shr 16) and 0xFF).toByte(),
            ((groupOfDtc shr 8) and 0xFF).toByte(),
            (groupOfDtc and 0xFF).toByte()
        )
        return try {
            val response = request(arbId, responseArbId, data, timeoutMs = 2000)
            response.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    // ---------- OBD-II (J1979) Mode 01 live data ----------
    /**
     * Read a single OBD-II PID. Returns the data bytes after the PID echo.
     * For live data on the N54, this works over D-CAN at 500 kbps with
     * request ID 0x7DF (functional broadcast) and response from 0x7E8.
     */
    suspend fun readObd2Pid(pid: Byte, timeoutMs: Int = 1000): ByteArray {
        val data = byteArrayOf(Obd2Pids.Mode.LIVE_DATA, pid)
        val response = request(
            arbId = BmwCanIds.Ista.FUNCTIONAL_REQUEST,
            responseArbId = BmwCanIds.Ista.FUNCTIONAL_RESPONSE,
            data = data,
            timeoutMs = timeoutMs
        )
        // Response: 0x41, pid, data...
        if (response.size < 2 || response[0] != (Obd2Pids.Mode.LIVE_DATA + 0x40).toByte()) {
            throw UdsException("Invalid OBD-II response")
        }
        return response.copyOfRange(2, response.size)
    }

    // ---------- Convenience: OBD-II PID decoders ----------
    fun parseRpm(data: ByteArray): Float {
        if (data.size < 2) return 0f
        return ((data[0].toInt() and 0xFF) * 256f + (data[1].toInt() and 0xFF)) / 4f
    }

    fun parseCoolantTemp(data: ByteArray): Float {
        if (data.isEmpty()) return 0f
        return (data[0].toInt() and 0xFF) - 40f
    }

    fun parseIntakeAirTemp(data: ByteArray): Float {
        if (data.isEmpty()) return 0f
        return (data[0].toInt() and 0xFF) - 40f
    }

    fun parseMaf(data: ByteArray): Float {
        if (data.size < 2) return 0f
        return ((data[0].toInt() and 0xFF) * 256f + (data[1].toInt() and 0xFF)) / 100f
    }

    fun parseThrottlePos(data: ByteArray): Float {
        if (data.isEmpty()) return 0f
        return (data[0].toInt() and 0xFF) * 100f / 255f
    }

    fun parseTimingAdvance(data: ByteArray): Float {
        if (data.isEmpty()) return 0f
        return ((data[0].toInt() and 0xFF) - 128f) / 2f
    }

    fun parseShortFuelTrim(data: ByteArray): Float {
        if (data.isEmpty()) return 0f
        return ((data[0].toInt() and 0xFF) - 128f) * 100f / 128f
    }

    fun parseFuelRailGaugePressure(data: ByteArray): Float {
        // N54: PID 0x23 = (A*256 + B) * 10 kPa
        if (data.size < 2) return 0f
        return ((data[0].toInt() and 0xFF) * 256f + (data[1].toInt() and 0xFF)) * 10f
    }

    fun parseBatteryVoltage(data: ByteArray): Float {
        // No native OBD-II PID for battery; we'll use ModuleVoltage if available.
        if (data.size < 2) return 0f
        return ((data[0].toInt() and 0xFF) * 256f + (data[1].toInt() and 0xFF)) / 1000f
    }

    fun parseVin(data: ByteArray): String {
        return String(data, StandardCharsets.US_ASCII).trim()
    }

    // ---------- NRC string mapping ----------
    fun nrcToString(nrc: Byte): String = when (nrc) {
        UdsProtocol.Nrc.GENERAL_REJECT -> "General reject"
        UdsProtocol.Nrc.SERVICE_NOT_SUPPORTED -> "Service not supported"
        UdsProtocol.Nrc.SUB_FUNCTION_NOT_SUPPORTED -> "Sub-function not supported"
        UdsProtocol.Nrc.INCORRECT_MESSAGE_LENGTH -> "Incorrect message length or invalid format"
        UdsProtocol.Nrc.CONDITIONS_NOT_CORRECT -> "Conditions not correct"
        UdsProtocol.Nrc.REQUEST_OUT_OF_RANGE -> "Request out of range"
        UdsProtocol.Nrc.SECURITY_ACCESS_DENIED -> "Security access denied"
        UdsProtocol.Nrc.INVALID_KEY -> "Invalid key"
        UdsProtocol.Nrc.EXCEEDED_NUMBER_OF_ATTEMPTS -> "Exceeded number of attempts"
        UdsProtocol.Nrc.REQUIRED_TIME_DELAY_NOT_EXPIRED -> "Required time delay not expired"
        UdsProtocol.Nrc.UPLOAD_DOWNLOAD_NOT_ACCEPTED -> "Upload/download not accepted"
        UdsProtocol.Nrc.TRANSFER_DATA_SUSPENDED -> "Transfer data suspended"
        UdsProtocol.Nrc.GENERAL_PROGRAMMING_FAILURE -> "General programming failure"
        UdsProtocol.Nrc.WRONG_BLOCK_SEQUENCE_COUNTER -> "Wrong block sequence counter"
        UdsProtocol.Nrc.REQUEST_CORRECTLY_RECEIVED_RESPONSE_PENDING -> "Response pending"
        UdsProtocol.Nrc.SUB_FUNCTION_NOT_SUPPORTED_IN_ACTIVE_SESSION -> "Sub-function not supported in active session"
        UdsProtocol.Nrc.SERVICE_NOT_SUPPORTED_IN_ACTIVE_SESSION -> "Service not supported in active session"
        else -> "Unknown NRC 0x%02X".format(nrc)
    }
}

class UdsException(message: String) : RuntimeException(message)
class NegativeResponseException(val originalSid: Byte, val nrc: Byte, val nrcDescription: String) :
    RuntimeException("Negative response: $nrcDescription (NRC=0x%02X)".format(nrc))

/**
 * Diagnostic Trouble Code — the actual code value plus its status bits.
 *
 * DTC encoding (ISO 14229-1, 3 bytes per DTC):
 *   - 2 bytes: DTC value (high and low nibbles of each byte encode type + value)
 *   - 1 byte: status
 *
 * First 2 bits of first byte encode the DTC type:
 *   00 = ISO/SAE controlled (P, C, B, U codes)
 *   01 = vehicle manufacturer specific (P, C, B, U codes, manufacturer-specific)
 *   10 = reserved
 *   11 = system-specific
 */
data class Dtc(
    val code: String,
    val status: Byte,
    val isConfirmed: Boolean,
    val isPending: Boolean,
    val isTestFailed: Boolean
) {
    companion object {
        fun fromBytes(bytes: ByteArray, status: Byte): Dtc {
            val b0 = bytes[0].toInt() and 0xFF
            val b1 = bytes[1].toInt() and 0xFF

            val type = when ((b0 shr 6) and 0x03) {
                0 -> 'P'  // Powertrain
                1 -> 'C'  // Chassis
                2 -> 'B'  // Body
                else -> 'U'  // Network/Communication
            }
            val digit1 = (b0 shr 4) and 0x03
            val digit2 = b0 and 0x0F
            val digit3 = (b1 shr 4) and 0x0F
            val digit4 = b1 and 0x0F
            val code = "$type$digit1$digit2${if (digit3 < 10) digit3 else 'A' + digit3 - 10}${if (digit4 < 10) digit4 else 'A' + digit4 - 10}"

            return Dtc(
                code = code,
                status = status,
                isConfirmed = (status.toInt() and UdsProtocol.DtcStatus.CONFIRMED_DTC.toInt()) != 0,
                isPending = (status.toInt() and UdsProtocol.DtcStatus.PENDING_DTC.toInt()) != 0,
                isTestFailed = (status.toInt() and UdsProtocol.DtcStatus.TEST_FAILED.toInt()) != 0
            )
        }
    }
}

private const val TAG = "UdsClient"
