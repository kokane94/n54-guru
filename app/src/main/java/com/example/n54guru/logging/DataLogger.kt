package com.example.n54guru.logging

import android.content.Context
import com.example.n54guru.protocol.N54DmeLiveDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Continuous data logger for live OBD-II / UDS data.
 *
 * Subscribes to the [N54DmeLiveDataSource] and appends one row per
 * sample to a CSV file in the app's files directory. User can start/stop
 * logging from the UI.
 *
 * CSV format (header):
 *   timestamp_ms,is_connected,rpm,coolant_c,intake_air_c,throttle_pct,
 *   maf_gps,stft_pct,ltft_pct,timing_deg,rail_pressure_kpa,
 *   battery_v,speed_kph
 */
class DataLogger(
    private val context: Context,
    private val source: N54DmeLiveDataSource
) {
    data class Status(
        val isLogging: Boolean = false,
        val currentFile: String? = null,
        val sampleCount: Long = 0L,
        val error: String? = null
    )

    private val _status = MutableStateFlow(Status())
    val status: StateFlow<Status> = _status.asStateFlow()

    private var loggingJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentFile: File? = null
    private var sampleCount: Long = 0L
    private val header = "timestamp_ms,is_connected,rpm,coolant_c,intake_air_c,throttle_pct,maf_gps,stft_pct,ltft_pct,timing_deg,rail_pressure_kpa,battery_v,speed_kph"

    /**
     * Start logging. Creates a new file with a timestamp-based name.
     * Returns the absolute path of the file.
     */
    fun start(): String? {
        if (_status.value.isLogging) return currentFile?.absolutePath
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val logsDir = File(context.filesDir, "logs").apply { mkdirs() }
        val file = File(logsDir, "n54_log_$ts.csv")
        try {
            file.writeText(header + "\n")
            currentFile = file
            sampleCount = 0L
            _status.value = Status(isLogging = true, currentFile = file.absolutePath, sampleCount = 0L)
            loggingJob = scope.launch {
                source.liveData.collect { data ->
                    if (!_status.value.isLogging) return@collect
                    try {
                        val row = listOf(
                            System.currentTimeMillis(),
                            data.isConnected,
                            data.rpm.roundToInt(),
                            data.coolantTemp.roundToInt(),
                            data.intakeAirTemp.roundToInt(),
                            data.throttlePos.roundToInt(),
                            data.mafRate.roundToInt(),
                            String.format("%.1f", data.shortFuelTrim),
                            String.format("%.1f", data.longFuelTrim),
                            String.format("%.1f", data.timingAdvance),
                            data.fuelRailGaugePressure.roundToInt(),
                            String.format("%.2f", data.batteryVoltage),
                            data.vehicleSpeed.roundToInt()
                        ).joinToString(",")
                        file.appendText(row + "\n")
                        sampleCount++
                        _status.value = _status.value.copy(sampleCount = sampleCount)
                    } catch (e: Exception) {
                        _status.value = _status.value.copy(error = e.message)
                    }
                }
            }
            return file.absolutePath
        } catch (e: Exception) {
            _status.value = _status.value.copy(error = "Failed to start: ${e.message}")
            return null
        }
    }

    fun stop() {
        loggingJob?.cancel()
        loggingJob = null
        _status.value = Status(
            isLogging = false,
            currentFile = currentFile?.absolutePath,
            sampleCount = sampleCount
        )
    }

    /**
     * Return all log files in chronological order (newest first).
     */
    fun listLogs(): List<File> {
        val logsDir = File(context.filesDir, "logs")
        if (!logsDir.exists()) return emptyList()
        return logsDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
