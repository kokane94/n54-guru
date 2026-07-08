package com.example.n54guru.knowledge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Curated N54 knowledge base, sourced from public BMW technical documentation
 * and the kokane94/BMW-N54-Tuning-Resources repo.
 *
 * This is a real, hand-written N54 reference — not a wrapper around proprietary
 * data. Articles cover the most common failure modes and mods for the N54
 * twin-turbo engine.
 */
object N54KnowledgeBase {

    data class Article(
        val id: String,
        val title: String,
        val category: String,         // fuel, boost, turbo, maintenance, cooling, general
        val difficulty: String,       // beginner, intermediate, advanced
        val summary: String,
        val symptoms: List<String>,
        val causes: List<String>,
        val fixes: List<String>,
        val cost: String,
        val priority: String          // critical, soon, monitor
    )

    val ARTICLES: List<Article> = listOf(
        Article(
            id = "hpfp-failure",
            title = "HPFP Failure",
            category = "fuel",
            difficulty = "beginner",
            summary = "The high-pressure fuel pump (HPFP) is the N54's most notorious failure point. BMW issued multiple recalls and revised the pump several times.",
            symptoms = listOf(
                "Long cranks / hard starting",
                "Engine stalling at idle",
                "Loss of power under acceleration",
                "Fault code 29CD (fuel rail pressure too low)"
            ),
            causes = listOf(
                "Cam follower wear",
                "Pump internals failure under high pressure demand",
                "Inadequate LPFP supply to the HPFP"
            ),
            fixes = listOf(
                "Replace with latest revision HPFP",
                "If out of warranty, expect $500-800 for parts + labor",
                "Consider aftermarket LPFP upgrade to feed the HPFP"
            ),
            cost = "$500-800",
            priority = "critical"
        ),
        Article(
            id = "charge-pipe",
            title = "Charge Pipe Cracking",
            category = "boost",
            difficulty = "beginner",
            summary = "The stock N54 charge pipe is made of plastic and connects the intercooler to the throttle body. Under boost, especially with a tune, it WILL eventually crack or blow off.",
            symptoms = listOf(
                "Sudden loss of boost",
                "Whistling / hissing sound under acceleration",
                "Fault code 2A99 (charge pressure too low)",
                "Check engine light"
            ),
            causes = listOf(
                "Plastic OEM construction cannot hold boost under tune",
                "Boost spikes crack the brittle plastic"
            ),
            fixes = listOf(
                "Replace with aluminum charge pipe ($100-180)",
                "Popular brands: ARM Motorsports, BMS, VRSF",
                "Takes about 30 minutes to install",
                "Do this BEFORE tuning — it's a reliability mod"
            ),
            cost = "$100-180",
            priority = "critical"
        ),
        Article(
            id = "walnut-blast",
            title = "Walnut Blasting (Intake Valve Cleaning)",
            category = "maintenance",
            difficulty = "intermediate",
            summary = "Direct injection engines like the N54 don't have fuel washing over the intake valves, so carbon deposits build up over time. This causes rough idle, misfires, and power loss.",
            symptoms = listOf(
                "Rough idle",
                "Misfires",
                "Power loss"
            ),
            causes = listOf(
                "Direct injection — no fuel over intake valves",
                "PCV oil vapors deposit on hot intake valves"
            ),
            fixes = listOf(
                "Every 50,000 miles as preventive maintenance",
                "If experiencing rough idle or misfires",
                "Before any major tune or power mods",
                "Install an oil catch can afterward to slow carbon buildup"
            ),
            cost = "$300-500 at a shop, $100-150 DIY",
            priority = "soon"
        ),
        Article(
            id = "wastegate-rattle",
            title = "Wastegate Rattle",
            category = "turbo",
            difficulty = "intermediate",
            summary = "The N54's twin turbos are known for developing a wastegate rattle, typically heard as a metallic rattling at idle or low RPM. This is caused by wear on the wastegate actuator arm and flapper valve.",
            symptoms = listOf(
                "Metallic rattle at idle, especially when cold",
                "Rattle goes away under boost",
                "May throw 30BA/30BB codes"
            ),
            causes = listOf(
                "Wear on wastegate actuator arm",
                "Flapper valve play increases with mileage"
            ),
            fixes = listOf(
                "OEM Wastegate Replacement — BMW updated the design, new actuators reduce play",
                "Aftermarket Wastegate Upgrade — Vargas Turbo offers tighter tolerances",
                "Turbo Replacement — if turbos have high miles and shaft play, full replacement may be more cost-effective"
            ),
            cost = "$500-2000",
            priority = "monitor"
        ),
        Article(
            id = "water-pump",
            title = "Electric Water Pump Failure",
            category = "cooling",
            difficulty = "beginner",
            summary = "The N54 uses an electric water pump that is known to fail, usually between 60,000-100,000 miles. When it fails, the engine can overheat quickly.",
            symptoms = listOf(
                "Fault code 2C5B",
                "Engine temperature rising above normal",
                "Coolant warning light",
                "Reduced engine power message"
            ),
            causes = listOf(
                "Electric pump motor failure",
                "Thermostat stuck closed"
            ),
            fixes = listOf(
                "Replace proactively at 60,000-80,000 miles",
                "Replace thermostat at the same time (they often fail together)",
                "Use BMW OEM or Pierburg (OE supplier) pump"
            ),
            cost = "$200-400 for pump + thermostat, ~2 hours labor",
            priority = "critical"
        ),
        Article(
            id = "oil-spec",
            title = "Oil Specification (LL-01)",
            category = "maintenance",
            difficulty = "beginner",
            summary = "The N54 requires BMW Longlife-01 (LL-01) approved oil. Using non-approved oil can cause issues with the VANOS system and turbo longevity.",
            symptoms = listOf(
                "VANOS solenoid clogging",
                "Turbo bearing wear"
            ),
            causes = listOf(
                "Non-LL-01 oil lacks required additive package"
            ),
            fixes = listOf(
                "Liqui Moly Leichtlauf High Tech 5W-40 — Most popular enthusiast choice",
                "Castrol Edge 0W-40 — BMW dealer fill",
                "Mobil 1 0W-40 — Widely available, LL-01 approved",
                "Shell Rotella T6 5W-40 — Budget-friendly, great protection"
            ),
            cost = "~6.9 quarts capacity",
            priority = "soon"
        ),
        Article(
            id = "n54-vs-n55",
            title = "N54 vs N55",
            category = "general",
            difficulty = "beginner",
            summary = "The N54 (2007-2010) was replaced by the N55 (2011+). The N54 is the better platform for modifications. The N55 is more reliable as a daily driver.",
            symptoms = listOf(
                "Curiosity about which generation you have"
            ),
            causes = listOf(
                "N54: twin turbo, more power potential",
                "N55: single turbo, more reliable, better fuel economy"
            ),
            fixes = listOf(
                "N54: 700+ WHP achievable with supporting mods",
                "N55: ~500 WHP max on stock turbo"
            ),
            cost = "N/A",
            priority = "monitor"
        )
    )

