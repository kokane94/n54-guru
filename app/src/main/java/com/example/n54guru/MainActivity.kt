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
import com.example.n54guru.models.OBDParameter
import com.example.n54guru.models.ElectricalParameter
import com.example.n54guru.models.OBDLog
import com.example.n54guru.models.DiagnosticAlert
import com.example.n54guru.models.AlertSeverity
import com.example.n54guru.services.OBD2Service
import com.example.n54guru.services.VoiceCommentaryService
import com.example.n54guru.services.PartFinderService
import com.example.n54guru.models.PartSearchResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var obdService: OBD2Service
    private lateinit var voiceService: VoiceCommentaryService
    private val ai = AdvancedDiagnosticAI()
    private lateinit var partFinderService: PartFinderService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        obdService = OBD2Service(this)
        voiceService = VoiceCommentaryService(this)
        partFinderService = PartFinderService()

        setContent {
            N54GuruApp(obdService, voiceService, ai, partFinderService)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        obdService.disconnect()
        voiceService.shutdown()
    }
}

@Composable
fun N54GuruApp(obd: OBD2Service, voice: VoiceCommentaryService, ai: AdvancedDiagnosticAI, partFinder: PartFinderService) {
    var data by remember { mutableStateOf(mapOf<String, OBDParameter>()) }
    var electricalData by remember { mutableStateOf(mapOf<String, ElectricalParameter>()) }
    var alerts by remember { mutableStateOf(listOf<DiagnosticAlert>()) }
    var historicalData by remember { mutableStateOf(listOf<OBDLog>()) }
    var searchQuery by remember { mutableStateOf("") }
    var partSearchResults by remember { mutableStateOf(listOf<PartSearchResult>()) }
    var isConnected by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        obd.connect { success ->
            isConnected = success
            if (success) {
                while (true) {
                    data = obd.readEngineData()
                    electricalData = obd.readElectricalData() // Assuming a new method for electrical data
                    historicalData = obd.getHistoricalData()
                    val engineMap = data.mapValues { it.value }
                    alerts = ai.analyzeAllSystems(engineMap, electricalData, historicalData)
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

                // Part Finder UI
                Spacer(Modifier.height(16.dp))
                Text("Part Finder", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search for parts (e.g., \'N54 HPFP\')") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    // Launch coroutine to perform search
                    obd.scope.launch {
                        partSearchResults = partFinder.searchAliExpress(searchQuery) + partFinder.searchAmazon(searchQuery) + partFinder.searcheBay(searchQuery)
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Search Parts")
                }
                Spacer(Modifier.height(16.dp))

                if (partSearchResults.isNotEmpty()) {
                    Text("Search Results", style = MaterialTheme.typography.titleMedium)
                    partSearchResults.forEach { result ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(result.name, style = MaterialTheme.typography.titleMedium)
                                Text("Price: ${result.price}")
                                Text("Source: ${result.source}")
                                Text("Link: ${result.link}")
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

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
