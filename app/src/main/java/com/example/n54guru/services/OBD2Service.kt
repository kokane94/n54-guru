package com.example.n54guru.services

import android.content.Context
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.*
import com.example.n54guru.models.OBDParameter
import com.example.n54guru.models.ElectricalParameter
import com.example.n54guru.models.OBDLog
import com.example.n54guru.data.AppDatabase
import com.example.n54guru.data.OBDLogDao
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class OBD2Service(private val context: Context) {
    private var port: UsbSerialPort? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
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

    fun readEngineData(): Map<String, OBDParameter> {
        val data = mutableMapOf<String, OBDParameter>()
        port?.let {
            // Read RPM (PID 010C)
            try {
                it.write("010C\r".toByteArray(), 1000)
                var response = it.read(ByteArray(256), 1000).toString(Charsets.UTF_8)
                parseOBDResponse("010C", response)?.let { value ->
                    data["RPM"] = OBDParameter("010C", value, "RPM")
                }
            } catch (e: IOException) { /* Handle error */ }

            // Read Coolant Temp (PID 0105)
            try {
                it.write("0105\r".toByteArray(), 1000)
                var response = it.read(ByteArray(256), 1000).toString(Charsets.UTF_8)
                parseOBDResponse("0105", response)?.let { value ->
                    data["CoolantTemp"] = OBDParameter("0105", value, "°C")
                }
            } catch (e: IOException) { /* Handle error */ }

            // Read Boost Pressure (PID 010B)
            try {
                it.write("010B\r".toByteArray(), 1000)
                var response = it.read(ByteArray(256), 1000).toString(Charsets.UTF_8)
                parseOBDResponse("010B", response)?.let { value ->
                    data["Boost"] = OBDParameter("010B", value, "psi")
                }
            } catch (e: IOException) { /* Handle error */ }

            // Read HPFP Pressure (PID 010A)
            try {
                it.write("010A\r".toByteArray(), 1000)
                var response = it.read(ByteArray(256), 1000).toString(Charsets.UTF_8)
                parseOBDResponse("010A", response)?.let { value ->
                    data["HPFPPressure"] = OBDParameter("010A", value, "kPa")
                }
            } catch (e: IOException) { /* Handle error */ }

            // Read Oil Temperature (PID 015C)
            try {
                it.write("015C\r".toByteArray(), 1000)
                var response = it.read(ByteArray(256), 1000).toString(Charsets.UTF_8)
                parseOBDResponse("015C", response)?.let { value ->
                    data["OilTemp"] = OBDParameter("015C", value, "°C")
                }
            } catch (e: IOException) { /* Handle error */ }

            // Read Misfire Counters (Placeholder PID 01XX)
            try {
                it.write("01XX\r".toByteArray(), 1000)
                var response = it.read(ByteArray(256), 1000).toString(Charsets.UTF_8)
                parseOBDResponse("01XX", response)?.let { value ->
                    data["MisfireCyl1"] = OBDParameter("01XX", value, "count") // Example for Cylinder 1
                }
            } catch (e: IOException) { /* Handle error */ }
        }

        // Save data to database
        scope.launch {
            data.forEach { (key, param) ->
                obdLogDao.insert(OBDLog(pid = param.pid, value = param.value, unit = param.unit, timestamp = param.timestamp))
            }
            // Delete old logs (older than 7 days)
            obdLogDao.deleteOldLogs(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L))
        }

        return data
    }

    fun readElectricalData(): Map<String, ElectricalParameter> {
        val data = mutableMapOf<String, ElectricalParameter>()
        port?.let {
            // Example: Read Battery Voltage (PID 0101)
            try {
                it.write("0101\r".toByteArray(), 1000)
                var response = it.read(ByteArray(256), 1000).toString(Charsets.UTF_8)
                parseOBDResponse("0101", response)?.let { value ->
                    data["BatteryVoltage"] = ElectricalParameter("0101", value, "V")
                }
            } catch (e: IOException) { /* Handle error */ }

            // Add more electrical PIDs here as needed
        }
        return data
    }

    private fun parseOBDResponse(pid: String, response: String): Float? {
        // Basic parsing logic for common PIDs
        // This is a simplified example and may need more robust error handling and PID-specific parsing
        val cleanedResponse = response.replace("\r|\n|>".toRegex(), "").trim()
        if (cleanedResponse.length < 4 || !cleanedResponse.startsWith(pid.substring(0, 2))) {
            return null // Invalid response
        }

        try {
            when (pid) {
                "010C" -> { // RPM
                    val a = cleanedResponse.substring(4, 6).toInt(16)
                    val b = cleanedResponse.substring(6, 8).toInt(16)
                    return ((a * 256) + b) / 4f
                }
                "0105" -> { // Coolant Temperature
                    val a = cleanedResponse.substring(4, 6).toInt(16)
                    return a - 40f
                }
                "010B" -> { // Boost Pressure (Manifold Absolute Pressure - MAP)
                    val a = cleanedResponse.substring(4, 6).toInt(16)
                    return (a * 0.145037738f) // Convert kPa to psi, assuming 100kPa offset for atmospheric pressure
                }
                "010A" -> { // Fuel Pressure (HPFP - High Pressure Fuel Pump)
                    val a = cleanedResponse.substring(4, 6).toInt(16)
                    return a * 3f // Assuming (A * 3) kPa, needs verification for N54
                }
                "015C" -> { // Engine Oil Temperature
                    val a = cleanedResponse.substring(4, 6).toInt(16)
                    return a - 40f
                }
                "01XX" -> { // Misfire Counters (Placeholder - N54 specific PIDs often needed)
                    val a = cleanedResponse.substring(4, 6).toInt(16)
                    val b = cleanedResponse.substring(6, 8).toInt(16)
                    return (a * 256 + b).toFloat() // Example, actual parsing depends on PID
                }
                "0101" -> { // Battery Voltage (example PID, actual might vary)
                    val a = cleanedResponse.substring(4, 6).toInt(16)
                    val b = cleanedResponse.substring(6, 8).toInt(16)
                    return ((a * 256) + b) / 1000f // Assuming voltage in mV
                }
                else -> return null
            }
        } catch (e: NumberFormatException) {
            return null
        }
    }

    suspend fun getHistoricalData(): List<OBDLog> {
        return obdLogDao.getRecentLogs()
    }

    fun disconnect() {
        port?.close()
        scope.cancel()
    }
}
