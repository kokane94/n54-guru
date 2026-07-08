package com.example.n54guru.knowledge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Build path for the N54, organized by stage. Inspired by the Base44 Mod
 * Guide structure. Each stage lists mods in priority order, with rough cost
 * and what it enables.
 */
object N54ModGuide {

    data class ModItem(
        val title: String,
        val priority: String,    // critical, recommended, optional
        val description: String,
        val cost: String,
        val enables: String
    )

    data class Stage(
        val name: String,
        val goal: String,
        val items: List<ModItem>
    )

    val STAGES: List<Stage> = listOf(
        Stage(
            name = "Stage 1 — Reliability",
            goal = "Stock power, bulletproof the weak points before adding any power.",
            items = listOf(
                ModItem(
                    "Aluminum Charge Pipe",
                    "critical",
                    "Replace the plastic OEM charge pipe. WILL crack under boost. ARM / BMS / VRSF.",
                    "$100-180",
                    "Allows safe stock and tuned boost"
                ),
                ModItem(
                    "Index 12 Injectors",
                    "critical",
                    "Earlier index injectors are failure-prone. Index 12 is the latest revision.",
                    "$200-300",
                    "Prevents misfire codes and lean conditions"
                ),
                ModItem(
                    "Oil Catch Can",
                    "recommended",
                    "Reduces carbon buildup on intake valves by catching oil vapors from the PCV system.",
                    "$80-150",
                    "Slows intake valve carbon buildup"
                ),
                ModItem(
                    "Walnut Blast Intake Valves",
                    "recommended",
                    "Direct injection = carbon buildup. Walnut blasting cleans them. Do every 50k miles.",
                    "$300-500 shop / $100-150 DIY",
                    "Restores idle quality and prevents misfires"
                ),
                ModItem(
                    "Proactive Water Pump",
                    "recommended",
                    "Electric water pump is a known failure. Replace proactively around 60-80k miles.",
                    "$200-400",
                    "Prevents overheating and head warping"
                )
            )
        ),
        Stage(
            name = "Stage 2 — Bolt-On Power",
            goal = "Add safe power through tunes, intakes, and exhaust.",
            items = listOf(
                ModItem(
                    "Tune (JB4 or MHD Flash)",
                    "critical",
                    "JB4 = plug-and-play piggyback tuner, Map 1-7. MHD = direct ECU flash, more control. MHD is most popular for N54.",
                    "$400-700",
                    "Unlocks the N54's tuning potential"
                ),
                ModItem(
                    "Catless Downpipes",
                    "critical",
                    "Massive restriction from stock cats. Catless = biggest single bolt-on gain. Expect +30-50 WHP.",
                    "$400-800",
                    "Unlocks turbo spool and top-end power"
                ),
                ModItem(
                    "Front Mount Intercooler (FMIC)",
                    "recommended",
                    "Stock intercooler heat soaks quickly. FMIC keeps intake temps low for consistent power.",
                    "$400-800",
                    "Prevents heat-soak power loss"
                ),
                ModItem(
                    "Cold Air Intake",
                    "optional",
                    "Small gains but better turbo sound. Mostly for aesthetics and sound at this level.",
                    "$200-400",
                    "Marginal power, mostly sound"
                ),
                ModItem(
                    "Colder Spark Plugs",
                    "recommended",
                    "Stock heat range plugs can't handle added boost. One step colder prevents detonation.",
                    "$40-80",
                    "Required for any tuned setup"
                )
            )
        ),
        Stage(
            name = "Stage 3 — Big Power",
            goal = "Maxing out the stock turbos. 400-500+ WHP.",
            items = listOf(
                ModItem(
                    "E85 Flex Fuel",
                    "critical",
                    "E85 fuel allows much more aggressive timing. Huge power gains. Requires port injection or blend.",
                    "$500-1000 for kit",
                    "30-50% more power on E85 vs 91 octane"
                ),
                ModItem(
                    "Upgraded LPFP",
                    "critical",
                    "Stock LPFP can't flow enough for E85. Upgraded internals required for any ethanol content.",
                    "$300-600",
                    "Required for E85 reliability"
                ),
                ModItem(
                    "Meth/Water Injection",
                    "recommended",
                    "Alternative to E85. Sprays water/methanol into intake for knock resistance and charge cooling.",
                    "$300-600",
                    "Allows more timing advance, safer high-boost"
                ),
                ModItem(
                    "Cat-Back Exhaust",
                    "recommended",
                    "Reduces backpressure after the turbos. Helps with top-end power and turbo spool.",
                    "$500-1500",
                    "Improved sound + small power gain"
                ),
                ModItem(
                    "Oil Cooler",
                    "recommended",
                    "Oil temps climb fast with added power. Oil cooler keeps temps safe during spirited driving.",
                    "$400-800",
                    "Critical for track / sustained high-load"
                )
            )
        ),
        Stage(
            name = "Stage 4 — Built Motor",
            goal = "Beyond stock turbo limits. 500+ WHP, requires built bottom end.",
            items = listOf(
                ModItem(
                    "Upgraded Turbos (Hybrid / Big Single)",
                    "critical",
                    "Stock turbos max around 520 WHP. Upgraded turbos open up massive power potential.",
                    "$2500-5000+",
                    "500-700+ WHP depending on setup"
                ),
                ModItem(
                    "Built Transmission",
                    "critical",
                    "Stock trans/clutch won't hold. 6MT needs built internals, auto needs upgraded clutch packs.",
                    "$2000-4000+",
                    "Required to put down the power"
                ),
                ModItem(
                    "Port Injection Kit",
                    "recommended",
                    "Port injection + direct injection combo for enough fuel flow at high HP. Required for 600+ WHP.",
                    "$1000-2000",
                    "Required at high power levels"
                ),
                ModItem(
                    "Larger FMIC",
                    "recommended",
                    "Big turbo = big heat. Need a serious FMIC to keep charge temps manageable.",
                    "$600-1500",
                    "Required with upgraded turbos"
                ),
                ModItem(
                    "Forged Internals",
                    "critical",
                    "Stock internals hold to ~600 WHP reliably. Beyond that, forged rods and pistons are smart insurance.",
                    "$3000-5000+",
                    "Required to push past stock limits safely"
                ),
                ModItem(
                    "Upgraded HPFP",
                    "critical",
                    "Stock HPFP maxes out with big turbos. Upgraded cam follower and internals required.",
                    "$500-1000",
                    "Required for fueling"
                )
            )
        )
    )
}

@Composable
fun ModGuideScreen() {
    var selectedStageIndex by remember { mutableStateOf(0) }
    val stage = N54ModGuide.STAGES[selectedStageIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Mod Guide", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text(
            "Build your N54 by stage",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        // Stage selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            N54ModGuide.STAGES.forEachIndexed { index, s ->
                FilterChip(
                    selected = selectedStageIndex == index,
                    onClick = { selectedStageIndex = index },
                    label = { Text(s.name.substringBefore(" —"), fontSize = 12.sp) }
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        Text(stage.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(stage.goal, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))

        stage.items.forEach { item -> ModItemCard(item) }
    }
}

@Composable
private fun ModItemCard(item: N54ModGuide.ModItem) {
    val priorityColor = when (item.priority) {
        "critical" -> Color(0xFFEF4444)
        "recommended" -> Color(0xFFF59E0B)
        else -> Color(0xFF6B7280)
    }
    val priorityLabel = when (item.priority) {
        "critical" -> "Must-Have"
        "recommended" -> "Recommended"
        else -> "Optional"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(priorityColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(priorityLabel, fontSize = 10.sp, color = priorityColor, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.width(8.dp))
                Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Cost: ${item.cost}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(2.dp))
            Text("→ ${item.enables}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
