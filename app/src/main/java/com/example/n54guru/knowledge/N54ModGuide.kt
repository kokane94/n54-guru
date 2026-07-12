package com.example.n54guru.knowledge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.n54guru.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModGuideScreen(onDetail: (String) -> Unit) {
    var selectedStage by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(N54Colors.background)
            .padding(16.dp)
    ) {
        N54ScreenHeader(title = "Mod Guide", subtitle = "Build your N54 by stage")

        Spacer(Modifier.height(12.dp))

        // Stage chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            STAGES.forEachIndexed { index, stage ->
                N54StageChip(
                    label = "Stage $index",
                    selected = selectedStage == index,
                    onClick = { selectedStage = index },
                    leadingIcon = Icons.Filled.Bolt
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        val stage = STAGES[selectedStage]

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Stage intro card
            N54Card {
                Text(stage.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = N54Colors.textPrimary)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Bolt, contentDescription = null, tint = N54Colors.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stage.power, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = N54Colors.primary)
                }
                Spacer(Modifier.height(8.dp))
                Text(stage.description, fontSize = 14.sp, color = N54Colors.textSecondary)
            }

            Spacer(Modifier.height(12.dp))

            // Mod cards
            stage.mods.forEach { mod ->
                ModCard(mod, onClick = { onDetail(mod.id) })
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ModCard(mod: ModItem, onClick: () -> Unit) {
    N54Card(onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(mod.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = N54Colors.textPrimary)
                Text(mod.brand, fontSize = 13.sp, color = N54Colors.textMuted)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(mod.price, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = N54Colors.primary)
                N54SeverityBadge(mod.priority)
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = N54Colors.textMuted, modifier = Modifier.size(22.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModDetailScreen(modId: String, onBack: () -> Unit) {
    val mod = STAGES.flatMap { it.mods }.find { it.id == modId } ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(N54Colors.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        N54BackButton(onBack)
        Spacer(Modifier.height(8.dp))

        N54ScreenHeader(title = mod.name, subtitle = mod.brand, icon = Icons.Filled.Build)
        Spacer(Modifier.height(16.dp))

        N54Card {
            Text(mod.price, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = N54Colors.primary)
            Spacer(Modifier.height(6.dp))
            N54SeverityBadge(mod.priority)
            Spacer(Modifier.height(16.dp))
            Text(mod.description, color = N54Colors.textSecondary)
        }
    }
}

data class Stage(
    val title: String,
    val power: String,
    val description: String,
    val mods: List<ModItem>
)

data class ModItem(
    val id: String,
    val name: String,
    val brand: String,
    val price: String,
    val priority: String,
    val description: String
)

val STAGES = listOf(
    Stage(
        title = "Stage 0 — Reliability",
        power = "~300 WHP (stock)",
        description = "Essential reliability mods before adding power. These address known N54 weak points.",
        mods = listOf(
            ModItem("cp", "Charge Pipe Upgrade", "ARM Motorsports / BMS", "$100-180", "Must-Have", "Stock charge pipe is plastic and cracks under boost. Upgrade prevents boost leaks and catastrophic failure."),
            ModItem("occ", "Oil Catch Can", "Burger Motorsports / Mishimoto", "$80-200", "Recommended", "Reduces carbon buildup on intake valves and keeps intake tract clean."),
            ModItem("inj", "Index 12 Injectors", "BMW OEM", "$350-500 (set)", "Must-Have", "Index 12 is the latest revision and most reliable. Earlier index injectors leak and cause misfires."),
            ModItem("wb", "Walnut Blast", "Service", "$300-500", "Recommended", "Clean carbon buildup from intake valves to restore compression and idle quality."),
            ModItem("wp", "Water Pump + Thermostat", "BMW OEM / Pierburg", "$200-400", "Must-Have", "Electric water pump is a common failure point; replace preventively to avoid overheating.")
        )
    ),
    Stage(
        title = "Stage 1 — Bolt-Ons",
        power = "~350-400 WHP",
        description = "Basic bolt-ons with stock turbos. No internal engine work required.",
        mods = listOf(
            ModItem("tune", "ECU Tune", "MHD / Bootmod3 / JB4", "$300-600", "Must-Have", "Unlocks power from bolt-ons and controls boost/fueling safely."),
            ModItem("dp", "Downpipes", "VRSF / BMS / Active", "$300-600", "Recommended", "Reduces exhaust backpressure, lowers EGTs, and improves turbo spool."),
            ModItem("fmic", "Intercooler", "Wagner / VRSF / ETS", "$500-1000", "Recommended", "Keeps intake temps down on repeated pulls; critical in hot climates."),
            ModItem("intake", "Intake", "BMS / Injen / AFE", "$200-400", "Optional", "Adds turbo sound and marginally improves flow."),
            ModItem("plugs", "Spark Plugs / Coils", "NGK 95770 / Delphi", "$100-300", "Must-Have", "One-step colder plugs and fresh coils are mandatory for tuned cars.")
        )
    ),
    Stage(
        title = "Stage 2 — Hybrid / Stock Frame",
        power = "~400-550 WHP",
        description = "Upgraded stock-frame turbos, full bolt-ons, and supporting fueling.",
        mods = listOf(
            ModItem("hybrid", "Hybrid Turbos", "Pure / VTT / RB", "$1500-2500", "Must-Have", "Upgraded stock-frame turbos with larger wheels for more flow."),
            ModItem("pi", "Port Injection", "BMS / Fuel-It", "$800-1500", "Must-Have", "Adds secondary injectors to fuel high-power targets beyond DI limits."),
            ModItem("lpfp", "Low Pressure Fuel Pump", "Fuel-It / Walbro", "$400-800", "Must-Have", "Supplies enough fuel volume for port injection and high boost."),
            ModItem(" clutch", "Upgraded Clutch", "SPEC / MFactory", "$1500-3000", "Recommended", "Stock clutch will slip above ~400 whp."),
            ModItem("cooling", "Cooling Upgrades", "CSF / Wagner", "$500-1200", "Recommended", "Bigger radiator and oil cooler help sustain power on track.")
        )
    ),
    Stage(
        title = "Stage 3 — Big Turbo",
        power = "~550-700+ WHP",
        description = "Beyond stock turbo limits. Requires turbo upgrade and significant supporting mods.",
        mods = listOf(
            ModItem("bt", "Turbo Upgrade", "Vargas Stage 1-3 / Pure Stage 2", "$2000-5000", "Must-Have", "Large single or big twin setup to move serious air."),
            ModItem("trans", "Built Transmission (6MT) / Upgraded Clutch", "OS Giken / Competition Clutch", "$1500-4000", "Must-Have", "Stock 6MT can handle ~500 whp; above that build it or swap auto."),
            ModItem("fuel", "Fueling Upgrade (PI + DI)", "VTT / Fuel-It", "$800-2000", "Must-Have", "High-flow DI injectors + port injection for full fueling."),
            ModItem("fmic2", "Upgraded Intercooler (Race FMIC)", "Wagner / ETS", "$800-1500", "Must-Have", "Large volume intercooler to keep IATs in check at high boost."),
            ModItem("internals", "Engine Internals (Rods/Pistons)", "CP / Manley / Carrillo", "$3000-6000+", "Recommended", "At 700+ whp, forged rods and pistons reduce the risk of catastrophic failure."),
            ModItem("hpfp", "HPFP Upgrade", "Autotech / VTT", "$300-600", "Must-Have", "High-pressure fuel pump upgrade to support more DI fueling.")
        )
    )
)
