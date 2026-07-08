package com.example.n54guru.services

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.example.n54guru.models.OBDParameter
import com.example.n54guru.models.N54Parameters
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * The original OBDService pointed at com.felhr.usbserial, which is the old
 * abandoned USB-serial library (the one previously referenced as
 * com.hoho.android:usbserial:0.3.1 — a non-existent version). This
 * rewrite uses the live mik3y fork (com.github.mik3y:usb-serial-for-android
 * 3.9.0) and the N54Parameters definitions in models/.
 *
 * OBDService is the longer-lived OBD-II polling entry point; OBD2Service
 * is the simpler per-PID request/response helper used by MainActivity.
 */
class OBDService(private val context: Context) {
    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var serialPort: UsbSerialPort? = null
    private var connection: UsbDeviceConnection? = null

    private val _obdData = MutableStateFlow<Map<String, OBDParameter>>(emptyMap())
    val obdData: StateFlow<Map<String, OBDParameter>> = _obdData

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var readingJob: Job? = null

    fun connectToOBD(): Boolean {
        return try {
            val manager = usbManager
            val drivers: List<UsbSerialDriver> =
                UsbSerialProber.getDefaultProber().findAllDrivers(manager)
            val driver = drivers.firstOrNull()
            if (driver != null) {
                openSerialPort(driver)
                initializeOBDConnection()
                _isConnected.value = true
                startReadingData()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun openSerialPort(driver: UsbSerialDriver) {
        connection = usbManager.openDevice(driver.device)
        serialPort = driver.ports[0].apply {
            open(connection)
            setParameters(38400, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        }
    }

    private fun initializeOBDConnection() {
        sendCommand("ATZ\r")
        Thread.sleep(500)
        sendCommand("ATSP0\r")
        Thread.sleep(500)
    }

    private fun startReadingData() {
        readingJob = scope.launch {
            val live: MutableMap<String, Float> = mutableMapOf()

            while (isActive) {
                try {
                    N54Parameters.ALL.keys.forEach { pidCode ->
                        val command = "$pidCode\r"
                        val response = sendCommandAndWait(command)
                        if (response.isNotEmpty()) {
                            val value = parseOBDResponse(pidCode, response)
                            if (value != null) live[pidCode] = value
                        }
                    }
                    _obdData.value = live.mapValues { (pid, v) ->
                        val def = N54Parameters.ALL[pid] ?: return@mapValues null
                        OBDParameter(pid = pid, value = v, unit = def.unit)
                    }.filterValues { it != null }.mapValues { it.value!! }
                    delay(500)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun sendCommand(command: String) {
        try {
            serialPort?.write(command.toByteArray(Charsets.US_ASCII), 1000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun sendCommandAndWait(command: String): String = withContext(Dispatchers.IO) {
        val response = StringBuilder()
        val startTime = System.currentTimeMillis()

        sendCommand(command)

        while (System.currentTimeMillis() - startTime < 1000) {
            try {
                val buffer = ByteArray(64)
                val bytesRead = serialPort?.read(buffer, 1000) ?: 0
                if (bytesRead > 0) {
                    response.append(String(buffer, 0, bytesRead, Charsets.US_ASCII))
                    if (response.contains(">")) break
                }
                delay(50)
            } catch (e: Exception) {
                break
            }
        }
        response.toString()
    }

    private fun parseOBDResponse(pidCode: String, response: String): Float? {
        return try {
            val cleaned = response.replace("[\r\n>]".toRegex(), "").trim()
            if (cleaned.length < 4) return null
            when (pidCode) {
                "010C" -> {
                    val a = cleaned.substring(4, 6).toInt(16)
                    val b = cleaned.substring(6, 8).toInt(16)
                    ((a * 256) + b) / 4f
                }
                "0105", "015C" -> cleaned.substring(4, 6).toInt(16) - 40f
                "010B" -> cleaned.substring(4, 6).toInt(16).toFloat()
                "010A", "012E" -> cleaned.substring(4, 6).toInt(16) * 3f
                "0142" -> {
                    val a = cleaned.substring(4, 6).toInt(16)
                    val b = cleaned.substring(6, 8).toInt(16)
                    ((a * 256) + b) / 1000f
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun disconnect() {
        readingJob?.cancel()
        serialPort?.close()
        connection?.close()
        _isConnected.value = false
        scope.cancel()
    }
}
