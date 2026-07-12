package com.example.n54guru.knowledge

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.n54guru.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaultCodesScreen(onDetail: (String) -> Unit) {
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Fuel", "Boost", "Ignition", "Turbo", "Engine")

    val filtered = remember(search, selectedCategory) {
        N54FaultCodes.CODE_DATA.filter { code ->
            val matchesSearch = search.isBlank() ||
                code.code.contains(search, ignoreCase = true) ||
                code.title.contains(search, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || code.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(N54Colors.background)
            .padding(16.dp)
    ) {
        N54TextField(
            value = search,
            onValueChange = { search = it },
            label = "Search",
            placeholder = "Search by code or name...",
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = N54Colors.textMuted) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Category chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            categories.forEach { cat ->
                N54FilterChip(
                    label = cat,
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered) { code ->
                FaultCodeRow(code, onClick = { onDetail(code.code) })
            }
        }
    }
}

@Composable
private fun FaultCodeRow(code: N54FaultCodes.FaultCode, onClick: () -> Unit) {
    N54Card(onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = code.code,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = N54Colors.primary,
                modifier = Modifier.width(56.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = code.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = N54Colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            N54SeverityBadge(code.severity)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = N54Colors.textMuted)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaultCodeDetailScreen(code: String, onBack: () -> Unit) {
    val data = N54FaultCodes.CODE_DATA.find { it.code == code } ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(N54Colors.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        N54BackButton(onBack)
        Spacer(Modifier.height(8.dp))

        N54ScreenHeader(title = data.code, subtitle = data.title, icon = Icons.Filled.Speed)

        Spacer(Modifier.height(16.dp))

        N54Card {
            Row(verticalAlignment = Alignment.CenterVertically) {
                N54SeverityBadge(data.severity)
                Spacer(Modifier.width(8.dp))
                Text(data.category, fontSize = 13.sp, color = N54Colors.textMuted)
            }
            Spacer(Modifier.height(12.dp))
            Text("Description", fontWeight = FontWeight.SemiBold, color = N54Colors.textPrimary)
            Text(data.description, color = N54Colors.textSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            Text("Likely Causes", fontWeight = FontWeight.SemiBold, color = N54Colors.textPrimary)
            data.causes.forEach { N54Bullet(it) }
            Spacer(Modifier.height(12.dp))
            Text("Fixes", fontWeight = FontWeight.SemiBold, color = N54Colors.textPrimary)
            data.fixes.forEach { N54Bullet(it) }
        }
    }
}

object N54FaultCodes {
    data class FaultCode(
        val code: String,
        val title: String,
        val category: String,
        val severity: String,
        val description: String,
        val causes: List<String>,
        val fixes: List<String>
    )

    val CODE_DATA = listOf(
    FaultCode("29CD", "Fuel Rail Pressure Too Low", "Fuel", "High", "Low pressure in the high-pressure fuel rail, often caused by a failing HPFP or weak LPFP.", listOf("Failing HPFP", "Weak LPFP", "Leaking injector", "Clogged fuel filter"), listOf("Check actual vs requested rail pressure", "Test HPFP volume", "Replace LPFP if pressure drops" )),
    FaultCode("29D0", "Fuel Rail Pressure Too High", "Fuel", "Medium", "Rail pressure exceeds target. Usually a regulator or sensor issue.", listOf("Faulty rail pressure sensor", "Stuck HPFP solenoid"), listOf("Replace rail pressure sensor", "Inspect HPFP control valve")),
    FaultCode("2A99", "Charge Pressure Too Low", "Boost", "High", "Boost pressure below requested value.", listOf("Boost leak", "Wastegate stuck open", "Faulty boost solenoid", "Turbo failing"), listOf("Smoke test for boost leaks", "Check wastegate actuation", "Test boost solenoids")),
    FaultCode("2A9A", "Charge Pressure Too High", "Boost", "High", "Boost pressure above target.", listOf("Wastegate stuck closed", "Boost solenoid stuck", "Tuning issue"), listOf("Inspect wastegate linkage", "Test boost control solenoid")),
    FaultCode("29DC", "Misfire Cylinder 1", "Ignition", "High", "Combustion event missed on cylinder 1.", listOf("Bad spark plug/coil/injector", "Carbon buildup", "Low compression"), listOf("Swap coil/plug to another cylinder", "Check compression", "Walnut blast intake valves")),
    FaultCode("29DD", "Misfire Cylinder 2", "Ignition", "High", "Combustion event missed on cylinder 2.", listOf("Bad spark plug/coil/injector", "Carbon buildup", "Low compression"), listOf("Swap coil/plug", "Check compression", "Walnut blast")),
    FaultCode("29DE", "Misfire Cylinder 3", "Ignition", "High", "Combustion event missed on cylinder 3.", listOf("Bad spark plug/coil/injector", "Carbon buildup", "Low compression"), listOf("Swap coil/plug", "Check compression", "Walnut blast")),
    FaultCode("29DF", "Misfire Cylinder 4", "Ignition", "High", "Combustion event missed on cylinder 4.", listOf("Bad spark plug/coil/injector", "Carbon buildup", "Low compression"), listOf("Swap coil/plug", "Check compression", "Walnut blast")),
    FaultCode("29E0", "Misfire Cylinder 5", "Ignition", "High", "Combustion event missed on cylinder 5.", listOf("Bad spark plug/coil/injector", "Carbon buildup", "Low compression"), listOf("Swap coil/plug", "Check compression", "Walnut blast")),
    FaultCode("29E1", "Misfire Cylinder 6", "Ignition", "High", "Combustion event missed on cylinder 6.", listOf("Bad spark plug/coil/injector", "Carbon buildup", "Low compression"), listOf("Swap coil/plug", "Check compression", "Walnut blast")),
    FaultCode("30BA", "Wastegate 1 Stuck Open/Closed", "Turbo", "High", "Turbo 1 wastegate position outside expected range.", listOf("Wastegate actuator failure", "Vacuum leak", "Stuck wastegate flap"), listOf("Check vacuum lines", "Inspect wastegate movement", "Replace actuator if needed")),
    FaultCode("30BB", "Wastegate 2 Stuck Open/Closed", "Turbo", "High", "Turbo 2 wastegate position outside expected range.", listOf("Wastegate actuator failure", "Vacuum leak", "Stuck wastegate flap"), listOf("Check vacuum lines", "Inspect wastegate movement", "Replace actuator")),
    FaultCode("2AAF", "VANOS Solenoid Intake", "Engine", "Medium", "Intake VANOS solenoid control or position issue.", listOf("Dirty VANOS solenoid", "Bad solenoid", "Low oil pressure"), listOf("Clean or replace intake VANOS solenoid", "Check oil level/quality")),
    FaultCode("2AB0", "VANOS Solenoid Exhaust", "Engine", "Medium", "Exhaust VANOS solenoid control or position issue.", listOf("Dirty VANOS solenoid", "Bad solenoid", "Low oil pressure"), listOf("Clean or replace exhaust VANOS solenoid", "Check oil level/quality")),
    FaultCode("2C5B", "Electric Water Pump Malfunction", "Engine", "High", "DME detects water pump failure or no coolant flow.", listOf("Failed electric water pump", "Wiring fault", "Low coolant"), listOf("Replace electric water pump", "Bleed cooling system")),
    FaultCode("2E81", "Oil Condition Sensor", "Engine", "Low", "Oil level/quality sensor reports out-of-range value.", listOf("Faulty oil condition sensor", "Wiring issue"), listOf("Replace oil condition sensor")),
    FaultCode("2C01", "Thermostat Malfunction", "Engine", "Medium", "Coolant thermostat heater control circuit issue.", listOf("Failed thermostat", "Wiring fault"), listOf("Replace thermostat")),
    FaultCode("2A87", "Boost Pressure Sensor", "Boost", "Medium", "Boost pressure sensor signal implausible.", listOf("Faulty boost pressure sensor", "Vacuum line cracked"), listOf("Replace boost pressure sensor", "Check vacuum hose to sensor"))
    )
}
