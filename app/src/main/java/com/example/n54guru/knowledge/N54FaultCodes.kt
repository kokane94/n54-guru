package com.example.n54guru.knowledge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.n54guru.knowledge.N54FaultCodes.CODE_DATA

/**
 * N54-specific BMW DME fault code database.
 *
 * Codes are 4-character hex strings as they appear in the DME (DME = Digital
 * Motor Electronics, the Bosch ME9 engine control unit in the N54). These are
 * read via UDS service 0x19 (ReadDTCInformation) on the BMW diagnostic bus.
 *
 * Sources: public BMW technical service bulletins, BimmerForums N54 fault
 * code catalog, and reverse-engineering of the DME9 ECU fault code tables.
 */
object N54FaultCodes {

    data class FaultCode(
        val code: String,
        val title: String,
        val description: String,
        val severity: String,        // critical, warning, info
        val symptoms: List<String>,
        val causes: List<String>,
        val fixes: List<String>
    )

    val CODE_DATA: List<FaultCode> = listOf(
        FaultCode(
            code = "29CD",
            title = "Fuel Rail Pressure Too Low",
            description = "HPFP cannot maintain commanded rail pressure. Common on N54, especially with age or aftermarket fueling demands.",
            severity = "critical",
            symptoms = listOf(
                "Long cranks",
                "Stalling at idle",
                "Power loss under load"
            ),
            causes = listOf(
                "Worn HPFP internals",
                "Failed cam follower",
                "Inadequate LPFP supply",
                "Leaking fuel injector"
            ),
            fixes = listOf(
                "Replace HPFP with latest revision",
                "Inspect cam follower, replace if worn",
                "Test LPFP pressure under load",
                "Smoke-test fuel system for leaks"
            )
        ),
        FaultCode(
            code = "2A87",
            title = "Boost Pressure Not Plausible",
            description = "DME commanded boost does not match measured MAP. Indicates boost leak, overboost, or sensor fault.",
            severity = "warning",
            symptoms = listOf(
                "Power loss",
                "Limp mode",
                "Whistling from engine bay"
            ),
            causes = listOf(
                "Cracked stock charge pipe (very common)",
                "Blown diverter valve",
                "Faulty MAP sensor",
                "Wastegate stuck open"
            ),
            fixes = listOf(
                "Replace charge pipe with aluminum (ARM, BMS, VRSF)",
                "Test diverter valve, replace if leaking",
                "Inspect boost lines and couplers",
                "Check MAP sensor wiring"
            )
        ),
        FaultCode(
            code = "2A99",
            title = "Charge Pressure Too Low",
            description = "Boost is below expected threshold. Almost always a leak, not a turbo problem.",
            severity = "warning",
            symptoms = listOf(
                "Power loss",
                "Hissing under boost",
                "Check engine light"
            ),
            causes = listOf(
                "Cracked charge pipe (stock plastic)",
                "Diverter valve leak",
                "Intercooler leak",
                "Loose clamp on boost piping"
            ),
            fixes = listOf(
                "Replace stock charge pipe",
                "Replace diverter valve with stronger unit",
                "Pressure-test the charge system",
                "Re-seat all boost clamps"
            )
        ),
        FaultCode(
            code = "2A9A",
            title = "Charge Pressure Too High",
            description = "DME detected boost above commanded value. Risk of overboost damage to engine.",
            severity = "critical",
            symptoms = listOf(
                "Power cut / limp mode",
                "Engine runs rough briefly"
            ),
            causes = listOf(
                "Wastegate stuck closed",
                "Boost solenoid fault",
                "N75 valve failure"
            ),
            fixes = listOf(
                "Test wastegate actuator operation",
                "Replace N75 boost pressure solenoid",
                "Inspect boost control vacuum lines"
            )
        ),
        FaultCode(
            code = "2AAF",
            title = "VANOS Solenoid Mechanical Fault",
            description = "Variable valve timing solenoid is not operating within specification.",
            severity = "warning",
            symptoms = listOf(
                "Rough idle",
                "Power loss",
                "Cold-start rattle"
            ),
            causes = listOf(
                "Sludge buildup on solenoid screen",
                "Failed solenoid coil",
                "Low oil pressure"
            ),
            fixes = listOf(
                "Remove and clean VANOS solenoids",
                "Replace seals on solenoid",
                "Use LL-01 approved oil on schedule"
            )
        ),
        FaultCode(
            code = "2AB0",
            title = "VANOS Exhaust Solenoid Fault",
            description = "Exhaust-side VANOS solenoid not actuating correctly.",
            severity = "warning",
            symptoms = listOf(
                "Reduced power",
                "Hesitation"
            ),
            causes = listOf(
                "Solenoid failure",
                "Oil contamination"
            ),
            fixes = listOf(
                "Test solenoid resistance",
                "Replace if out of spec",
                "Verify oil quality and level"
            )
        ),
        FaultCode(
            code = "2C5B",
            title = "Electric Water Pump Failure",
            description = "DME detected the electric water pump is not operating correctly. Engine may overheat quickly.",
            severity = "critical",
            symptoms = listOf(
                "Temperature rising",
                "Coolant warning",
                "Reduced power message"
            ),
            causes = listOf(
                "Pump motor failure (very common 60-100k miles)",
                "Wiring fault",
                "DME driver failure"
            ),
            fixes = listOf(
                "Replace water pump (Pierburg OEM recommended)",
                "Replace thermostat at same time",
                "Inspect wiring harness"
            )
        ),
        FaultCode(
            code = "3000",
            title = "Cylinder 1 Misfire Detected",
            description = "DME detected misfire on cylinder 1. Counters track total misfires; check freeze frame data.",
            severity = "warning",
            symptoms = listOf(
                "Rough idle",
                "Power loss",
                "Flashing check engine light if severe"
            ),
            causes = listOf(
                "Failing ignition coil (very common)",
                "Worn spark plug",
                "Failing fuel injector (index 12 = latest)",
                "Low compression"
            ),
            fixes = listOf(
                "Swap coil from another cylinder to confirm",
                "Replace spark plugs (one step colder if tuned)",
                "Test injector flow rate",
                "Compression test if misfire persists"
            )
        ),
        FaultCode(
            code = "3001",
            title = "Cylinder 2 Misfire Detected",
            description = "Misfire on cylinder 2. Same diagnostic path as 3000.",
            severity = "warning",
            symptoms = listOf(
                "Rough idle",
                "Power loss"
            ),
            causes = listOf(
                "Failing ignition coil",
                "Worn spark plug",
                "Failing fuel injector"
            ),
            fixes = listOf(
                "Swap coil to confirm",
                "Replace plugs if due",
                "Test injector"
            )
        ),
        FaultCode(
            code = "3002",
            title = "Cylinder 3 Misfire Detected",
            description = "Misfire on cylinder 3. Same diagnostic path as 3000.",
            severity = "warning",
            symptoms = listOf(
                "Rough idle",
                "Power loss"
            ),
            causes = listOf(
                "Failing ignition coil",
                "Worn spark plug",
                "Failing fuel injector"
            ),
            fixes = listOf(
                "Swap coil",
                "Replace plugs if due",
                "Test injector"
            )
        ),
        FaultCode(
            code = "30BA",
            title = "Turbocharger Wastegate Mechanical Fault",
            description = "Wastegate mechanism on a turbo is sticking or not operating correctly. Often related to wastegate rattle.",
            severity = "warning",
            symptoms = listOf(
                "Metallic rattle at idle",
                "Boost underperformance",
                "Check engine light"
            ),
            causes = listOf(
                "Worn wastegate actuator",
                "Carbon buildup in mechanism"
            ),
            fixes = listOf(
                "Replace wastegate actuator",
                "Clean carbon from mechanism",
                "If high mileage, full turbo replacement"
            )
        ),
        FaultCode(
            code = "30BB",
            title = "Turbocharger Boost Control Fault",
            description = "Boost control system not maintaining target pressure.",
            severity = "warning",
            symptoms = listOf(
                "Power loss",
                "Boost fluctuation"
            ),
            causes = listOf(
                "N75 valve failure",
                "Wastegate issue",
                "Vacuum leak in boost control"
            ),
            fixes = listOf(
                "Test N75 valve",
                "Inspect vacuum lines",
                "Check wastegate operation"
            )
        )
    )

