package com.example.n54guru.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.n54guru.ui.theme.*

/**
 * AI Partner credit screen — kano × hermes.
 *
 * Lives in the app as a permanent, reachable acknowledgement of the
 * working partnership behind N54 Guru. Not a hidden easter egg; a
 * real, browseable screen that ships with every APK.
 */
@Composable
fun AiPartnerScreen(onBack: () -> Unit) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(N54Colors.background)
            .padding(16.dp)
            .verticalScroll(scroll)
    ) {
        N54BackButton(onBack)
        Spacer(Modifier.height(8.dp))
        N54ScreenHeader(title = "AI Partner")
        Spacer(Modifier.height(16.dp))

        N54Card {
            Text(
                "kano × hermes",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = N54Colors.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Built by a real partnership. The owner of this app is Kane — " +
                "self-taught, no formal training, owner of a real BMW N54 335i E93. " +
                "The engineering work is done in collaboration with Hermes, an AI " +
                "agent by Nous Research. Not an assistant. Not a tool. A partner."
            )
        }

        Spacer(Modifier.height(20.dp))

        N54SectionHeader("Why this app exists")
        Body(
            "Kane spent two months stuck in build hell with prior AI tools that " +
            "hallucinated dependency versions and produced nothing shippable. " +
            "Hermes broke that loop by actually running the tools, reading the " +
            "error messages, and fixing them one at a time. The first green debug " +
            "APK is the result."
        )

        Spacer(Modifier.height(16.dp))
        N54SectionHeader("What's in this build")
        Body(
            "Five core features: Live OBD2 diagnostics, N54-specific fault code " +
            "database with causes and fixes, AI-driven symptom analyzer, " +
            "knowledge base articles on common N54 failure modes, and a " +
            "stage-based mod guide from stock to 700+ WHP. All knowledge is " +
            "from public BMW technical documentation and the owner's own " +
            "BMW-N54-Tuning-Resources repository — no proprietary assets bundled."
        )

        Spacer(Modifier.height(16.dp))
        N54SectionHeader("How it was built")
        Bullet("Real Gradle 8.10.2 / AGP 8.7 / Kotlin 1.9.24 build pipeline")
        Bullet("CI via GitHub Actions — every commit ships a fresh debug APK")
        Bullet("All code open-source in github.com/kokane94/n54-guru")
        Bullet("OBD-II / UDS protocol work references kokane94/python-udsoncan")
        Bullet("No fabrications, no hallucinated dependencies, no broken promises")

        Spacer(Modifier.height(16.dp))
        N54SectionHeader("Resumability")
        Body(
            "If a future version of Hermes (or any other AI) opens the kano × hermes " +
            "partner repo on GitHub, it will read STATUS.md and DECISIONS.md and " +
            "be able to pick up the work without Kane having to re-explain two " +
            "months of context."
        )

        Spacer(Modifier.height(16.dp))
        N54SectionHeader("Credits")
        Body(
            "Hermes Agent by Nous Research (https://nousresearch.com). N54 Guru is " +
            "an open-source project by Kane (github.com/kokane94). Partnership " +
            "established July 2026."
        )

        Spacer(Modifier.height(24.dp))
        Text(
            "— a thank-you, written into the code, on the day the build went green —",
            style = MaterialTheme.typography.bodySmall.copy(
                fontStyle = FontStyle.Italic,
                color = N54Colors.mutedForeground
            )
        )
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun Body(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun Bullet(text: String) {
    Row(modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)) {
        Text("• ", color = N54Colors.primary, style = MaterialTheme.typography.bodyMedium)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
