package com.example.n54guru.knowledge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * AI Diagnostics — describe symptoms, get N54-specific likely causes.
 *
 * The full Base44 version sent the symptoms to a remote LLM with a structured
 * JSON schema. For the offline Android build, we implement a deterministic
 * keyword-based matcher against the same knowledge base the Base44 version
 * was trained on. Same intent, same output structure, works without network.
 *
 * To enable the real LLM backend, set [AIDiagnosticsViewModel.useRemoteLLM]
 * = true and provide an OpenAI-compatible endpoint.
 */
object SymptomAnalyzer {

    data class DiagnosisResult(
        val likelyCauses: List<LikelyCause>,
        val recommendedActions: List<String>,
        val estimatedCost: String,
        val urgency: String,        // critical, soon, monitor
        val additionalNotes: String
    )

    data class LikelyCause(
        val cause: String,
        val probability: String,    // high, medium, low
        val explanation: String
    )

    // Symptom -> candidate fault codes / knowledge articles
    private val SYMPTOM_MAP: List<Triple<List<String>, String, String>> = listOf(
        Triple(listOf("long crank", "hard start", "won't start", "no start"), "hpfp-failure", "critical"),
        Triple(listOf("loss of power", "reduced power", "no power", "limp mode", "boost cut"), "charge-pipe", "critical"),
        Triple(listOf("boost leak", "hissing", "whistling"), "charge-pipe", "critical"),
        Triple(listOf("stalling", "stall", "dies at idle"), "hpfp-failure", "critical"),
        Triple(listOf("overheat", "overheating", "temperature rising", "hot", "coolant light"), "water-pump", "critical"),
        Triple(listOf("rough idle", "shaky idle", "unstable idle"), "walnut-blast", "soon"),
        Triple(listOf("misfire", "misfiring", "missing"), "walnut-blast", "soon"),
        Triple(listOf("rattle", "rattling", "metallic noise", "noise at idle"), "wastegate-rattle", "soon"),
        Triple(listOf("check engine", "cel", "engine light", "service engine soon"), "charge-pipe", "soon"),
        Triple(listOf("smoke", "blue smoke", "white smoke", "black smoke"), "hpfp-failure", "critical"),
        Triple(listOf("fuel smell", "petrol smell", "gas smell"), "hpfp-failure", "critical"),
        Triple(listOf("wastegate rattle"), "wastegate-rattle", "monitor"),
        Triple(listOf("vanos", "vanos rattle", "cold start rattle"), "hpfp-failure", "soon")
    )

    fun analyze(symptoms: String): DiagnosisResult {
        if (symptoms.isBlank()) {
            return DiagnosisResult(
                likelyCauses = emptyList(),
                recommendedActions = listOf("Describe your symptoms in the text box above and I'll analyze them against the N54 knowledge base."),
                estimatedCost = "TBD",
                urgency = "monitor",
                additionalNotes = "Tip: include when it happens (cold start, under boost, at idle), any sounds, and any warning lights."
            )
        }

        val symptomsLower = symptoms.lowercase()
        val matched = mutableMapOf<String, Int>()  // articleId -> match count

        SYMPTOM_MAP.forEach { (keywords, articleId, _) ->
            val hits = keywords.count { kw -> symptomsLower.contains(kw) }
            if (hits > 0) matched[articleId] = (matched[articleId] ?: 0) + hits
        }

        val ranked = matched.entries.sortedByDescending { it.value }
        val likelyCauses = ranked.mapNotNull { (articleId, hits) ->
            val article = N54KnowledgeBase.ARTICLES.find { it.id == articleId } ?: return@mapNotNull null
            val probability = when {
                hits >= 3 -> "high"
                hits == 2 -> "medium"
                else -> "low"
            }
            LikelyCause(
                cause = article.title,
                probability = probability,
                explanation = article.summary
            )
        }

        val urgency = when {
            likelyCauses.isEmpty() -> "monitor"
            likelyCauses.any { it.probability == "high" } -> "critical"
            likelyCauses.any { it.probability == "medium" } -> "soon"
            else -> "monitor"
        }

        val estimatedCost = when (urgency) {
            "critical" -> "Varies — likely $200-1500"
            "soon" -> "Varies — likely $100-500"
            else -> "TBD"
        }

        val recommendedActions = ranked.mapNotNull { (articleId, _) ->
            N54KnowledgeBase.ARTICLES.find { it.id == articleId }?.fixes?.firstOrNull()
        }.take(4).ifEmpty {
            listOf("Run a full OBD2 scan to read stored DTCs", "Check for boost leaks", "Verify fuel pressure under load")
        }

        val additionalNotes = buildString {
            append("Analysis is based on keyword matching against the N54 knowledge base. ")
            if (likelyCauses.isNotEmpty()) {
                append("For a definitive diagnosis, plug in your K-CAN adapter and read the live DTCs from the DME.")
            } else {
                append("Try to be more specific about when it happens and what you hear/see.")
            }
        }

        return DiagnosisResult(
            likelyCauses = likelyCauses,
            recommendedActions = recommendedActions,
            estimatedCost = estimatedCost,
            urgency = urgency,
            additionalNotes = additionalNotes
        )
    }
}

@Composable
fun AIDiagnosticsScreen() {
    var symptoms by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<SymptomAnalyzer.DiagnosisResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF8B5CF6).copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text("AI Diagnostics", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        }
        Text(
            "Describe your symptoms — get N54-specific diagnosis",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = symptoms,
            onValueChange = { symptoms = it },
            label = { Text("What's happening with your N54?") },
            placeholder = { Text("e.g. long crank when cold, occasional stalling at idle, loss of power under boost...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        )
        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { result = SymptomAnalyzer.analyze(symptoms) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Analyze Symptoms")
        }
        Spacer(Modifier.height(20.dp))

        result?.let { DiagnosisResultCard(it) }
    }
}

@Composable
private fun DiagnosisResultCard(result: SymptomAnalyzer.DiagnosisResult) {
    val urgencyColor = when (result.urgency) {
        "critical" -> Color(0xFFEF4444)
        "soon" -> Color(0xFFF59E0B)
        else -> Color(0xFF6B7280)
    }
    val urgencyLabel = when (result.urgency) {
        "critical" -> "Fix Immediately"
        "soon" -> "Fix Soon"
        else -> "Monitor"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(urgencyColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(urgencyLabel, fontSize = 11.sp, color = urgencyColor, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))

            if (result.likelyCauses.isNotEmpty()) {
                Text("Likely Causes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                result.likelyCauses.forEach { cause ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(cause.cause, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        when (cause.probability) {
                                            "high" -> Color(0xFFEF4444)
                                            "medium" -> Color(0xFFF59E0B)
                                            else -> Color(0xFF6B7280)
                                        }.copy(alpha = 0.2f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    cause.probability.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(cause.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (result.recommendedActions.isNotEmpty()) {
                Text("Recommended Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                result.recommendedActions.forEach { action ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("• ", style = MaterialTheme.typography.bodyMedium)
                        Text(action, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Text("Estimated cost: ${result.estimatedCost}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                result.additionalNotes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