    val CATEGORIES: List<String> = listOf("all", "critical", "warning", "info")

    fun search(query: String, severity: String = "all"): List<FaultCode> {
        val q = query.lowercase()
        return CODE_DATA.filter { code ->
            val matchesQuery = q.isEmpty() ||
                code.code.lowercase().contains(q) ||
                code.title.lowercase().contains(q) ||
                code.description.lowercase().contains(q)
            val matchesSeverity = severity == "all" || code.severity == severity
            matchesQuery && matchesSeverity
        }
    }
}

@Composable
fun FaultCodesScreen(onCodeClick: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSeverity by remember { mutableStateOf("all") }

    val codes = remember(searchQuery, selectedSeverity) {
        N54FaultCodes.search(searchQuery, selectedSeverity)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("Fault Codes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text(
            "N54-specific BMW DME fault codes with causes & fixes",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by code or name...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            N54FaultCodes.CATEGORIES.forEach { sev ->
                FilterChip(
                    selected = selectedSeverity == sev,
                    onClick = { selectedSeverity = sev },
                    label = { Text(if (sev == "all") "All" else sev.replaceFirstChar { it.uppercase() }, fontSize = 12.sp) }
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        if (codes.isEmpty()) {
            Text("No fault codes found", modifier = Modifier.padding(32.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(codes, key = { it.code }) { code ->
                    FaultCodeCard(code = code, onClick = { onCodeClick(code.code) })
                }
            }
        }
    }
}

@Composable
private fun FaultCodeCard(code: N54FaultCodes.FaultCode, onClick: () -> Unit) {
    val severityColor = when (code.severity) {
        "critical" -> Color(0xFFEF4444)
        "warning" -> Color(0xFFF59E0B)
        else -> Color(0xFF6B7280)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(severityColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(code.code.take(2), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = severityColor)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(code.code, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(code.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(code.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaultCodeDetailScreen(code: String, onBack: () -> Unit) {
    val codeData = CODE_DATA.find { it.code == code }
    if (codeData == null) {
        Text("Code not found")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Back") }
        Spacer(Modifier.height(8.dp))
        Text(codeData.code, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
        Text(codeData.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        AssistChip(onClick = {}, label = { Text(codeData.severity) })
        Spacer(Modifier.height(12.dp))
        Text(codeData.description, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))

        Text("Symptoms", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        codeData.symptoms.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
        Spacer(Modifier.height(12.dp))

        Text("Likely Causes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        codeData.causes.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
        Spacer(Modifier.height(12.dp))

        Text("Fixes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        codeData.fixes.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
    }
}
