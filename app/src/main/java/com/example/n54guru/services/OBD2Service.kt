package com.example.n54guru.services

import android.content.Context
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.*
import com.example.n54guru.models.OBDParameter

class OBD2Service(private val context: Context) {

    private var port: UsbSerialPort? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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
        // Simulated data for now (works without adapter)
        return mapOf(
            "RPM" to OBDParameter("010C", (1200..4500).random().toFloat(), "RPM"),
            "CoolantTemp" to OBDParameter("0105", (75..105).random().toFloat(), "°C"),
            "Boost" to OBDParameter("010B", (0.8..1.8).random().toFloat(), "bar")
        )
    }

    fun disconnect() {
        port?.close()
        scope.cancel()
    }
}
