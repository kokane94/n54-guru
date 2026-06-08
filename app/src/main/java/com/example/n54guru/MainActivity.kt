package com.example.n54guru

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.n54guru.ai.AdvancedDiagnosticAI
import com.example.n54guru.models.*
import com.example.n54guru.services.OBD2Service
import com.example.n54guru.services.VoiceCommentaryService
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private lateinit var obdService: OBD2Service
    private lateinit var voiceService: VoiceCommentaryService
    private val ai = AdvancedDiagnosticAI()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        obdService = OBD2Service(this)
        voiceService = VoiceCommentaryService(this)

        setContent {
            N54GuruApp(obdService, voiceService, ai)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        obdService.disconnect()
        voiceService.shutdown()
    }
}

@Composable
fun N54GuruApp(obd: OBD2Service, voice: VoiceCommentaryService, ai: AdvancedDiagnosticAI) {
    var data by remember { mutableStateOf(mapOf<String, OBDParameter>()) }
    var alerts by remember { mutableStateOf(listOf<DiagnosticAlert>()) }
    var isConnected by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        obd.connect { success ->
            isConnected = success
            if (success) {
                while (true) {
                    data = obd.readEngineData()
                    val engineMap = data.mapValues { it.value }
                    alerts = ai.analyzeAllSystems(engineMap, emptyMap(), emptyList())
                    alerts.firstOrNull()?.let { voice.speakAlert(it) }
                    delay(2500)
                }
            }
        }
    }

    MaterialTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("N54 Guru - BMW 335i") }) }) { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                Text("Real-time N54 Diagnostics", style = MaterialTheme.typography.headlineMedium)

                Spacer(Modifier.height(16.dp))

                if (!isConnected) {
                    Text("Connect your OBD2 adapter via USB OTG", color = MaterialTheme.colorScheme.error)
                }

                data.forEach { (_, param) ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("${param.unit}: ${"%.1f".format(param.value)}", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (alerts.isNotEmpty()) {
                    Text("AI Alerts", style = MaterialTheme.typography.titleMedium)
                    alerts.forEach { alert ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (alert.severity == AlertSeverity.CRITICAL) 
                                    MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(alert.component, style = MaterialTheme.typography.titleMedium)
                                Text(alert.reason)
                                Text("→ ${alert.recommendation}")
                            }
                        }
                    }
                }
            }
        }
    }
}
