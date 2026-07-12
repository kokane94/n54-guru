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
import com.example.n54guru.ui.theme.*

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIDiagnosticsScreen() {
    var symptoms by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<SymptomAnalyzer.DiagnosisResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(N54Colors.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        N54ScreenHeader(
            title = "AI Diagnostics",
            subtitle = "Describe your symptoms — get N54-specific diagnosis",
            icon = Icons.Filled.AutoAwesome,
            iconTint = N54Colors.violet
        )
        Spacer(Modifier.height(16.dp))

        N54TextField(
            value = symptoms,
            onValueChange = { symptoms = it },
            label = "What's happening with your N54?",
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            singleLine = false,
            maxLines = 6,
            minLines = 5
        )
        Spacer(Modifier.height(8.dp))

        N54PrimaryButton(
            text = "Analyze Symptoms",
            onClick = { result = SymptomAnalyzer.analyze(symptoms) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))

        result?.let { DiagnosisResultCard(it) }
    }
}

@Composable
private fun DiagnosisResultCard(result: SymptomAnalyzer.DiagnosisResult) {
    val urgencyColor = when (result.urgency) {
        "critical" -> N54Colors.destructive
        "soon" -> N54Colors.yellow
        else -> N54Colors.mutedForeground
    }
    val urgencyLabel = when (result.urgency) {
        "critical" -> "Fix Immediately"
        "soon" -> "Fix Soon"
        else -> "Monitor"
    }

    N54Card {
        N54PriorityBadge(urgencyLabel, urgencyColor)
        Spacer(Modifier.height(12.dp))

        if (result.likelyCauses.isNotEmpty()) {
            N54SectionHeader("Likely Causes")
            result.likelyCauses.forEach { cause ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            cause.cause,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(8.dp))
                        val probColor = when (cause.probability) {
                            "high" -> N54Colors.destructive
                            "medium" -> N54Colors.yellow
                            else -> N54Colors.mutedForeground
                        }
                        N54PriorityBadge(cause.probability.uppercase(), probColor)
                    }
                    Text(
                        cause.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = N54Colors.mutedForeground
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        if (result.recommendedActions.isNotEmpty()) {
            N54SectionHeader("Recommended Actions")
            result.recommendedActions.forEach { N54Bullet(it) }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "Estimated cost: ${result.estimatedCost}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = N54Colors.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            result.additionalNotes,
            style = MaterialTheme.typography.bodySmall,
            color = N54Colors.mutedForeground
        )
    }
}
