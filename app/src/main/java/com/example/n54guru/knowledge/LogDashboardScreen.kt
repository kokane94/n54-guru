package com.example.n54guru.knowledge

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.n54guru.logging.DataLogger
import com.example.n54guru.protocol.N54DmeLiveDataSource
import com.example.n54guru.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Live log dashboard.
 *
 * Shows a list of recorded log files, lets the user start/stop a new
 * recording, and offers a CSV viewer for the most recent log. Each
 * row of CSV is rendered as a card with the full PIDs and the
 * timestamp, so the user can scan through a session without leaving
 * the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDashboardScreen(source: N54DmeLiveDataSource) {
    val context = LocalContext.current
    val logger = remember { DataLogger(context, source) }
    val status by logger.status.collectAsState()
    val liveData by source.liveData.collectAsState()
    var selectedFile by remember { mutableStateOf<File?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(N54Colors.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        N54ScreenHeader(
            title = "Data Logger",
            subtitle = "Record live OBD-II data to CSV for later analysis"
        )
        Spacer(Modifier.height(16.dp))

        // Control card
        N54Card {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (status.isLogging) N54Colors.destructive.copy(alpha = 0.18f)
                            else N54Colors.primary.copy(alpha = 0.18f),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (status.isLogging) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = if (status.isLogging) N54Colors.destructive else N54Colors.primary
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        if (status.isLogging) "RECORDING" else "Ready to record",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${status.sampleCount} samples captured",
                        style = MaterialTheme.typography.bodySmall,
                        color = N54Colors.mutedForeground
                    )
                }
            }
            status.currentFile?.let { path ->
                Spacer(Modifier.height(4.dp))
                Text("File: $path", style = MaterialTheme.typography.bodySmall, color = N54Colors.mutedForeground)
            }
            status.error?.let { err ->
                Spacer(Modifier.height(4.dp))
                Text("⚠ $err", color = N54Colors.destructive, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(12.dp))
            if (!status.isLogging) {
                N54PrimaryButton(
                    text = "Start Logging",
                    onClick = { logger.start() },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                N54OutlinedButton(
                    text = "Stop Logging",
                    onClick = { logger.stop() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        // Live readout summary
        N54Card {
            Text("Current Reading", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text("RPM: ${liveData.rpm.toInt()}")
            Text("Coolant: ${liveData.coolantTemp.toInt()}°C")
            Text("Rail Pressure: ${liveData.fuelRailGaugePressure.toInt()} kPa")
            Text("Throttle: ${liveData.throttlePos.toInt()}%")
            Text("STFT: %+.1f".format(liveData.shortFuelTrim))
        }
        Spacer(Modifier.height(16.dp))

        // Log file list
        Text("Saved Logs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        val logs = remember(status) { logger.listLogs() }
        if (logs.isEmpty()) {
            Text(
                "No logs yet",
                style = MaterialTheme.typography.bodySmall,
                color = N54Colors.mutedForeground
            )
        } else {
            logs.forEach { file ->
                LogFileRow(
                    file = file,
                    onView = { selectedFile = file },
                    onShare = {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share N54 log"))
                    }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    selectedFile?.let { file ->
        LogViewerDialog(file = file, onDismiss = { selectedFile = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogFileRow(file: File, onView: () -> Unit, onShare: () -> Unit) {
    val ts = remember(file) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(Date(file.lastModified()))
    }
    N54Card(onClick = onView) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(file.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Text("$ts • ${file.length() / 1024} KB", style = MaterialTheme.typography.bodySmall, color = N54Colors.mutedForeground)
            }
            TextButton(onClick = onShare) {
                Icon(Icons.Filled.Save, contentDescription = null, tint = N54Colors.primary)
                Spacer(Modifier.width(4.dp))
                Text("Share", color = N54Colors.primary)
            }
            TextButton(onClick = onView) { Text("View", color = N54Colors.primary) }
        }
    }
}

@Composable
private fun LogViewerDialog(file: File, onDismiss: () -> Unit) {
    val lines = remember(file) {
        try { file.readLines() } catch (e: Exception) { listOf("Failed to read log: ${e.message}") }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(file.name) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (lines.isEmpty()) {
                    Text("Empty log")
                } else {
                    Text(lines[0], style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    lines.drop(1).takeLast(30).reversed().forEach { line ->
                        Text(line, style = MaterialTheme.typography.bodySmall)
                    }
                    if (lines.size > 31) {
                        Spacer(Modifier.height(4.dp))
                        Text("...${lines.size - 31} more rows...", style = MaterialTheme.typography.bodySmall, color = N54Colors.mutedForeground)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
