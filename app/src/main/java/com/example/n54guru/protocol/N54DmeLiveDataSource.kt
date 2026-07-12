package com.example.n54guru.protocol

import android.content.Context
import android.util.Log
import com.example.n54guru.knowledge.N54FaultCodes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * High-level orchestrator for live OBD-II / UDS reads on the N54 DME.
 *
 * Owns the [Elm327Driver], [IsotpTransport], and [UdsClient] lifecycle.
 * Exposes live data as a [StateFlow] so the UI can reactively update.
 *
 * Reads a configurable set of PIDs every ~50ms. DME updates internally
 * every 10ms so this is fast enough for dashboard use.
 */
class N54DmeLiveDataSource(private val context: Context) {

    data class LiveData(
        val isConnected: Boolean = false,
        val rpm: Float = 0f,
        val coolantTemp: Float = 0f,
        val intakeAirTemp: Float = 0f,
        val throttlePos: Float = 0f,
        val mafRate: Float = 0f,
        val shortFuelTrim: Float = 0f,
        val longFuelTrim: Float = 0f,
        val timingAdvance: Float = 0f,
        val fuelRailGaugePressure: Float = 0f,
        val batteryVoltage: Float = 0f,
        val vehicleSpeed: Float = 0f,
        val lastUpdate: Long = 0L,
        val errorMessage: String? = null
    )

    private val _liveData = MutableStateFlow(LiveData())
    val liveData: StateFlow<LiveData> = _liveData.asStateFlow()

    private val _dtcs = MutableStateFlow<List<Dtc>>(emptyList())
    val dtcs: StateFlow<List<Dtc>> = _dtcs.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val elm = Elm327Driver(context)
    private var isotp: IsotpTransport? = null
    private var uds: UdsClient? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollingJob: Job? = null
    private val isPolling = AtomicBoolean(false)

    /**
     * Open the adapter and start a fast poll loop. Returns immediately;
     * subscribe to [liveData] for actual values.
     */
    suspend fun connect(device: android.hardware.usb.UsbDevice): Boolean {
        val ok = elm.connect(device)
        if (!ok) {
            _liveData.value = _liveData.value.copy(
                isConnected = false,
                errorMessage = "Failed to open USB serial"
            )
            return false
        }
        elm.switchToDCan()
        isotp = IsotpTransport(elm)
        uds = UdsClient(isotp!!)
        _liveData.value = _liveData.value.copy(isConnected = true, errorMessage = null)
        startPolling()
        return true
    }

    fun disconnect() {
        stopPolling()
        scope.launch {
            elm.disconnect()
            isotp = null
            uds = null
            _liveData.value = LiveData(isConnected = false)
        }
    }

    private fun startPolling() {
        if (!isPolling.compareAndSet(false, true)) return
        pollingJob = scope.launch {
            while (isPolling.get()) {
                try {
                    pollOnce()
                } catch (e: Exception) {
                    Log.e(TAG, "Polling error: ${e.message}")
                    _liveData.value = _liveData.value.copy(
                        errorMessage = e.message,
                        lastUpdate = System.currentTimeMillis()
                    )
                }
                delay(50L)  // 20 Hz
            }
        }
    }

    private fun stopPolling() {
        isPolling.set(false)
        pollingJob?.cancel()
        pollingJob = null
    }

    /**
     * Single poll cycle — reads all PIDs and updates the StateFlow.
     * Errors on individual PIDs don't stop the cycle; the value just
     * isn't updated that cycle.
     */
    private suspend fun pollOnce() {
        val uds = uds ?: return
        val next = _liveData.value

        var updated = next.copy(lastUpdate = System.currentTimeMillis(), errorMessage = null)

        try {
            val rpmData = uds.readObd2Pid(Obd2Pids.Pid.ENGINE_RPM, timeoutMs = 200)
            updated = updated.copy(rpm = uds.parseRpm(rpmData))
        } catch (e: Exception) { /* ignore individual PID failures */ }

        try {
            val coolantData = uds.readObd2Pid(Obd2Pids.Pid.ENGINE_COOLANT_TEMP, timeoutMs = 200)
            updated = updated.copy(coolantTemp = uds.parseCoolantTemp(coolantData))
        } catch (_: Exception) {}

        try {
            val iatData = uds.readObd2Pid(Obd2Pids.Pid.INTAKE_AIR_TEMP, timeoutMs = 200)
            updated = updated.copy(intakeAirTemp = uds.parseIntakeAirTemp(iatData))
        } catch (_: Exception) {}

        try {
            val tpsData = uds.readObd2Pid(Obd2Pids.Pid.THROTTLE_POSITION, timeoutMs = 200)
            updated = updated.copy(throttlePos = uds.parseThrottlePos(tpsData))
        } catch (_: Exception) {}

        try {
            val mafData = uds.readObd2Pid(Obd2Pids.Pid.MAF_RATE, timeoutMs = 200)
            updated = updated.copy(mafRate = uds.parseMaf(mafData))
        } catch (_: Exception) {}

        try {
            val stftData = uds.readObd2Pid(Obd2Pids.Pid.SHORT_FUEL_TRIM_1, timeoutMs = 200)
            updated = updated.copy(shortFuelTrim = uds.parseShortFuelTrim(stftData))
        } catch (_: Exception) {}

        try {
            val ltftData = uds.readObd2Pid(Obd2Pids.Pid.LONG_FUEL_TRIM_1, timeoutMs = 200)
            updated = updated.copy(longFuelTrim = uds.parseShortFuelTrim(ltftData))
        } catch (_: Exception) {}

        try {
            val timingData = uds.readObd2Pid(Obd2Pids.Pid.TIMING_ADVANCE, timeoutMs = 200)
            updated = updated.copy(timingAdvance = uds.parseTimingAdvance(timingData))
        } catch (_: Exception) {}

        try {
            val fuelPData = uds.readObd2Pid(Obd2Pids.Pid.FUEL_RAIL_GAUGE_PRESSURE, timeoutMs = 200)
            updated = updated.copy(fuelRailGaugePressure = uds.parseFuelRailGaugePressure(fuelPData))
        } catch (_: Exception) {}

        try {
            val speedData = uds.readObd2Pid(Obd2Pids.Pid.VEHICLE_SPEED, timeoutMs = 200)
            updated = updated.copy(vehicleSpeed = (speedData.getOrNull(0)?.toInt()?.and(0xFF))?.toFloat() ?: 0f)
        } catch (_: Exception) {}

        _liveData.value = updated
    }

