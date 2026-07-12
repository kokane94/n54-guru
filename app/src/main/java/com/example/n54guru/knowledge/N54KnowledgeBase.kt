package com.example.n54guru.knowledge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.n54guru.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeBaseScreen(onDetail: (String) -> Unit) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Fuel", "Boost", "Turbo", "Maintenance", "Cooling", "General")

    val filtered = remember(selectedCategory) {
        ARTICLES.filter { selectedCategory == "All" || it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(N54Colors.background)
            .padding(16.dp)
    ) {
        N54ScreenHeader(title = "Wiki", subtitle = "N54 knowledge base")

        Spacer(Modifier.height(12.dp))

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
            items(filtered) { article ->
                ArticleRow(article, onClick = { onDetail(article.id) })
            }
        }
    }
}

@Composable
private fun ArticleRow(article: Article, onClick: () -> Unit) {
    N54Card(onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(article.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = N54Colors.textPrimary)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    N54LevelBadge(article.level)
                    N54Badge(article.category, N54Colors.textMuted)
                }
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = N54Colors.textMuted, modifier = Modifier.size(22.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(articleId: String, onBack: () -> Unit) {
    val article = ARTICLES.find { it.id == articleId } ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(N54Colors.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        N54BackButton(onBack)
        Spacer(Modifier.height(8.dp))
        N54ScreenHeader(title = article.title, subtitle = article.category, icon = Icons.Filled.MenuBook)
        Spacer(Modifier.height(16.dp))
        N54Card {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                N54LevelBadge(article.level)
                N54Badge(article.category, N54Colors.textMuted)
            }
            Spacer(Modifier.height(12.dp))
            Text(article.body, fontSize = 14.sp, color = N54Colors.textSecondary)
        }
    }
}

data class Article(
    val id: String,
    val title: String,
    val category: String,
    val level: String,
    val body: String
)

val ARTICLES = listOf(
    Article("wastegate", "Wastegate Rattle — What It Is & How to Fix It", "Turbo", "Intermediate", "Wastegate rattle is the clanking sound N54 turbos make when the wastegate flaps wear loose in the turbine housing. It usually happens at idle, on cold start, and during deceleration. It is not immediately dangerous but the flap can eventually break off and damage the turbo.\n\nCommon fixes:\n• Wastegate rattle repair kits (bushings/clips)\n• Replace or rebuild the turbos\n• Upgraded stock-frame or hybrid turbos\n• Big-turbo conversion for permanent fix"),
    Article("walnut", "Walnut Blasting — Why Your N54 Needs It", "Maintenance", "Beginner", "Direct injection means fuel never washes the back of the intake valves. Over time carbon deposits build up, causing rough idle, misfires, and lost power.\n\nWalnut blasting uses crushed walnut shells to clean intake valves without removing the head. Recommended every ~50k miles or when idle quality degrades."),
    Article("hpfp", "HPFP Failure — The Most Common N54 Issue", "Fuel", "Beginner", "The high-pressure fuel pump (HPFP) is a known weak point. Symptoms include long cranks, stumble under load, and fault 29CD (rail pressure too low).\n\nIf rail pressure lags commanded pressure under WOT, the HPFP is failing. Replace with a new or upgraded HPFP."),
    Article("oil", "Best Oil for the N54", "Maintenance", "Beginner", "Use a full synthetic LL-01 / LL-04 approved oil. Popular choices:\n• Liqui Moly 5W-40\n• Castrol Edge 0W-40\n• Mobil 1 5W-40\n\nChange every 5k-7k miles because fuel dilution and turbo heat degrade oil quickly."),
    Article("chargepipe", "Why Your Charge Pipe Will Crack (And How to Fix It)", "Boost", "Beginner", "The stock plastic charge pipe is known to crack or completely blow apart under increased boost. It is the most common first mod.\n\nReplace with an aluminum charge pipe (ARM, VRSF, FTP, BMS). Most kits also include a meth bung or BOV connection."),
    Article("waterpump", "Electric Water Pump Failure — Don't Ignore This", "Cooling", "Beginner", "The N54 uses an electric water pump that commonly fails around 60k-80k miles. Signs include overheating, coolant leaks, and a whining pump.\n\nReplace with an OEM/Pierburg unit and bleed the cooling system thoroughly with the heater on high."),
    Article("n55", "N54 vs N55 — Key Differences", "General", "Beginner", "The N54 has a twin-turbo inline-6 with stronger rods and forged crank. The N55 moved to a single twin-scroll turbo with an open-deck block.\n\nN54 makes more power with bolt-ons and tunes because of twin turbos and stronger internals. N55 is more reliable stock but has a lower ceiling without forged internals."),
    Article("turbos", "Twin Turbo Overview", "Turbo", "Beginner", "N54 uses two small Mitsubishi TD03-based turbos. Quick spool, great response, and easy to push 400+ whp on stock turbos with bolt-ons and a tune."),
    Article("jb4", "JB4 vs MHD vs Bootmod3", "General", "Intermediate", "JB4 is a piggyback that interceptes sensor signals and is easy to remove. MHD is a flash tune via OBD with maps stored on the DME. Bootmod3 is cloud-based flashing with more advanced features.\n\nMost people start with MHD for simplicity; Bootmod3 is better for deep tuning and logging."),
    Article("spark", "Spark Plugs & Gapping", "Maintenance", "Beginner", "One-step colder plugs are needed once tuned. NGK 95770 is the go-to. Gap to 0.020\"-0.022\" for higher boost to prevent blowout."),
    Article("injectors", "Index 12 Injectors Explained", "Fuel", "Intermediate", "Earlier index injectors leak externally and cause misfires. Index 12 is the latest BMW revision and the only one worth installing today."),
    Article("dp", "Downpipes and Emissions", "Boost", "Beginner", "Catless downpipes add power and turbo spool but produce a fuel smell and fail emissions/visual inspection in many regions. Catted downpipes are a middle ground."),
    Article("lpfp", "Low Pressure Fuel Pump", "Fuel", "Intermediate", "The stock LPFP can support ~400 whp. Beyond that, upgrade to a Walbro 450 or Fuel-It stage kit to feed the HPFP and port injection."),
    Article("port", "Port Injection Basics", "Fuel", "Intermediate", "Port injection adds secondary fuel injectors in the intake manifold. It is required for 500+ whp because direct injection alone cannot flow enough fuel."),
    Article("overheating", "Overheating Diagnostics", "Cooling", "Intermediate", "Check coolant level, electric water pump function, thermostat, and fan operation. Common N54 causes are water pump, thermostat, and expansion tank/cap failure.")
)
