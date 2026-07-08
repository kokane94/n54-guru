package com.example.n54guru.knowledge

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.n54guru.protocol.N54DmeLiveDataSource
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Live data dashboard with the N54 DME source backing it.
 *
 * Shows real RPM, coolant, intake air, throttle, MAF, fuel trims, timing,
 * fuel rail pressure, and vehicle speed. Refreshes at 20 Hz.
 *
 * Includes USB device picker for the K-CAN adapter, and DTC scan button
 * that reads all stored DTCs and shows them with N54-specific
 * cross-references.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveDataScreen(
    source: N54DmeLiveDataSource,
    onShowPartner: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val data by source.liveData.collectAsState()
    val dtcs by source.dtcs.collectAsState()
    val isScanning by source.isScanning.collectAsState()

    var usbDevices by remember { mutableStateOf<List<UsbDevice>>(emptyList()) }
    var showDevicePicker by remember { mutableStateOf(false) }
    var vin by remember { mutableStateOf<String?>(null) }
    var dmeId by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var dtcScanResult by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Connection status card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (data.isConnected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Usb,
                        contentDescription = null,
                        tint = if (data.isConnected) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (data.isConnected) "Connected to N54 DME" else "USB Adapter Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (data.isConnected) "Live data @ 20 Hz over ISO 15765 / UDS"
                    else "Plug in K-CAN cable or ELM327 OBD-II adapter",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!data.isConnected) {
                        Button(onClick = {
                            usbDevices = N54DmeLiveDataSource.listAvailableDevicesFromContext(context)
                            showDevicePicker = usbDevices.isNotEmpty()
                        }) { Text("Connect") }
                    } else {
                        OutlinedButton(onClick = { source.disconnect() }) { Text("Disconnect") }
                        OutlinedButton(onClick = {
                        scope.launch { source.connectCurrentDevice(context) }
                    }) { Text("Reconnect") }
                    }
                    OutlinedButton(onClick = onShowPartner) { Text("AI Partner") }
                }
                data.errorMessage?.let { err ->
                    Spacer(Modifier.height(8.dp))
                    Text("⚠ $err", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Live data grid
        if (data.isConnected) {
            LiveDataGrid(data)
            Spacer(Modifier.height(12.dp))

            // DTC scan button
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Speed, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("DTC Scan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "Read all stored diagnostic trouble codes from the DME",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val result = source.scanDtcs()
                                    dtcScanResult = if (result.isEmpty()) "No DTCs found"
                                    else "${result.size} code(s): ${result.joinToString { it.code }}"
                                }
                            },
                            enabled = !isScanning
                        ) { Text(if (isScanning) "Scanning..." else "Scan DTCs") }
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val ok = source.clearAllDtcs()
                                    dtcScanResult = if (ok) "DTCs cleared" else "Failed to clear"
                                }
                            }
                        ) { Text("Clear DTCs") }
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    vin = source.readVin()
                                    dmeId = source.readDmeIdentification()
                                }
                            }
                        ) { Text("Read VIN + DME ID") }
                    }
                    dtcScanResult?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                    vin?.let {
                        Spacer(Modifier.height(8.dp))
                        Text("VIN: $it", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                    dmeId.forEach { (k, v) ->
                        Text("$k: $v", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Active DTCs list
            if (dtcs.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("Active DTCs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                dtcs.forEach { dtc ->
                    val matched = N54FaultCodes.CODE_DATA.find { fc ->
                        dtc.code.contains(fc.code.takeLast(4), ignoreCase = true) ||
                        fc.title.lowercase().contains(dtc.code.lowercase().takeLast(4))
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (dtc.isConfirmed) Color(0xFFEF4444).copy(alpha = 0.1f)
                            else Color(0xFFF59E0B).copy(alpha = 0.1f)
                        )
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                "${dtc.code} ${if (dtc.isConfirmed) "[STORED]" else "[PENDING]"}",
                                fontWeight = FontWeight.Bold,
                                color = if (dtc.isConfirmed) Color(0xFFEF4444) else Color(0xFFF59E0B)
                            )
                            matched?.let {
                                Text(it.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(it.description, style = MaterialTheme.typography.bodySmall)
                            } ?: Text("No N54 reference for this code in local DB", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("To connect:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text("• Plug your K-CAN cable or ELM327 OBD-II adapter into the car's diagnostic port (under hood, driver side for E93)")
                    Spacer(Modifier.height(4.dp))
                    Text("• Connect the USB end to your phone (USB OTG adapter required)")
                    Spacer(Modifier.height(4.dp))
                    Text("• Grant USB permission when prompted")
                    Spacer(Modifier.height(4.dp))
                    Text("• Tap Connect and select your adapter from the list")
                }
            }
        }
    }

    if (showDevicePicker) {
        AlertDialog(
            onDismissRequest = { showDevicePicker = false },
            title = { Text("Select USB Adapter") },
            text = {
                Column {
                    usbDevices.forEach { device ->
                        TextButton(onClick = {
                            showDevicePicker = false
                            scope.launch {
                                source.connectToDevice(context, device)
                            }
                        }) {
                            Text("${device.productName ?: "USB Device"} (VID=${device.vendorId}, PID=${device.productId})")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDevicePicker = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun LiveDataGrid(data: N54DmeLiveDataSource.LiveData) {
    Column {
        Text("Live Data @ 20 Hz", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiveTile("RPM", data.rpm.roundToInt().toString(), "rev/min", Modifier.weight(1f))
            LiveTile("Speed", data.vehicleSpeed.roundToInt().toString(), "km/h", Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiveTile("Coolant", data.coolantTemp.roundToInt().toString(), "°C", Modifier.weight(1f),
                warn = data.coolantTemp > 105f, crit = data.coolantTemp > 115f)
            LiveTile("Intake Air", data.intakeAirTemp.roundToInt().toString(), "°C", Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiveTile("Throttle", data.throttlePos.roundToInt().toString(), "%", Modifier.weight(1f))
            LiveTile("MAF", data.mafRate.roundToInt().toString(), "g/s", Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiveTile("STFT", String.format("%+.1f", data.shortFuelTrim), "%", Modifier.weight(1f),
                warn = kotlin.math.abs(data.shortFuelTrim) > 8f)
            LiveTile("LTFT", String.format("%+.1f", data.longFuelTrim), "%", Modifier.weight(1f),
                warn = kotlin.math.abs(data.longFuelTrim) > 8f)
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiveTile("Timing", String.format("%+.1f", data.timingAdvance), "°", Modifier.weight(1f))
            LiveTile("Rail Press", data.fuelRailGaugePressure.roundToInt().toString(), "kPa", Modifier.weight(1f),
                warn = data.fuelRailGaugePressure < 5000f)
        }
    }
}

@Composable
private fun LiveTile(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    warn: Boolean = false,
    crit: Boolean = false
) {
    val bg = when {
        crit -> Color(0xFFEF4444).copy(alpha = 0.15f)
        warn -> Color(0xFFF59E0B).copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val valueColor = when {
        crit -> Color(0xFFEF4444)
        warn -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.onSurface
    }
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = valueColor)
                Spacer(Modifier.width(4.dp))
                Text(unit, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}