    val CATEGORIES: List<String> = listOf("all", "fuel", "boost", "turbo", "maintenance", "cooling", "general")

    fun articlesFor(category: String): List<Article> =
        if (category == "all") ARTICLES else ARTICLES.filter { it.category == category }

    fun searchArticles(query: String): List<Article> {
        if (query.isBlank()) return ARTICLES
        val q = query.lowercase()
        return ARTICLES.filter {
            it.title.lowercase().contains(q) ||
            it.summary.lowercase().contains(q) ||
            it.symptoms.any { s -> s.lowercase().contains(q) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeBaseScreen(onArticleClick: (String) -> Unit) {
    var selectedCategory by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }

    val articles = remember(selectedCategory, searchQuery) {
        val byCategory = N54KnowledgeBase.articlesFor(selectedCategory)
        if (searchQuery.isBlank()) byCategory
        else byCategory.filter { a ->
            a.title.lowercase().contains(searchQuery.lowercase()) ||
            a.summary.lowercase().contains(searchQuery.lowercase())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("Knowledge Base", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text(
            "N54 deep dives, how-tos, and reference",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search articles...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        // Category chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            N54KnowledgeBase.CATEGORIES.forEach { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = {
                        Text(
                            if (cat == "all") "All" else cat.replaceFirstChar { it.uppercase() },
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(articles, key = { it.id }) { article ->
                ArticleCard(article = article, onClick = { onArticleClick(article.id) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleCard(article: N54KnowledgeBase.Article, onClick: () -> Unit) {
    val priorityColor = when (article.priority) {
        "critical" -> Color(0xFFEF4444)
        "soon" -> Color(0xFFF59E0B)
        else -> Color(0xFF6B7280)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(priorityColor, RoundedCornerShape(4.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                article.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text(article.category, fontSize = 10.sp) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(article.difficulty, fontSize = 10.sp) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(article.cost, fontSize = 10.sp) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(articleId: String, onBack: () -> Unit) {
    val article = N54KnowledgeBase.ARTICLES.find { it.id == articleId }
    if (article == null) {
        Text("Article not found")
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
        Text(article.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text(article.category, fontSize = 10.sp) })
            AssistChip(onClick = {}, label = { Text(article.difficulty, fontSize = 10.sp) })
            AssistChip(onClick = {}, label = { Text(article.priority, fontSize = 10.sp) })
        }
        Spacer(Modifier.height(16.dp))
        Text(article.summary, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))

        DetailSection("Symptoms", article.symptoms)
        DetailSection("Causes", article.causes)
        DetailSection("Fixes", article.fixes)

        Spacer(Modifier.height(16.dp))
        Text("Estimated cost: ${article.cost}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DetailSection(title: String, items: List<String>) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 4.dp))
    items.forEach { item ->
        Row(modifier = Modifier.padding(vertical = 2.dp)) {
            Text("• ", style = MaterialTheme.typography.bodyMedium)
            Text(item, style = MaterialTheme.typography.bodyMedium)
        }
    }
    Spacer(Modifier.height(12.dp))
}
