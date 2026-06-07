package com.example.n54guru.services

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import com.example.n54guru.models.OBDParameter
import com.example.n54guru.models.N54Parameters
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OBDService(private val context: Context) {
    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var serialDevice: UsbSerialDevice? = null
    private var connection: UsbDeviceConnection? = null

    private val _obdData = MutableStateFlow<Map<String, OBDParameter>>(emptyMap())
    val obdData: StateFlow<Map<String, OBDParameter>> = _obdData

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var readingJob: Job? = null

    fun connectToOBD(): Boolean {
        return try {
            val devices = usbManager.deviceList.values
            val obdDevice = devices.find {
                (it.vendorId == 0x067B && it.productId == 0x2303) ||
                (it.vendorId == 0x10c4 && it.productId == 0xea60) ||
                (it.vendorId == 0x0403 && it.productId == 0x6001) ||
                (it.vendorId == 0x1a86 && it.productId == 0x7523)
            }

            if (obdDevice != null) {
                openSerialPort(obdDevice)
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

    private fun openSerialPort(device: UsbDevice) {
        connection = usbManager.openDevice(device)
        serialDevice = UsbSerialDevice.createUsbSerialDevice(device, connection)
        serialDevice?.apply {
            open()
            setBaudRate(38400)
            setDataBits(UsbSerialInterface.DATA_BITS_8)
            setStopBits(UsbSerialInterface.STOP_BITS_1)
            setParity(UsbSerialInterface.PARITY_NONE)
            setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
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
            val allParameters = (N54Parameters.ENGINE_PARAMETERS + N54Parameters.TRANSMISSION_PARAMETERS).toMutableMap()

            while (isActive) {
                try {
                    for ((pidCode, parameter) in allParameters) {
                        val command = "01$pidCode\r"
                        val response = sendCommandAndWait(command)

                        if (response.isNotEmpty()) {
                            val value = parseOBDResponse(pidCode, response)
                            allParameters[pidCode] = parameter.copy(value = value)
                        }
                    }

                    _obdData.emit(allParameters)
                    delay(500)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun sendCommand(command: String) {
        try {
            serialDevice?.write(command.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun sendCommandAndWait(command: String): String {
        return withContext(Dispatchers.IO) {
            val response = StringBuilder()
            val startTime = System.currentTimeMillis()

            sendCommand(command)

            while (System.currentTimeMillis() - startTime < 1000) {
                try {
                    val buffer = ByteArray(64)
                    val bytesRead = serialDevice?.syncRead(buffer, 1000) ?: 0

                    if (bytesRead > 0) {
                        response.append(String(buffer, 0, bytesRead))
                        if (response.contains(">")) break
                    }
                    delay(50)
                } catch (e: Exception) {
                    break
                }
            }

            response.toString()
        }
    }

    private fun parseOBDResponse(pidCode: String, response: String): Float {
        return try {
            val hexValues = response.split(" ").filter { it.length == 2 && it != "41" }

            when (pidCode) {
                "010C" -> {
                    val a = hexValues.getOrNull(0)?.toInt(16) ?: 0
                    val b = hexValues.getOrNull(1)?.toInt(16) ?: 0
                    ((a * 256 + b) / 4).toFloat()
                }
                "0105" -> {
                    hexValues.getOrNull(0)?.toInt(16)?.minus(40)?.toFloat() ?: 0f
                }
                "010B" -> {
                    hexValues.getOrNull(0)?.toInt(16)?.toFloat() ?: 0f
                }
                "0110" -> {
                    val a = hexValues.getOrNull(0)?.toInt(16) ?: 0
                    val b = hexValues.getOrNull(1)?.toInt(16) ?: 0
                    ((a * 256 + b) / 100).toFloat()
                }
                "012E" -> {
                    hexValues.getOrNull(0)?.toInt(16)?.times(3)?.toFloat() ?: 0f
                }
                else -> 0f
            }
        } catch (e: Exception) {
            0f
        }
    }

    fun disconnect() {
        readingJob?.cancel()
        serialDevice?.close()
        connection?.close()
        _isConnected.value = false
        scope.cancel()
    }
}
