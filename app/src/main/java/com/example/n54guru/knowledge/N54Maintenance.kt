package com.example.n54guru.knowledge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.n54guru.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(onDetail: (String) -> Unit) {
    var showAdd by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(N54Colors.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        N54ScreenHeader(
            title = "Maintenance",
            subtitle = "Track your N54 services",
            action = {
                N54PrimaryButton(
                    text = "+ Log Service",
                    onClick = { showAdd = true },
                    leadingIcon = Icons.Filled.Add
                )
            }
        )

        Spacer(Modifier.height(8.dp))

        N54SectionHeader("RECOMMENDED SCHEDULE")

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.height(560.dp)
        ) {
            items(MAINTENANCE_ITEMS) { item ->
                MaintenanceCard(item, onClick = { onDetail(item.id) })
            }
        }

        Spacer(Modifier.height(16.dp))

        N54SectionHeader("SERVICE HISTORY")
        N54Card {
            Text(
                "No services logged yet. Tap \"Log Service\" to start tracking.",
                color = N54Colors.textSecondary,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Spacer(Modifier.height(40.dp))
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Log Service") },
            text = { Text("Service logging will be implemented with persistent storage in the next update.") },
            confirmButton = {
                TextButton(onClick = { showAdd = false }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun MaintenanceCard(item: MaintenanceItem, onClick: () -> Unit) {
    N54Card(onClick = onClick) {
        Text(item.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = N54Colors.textPrimary)
        Spacer(Modifier.height(4.dp))
        Text(item.interval, fontSize = 13.sp, color = N54Colors.textSecondary)
        if (item.critical) {
            Spacer(Modifier.height(8.dp))
            N54SeverityBadge("Critical")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(serviceId: String, onBack: () -> Unit) {
    val item = MAINTENANCE_ITEMS.find { it.id == serviceId } ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(N54Colors.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        N54BackButton(onBack)
        Spacer(Modifier.height(8.dp))
        N54ScreenHeader(title = item.name, subtitle = item.interval, icon = Icons.Filled.DateRange)
        Spacer(Modifier.height(16.dp))
        N54Card {
            if (item.critical) {
                N54SeverityBadge("Critical")
                Spacer(Modifier.height(12.dp))
            }
            Text(item.description, color = N54Colors.textSecondary)
            Spacer(Modifier.height(12.dp))
            Text("Parts to use:", fontWeight = FontWeight.SemiBold, color = N54Colors.textPrimary)
            item.parts.forEach { N54Bullet(it) }
        }
    }
}

data class MaintenanceItem(
    val id: String,
    val name: String,
    val interval: String,
    val critical: Boolean,
    val description: String,
    val parts: List<String>
)

val MAINTENANCE_ITEMS = listOf(
    MaintenanceItem("oil", "Oil Change", "5,000-7,000 mi", true, "N54 benefits from frequent oil changes due to turbo heat and fuel dilution. Use LL-01 or better synthetic oil.", listOf("5W-40 or 5W-30 synthetic", "OEM Mahle filter", "New drain plug washer")),
    MaintenanceItem("plugs", "Spark Plugs", "15,000-20,000 mi", true, "Tuned N54s need fresh plugs more often. Gap to spec for your boost level.", listOf("NGK 95770 one-step colder", "Anti-seize", "Proper gap tool")),
    MaintenanceItem("coils", "Ignition Coils", "30,000-50,000 mi", false, "Replace coils when misfires appear or during tune-up.", listOf("Bosch / Delphi coils", "Dielectric grease")),
    MaintenanceItem("walnut", "Walnut Blast", "50,000 mi", true, "Carbon buildup hurts idle, compression, and throttle response. Walnut blast intake valves.", listOf("Walnut shell media", "New intake gaskets", "Shop service or DIY blaster")),
    MaintenanceItem("pump", "Water Pump + Thermostat", "60,000-80,000 mi", true, "Electric water pump and plastic thermostat are common failure points. Replace preventively.", listOf("BMW/Pierburg water pump", "BMW thermostat", "Coolant (BMW blue)")),
    MaintenanceItem("fuel_filter", "Fuel Filter", "30,000 mi", false, "Clogged fuel filter can contribute to HPFP strain and rail pressure faults.", listOf("OEM fuel filter", "Fuel line clamps")),
    MaintenanceItem("brake_fluid", "Brake Fluid Flush", "24,000 mi / 2 years", false, "DOT4 fluid absorbs moisture over time. Flush to maintain pedal feel.", listOf("DOT4 LV fluid", "Bleeder bottle / pressure bleeder")),
    MaintenanceItem("coolant", "Coolant Flush", "60,000 mi / 4 years", false, "Replace coolant and refresh system to avoid pump and radiator deposits.", listOf("BMW blue coolant", "Distilled water for 50/50 mix")),
    MaintenanceItem("trans", "Transmission Fluid", "60,000 mi", true, "Fresh fluid keeps the 6MT/6AT happy, especially if tracked.", listOf("MTF-LT-3 for manual", "Shell TF-0870 for auto", "New fill/drain plugs")),
    MaintenanceItem("diff", "Differential Fluid", "60,000 mi", false, "Limited slip diff and rear diff need fresh fluid.", listOf("75W-90 synthetic", "LSD additive if applicable")),
    MaintenanceItem("belt", "Serpentine Belt", "50,000-60,000 mi", true, "Cracked belt can snap and cause overheating / no alternator.", listOf("OEM belt", "Check tensioner/idler pulleys")),
    MaintenanceItem("vanos", "VANOS Solenoids", "80,000-100,000 mi", false, "Clean or replace sticky solenoids to restore VANOS response.", listOf("VANOS solenoid seals", "BMW solenoid cleaner or new unit"))
)