    /**
     * Read all stored DTCs from the DME via UDS 0x19 service.
     * The N54 DME supports both OBD-II Mode 03 and UDS 0x19 — we use
     * UDS for richer status info.
     */
    suspend fun scanDtcs(): List<Dtc> {
        val uds = uds ?: return emptyList()
        _isScanning.value = true
        try {
            val dtcList = uds.readDtcByStatusMask(
                arbId = BmwCanIds.Dme.DIAG_REQUEST,
                responseArbId = BmwCanIds.Dme.DIAG_RESPONSE,
                statusMask = UdsProtocol.DtcStatus.CONFIRMED_DTC
            )
            _dtcs.value = dtcList
            return dtcList
        } catch (e: Exception) {
            Log.e(TAG, "DTC scan failed: ${e.message}")
            return emptyList()
        } finally {
            _isScanning.value = false
        }
    }

    /**
     * Read the VIN via UDS 0x22 / DID 0xF190.
     */
    suspend fun readVin(): String? {
        val uds = uds ?: return null
        return try {
            val data = uds.readDataByIdentifier(
                arbId = BmwCanIds.Dme.DIAG_REQUEST,
                responseArbId = BmwCanIds.Dme.DIAG_RESPONSE,
                did = CommonDids.VIN
            )
            uds.parseVin(data)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Read DME part number and software/hardware version.
     */
    suspend fun readDmeIdentification(): Map<String, String> {
        val uds = uds ?: return emptyMap()
        val result = mutableMapOf<String, String>()
        try {
            val part = uds.readDataByIdentifier(
                BmwCanIds.Dme.DIAG_REQUEST,
                BmwCanIds.Dme.DIAG_RESPONSE,
                CommonDids.BMW_PART_NUMBER
            )
            result["part_number"] = String(part, Charsets.US_ASCII).trim()
        } catch (_: Exception) {}
        try {
            val sw = uds.readDataByIdentifier(
                BmwCanIds.Dme.DIAG_REQUEST,
                BmwCanIds.Dme.DIAG_RESPONSE,
                CommonDids.BMW_SOFTWARE_VERSION
            )
            result["software_version"] = String(sw, Charsets.US_ASCII).trim()
        } catch (_: Exception) {}
        try {
            val hw = uds.readDataByIdentifier(
                BmwCanIds.Dme.DIAG_REQUEST,
                BmwCanIds.Dme.DIAG_RESPONSE,
                CommonDids.BMW_HARDWARE_VERSION
            )
            result["hardware_version"] = String(hw, Charsets.US_ASCII).trim()
        } catch (_: Exception) {}
        return result
    }

    /** Currently connected device (set by connectToDevice). */
    private var currentDevice: android.hardware.usb.UsbDevice? = null

    /**
     * Connect to a specific USB device. Stores the reference so it can be
     * reconnected from the UI without re-picking.
     */
    suspend fun connectToDevice(context: Context, device: android.hardware.usb.UsbDevice): Boolean {
        currentDevice = device
        return connect(device)
    }

    /**
     * Reconnect to the previously-connected device, if any.
     */
    suspend fun connectCurrentDevice(context: Context): Boolean {
        val device = currentDevice ?: return false
        return connect(device)
    }

    /**
     * Clear all stored DTCs via UDS 0x14 service.
     * This will also reset the check engine light.
     */
    suspend fun clearAllDtcs(): Boolean {
        val uds = uds ?: return false
        return try {
            uds.clearDtc(
                BmwCanIds.Dme.DIAG_REQUEST,
                BmwCanIds.Dme.DIAG_RESPONSE,
                groupOfDtc = 0xFFFFFF
            )
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Cross-reference stored DTCs against the N54 fault code database
     * and return a list of [N54FaultCodes.FaultCode] entries that match.
     */
    fun knownDtcs(): List<N54FaultCodes.FaultCode> {
        val activeCodes = _dtcs.value.map { it.code }
        return N54FaultCodes.CODE_DATA.filter { faultCode ->
            // The database uses hex codes (e.g. "29CD") but the live DTCs
            // come back as P0XXX P-codes. Try to match on title and code.
            activeCodes.any { live ->
                faultCode.title.contains(live) || live.contains(faultCode.code) ||
                faultCode.title.contains(live.takeLast(4), ignoreCase = true)
            }
        }
    }

    companion object {
        private const val TAG = "N54DmeLiveDataSource"

        fun listAvailableDevicesFromContext(context: Context): List<android.hardware.usb.UsbDevice> {
            val usbManager = context.getSystemService(Context.USB_SERVICE) as android.hardware.usb.UsbManager
            val prober = com.hoho.android.usbserial.driver.UsbSerialProber.getDefaultProber()
            return usbManager.deviceList.values.filter { prober.probeDevice(it) != null }
        }
    }
}
