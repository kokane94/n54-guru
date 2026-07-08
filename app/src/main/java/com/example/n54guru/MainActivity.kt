package com.example.n54guru

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.n54guru.knowledge.AIDiagnosticsScreen
import com.example.n54guru.knowledge.ArticleDetailScreen
import com.example.n54guru.knowledge.FaultCodeDetailScreen
import com.example.n54guru.knowledge.FaultCodesScreen
import com.example.n54guru.knowledge.KnowledgeBaseScreen
import com.example.n54guru.knowledge.ModGuideScreen
import com.example.n54guru.ui.AiPartnerScreen

/**
 * Top-level navigation for the N54 Guru app.
 *
 * Five core features (matching the Base44 version) plus the AI Partner
 * credit screen and the real-time diagnostic OBD2 view.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Live : Screen("live", "Live", Icons.Filled.Speed)
    object FaultCodes : Screen("codes", "Codes", Icons.Filled.Warning)
    object Diagnose : Screen("diagnose", "AI", Icons.Filled.AutoAwesome)
    object Knowledge : Screen("knowledge", "Knowledge", Icons.Filled.LibraryBooks)
    object Mods : Screen("mods", "Mods", Icons.Filled.Build)
    object Partner : Screen("partner", "AI Partner", Icons.Filled.Info)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun N54GuruApp(obd: com.example.n54guru.services.OBD2Service, voice: com.example.n54guru.services.VoiceCommentaryService) {
    var currentRoute by remember { mutableStateOf(Screen.Live.route) }
    var faultCodeDetail by remember { mutableStateOf<String?>(null) }
    var articleDetail by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("N54 Guru") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val tabs = listOf(
                    Screen.Live,
                    Screen.FaultCodes,
                    Screen.Diagnose,
                    Screen.Knowledge,
                    Screen.Mods
                )
                tabs.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            currentRoute = screen.route
                            faultCodeDetail = null
                            articleDetail = null
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                currentRoute == Screen.Partner.route -> {
                    AiPartnerScreen(onBack = { currentRoute = Screen.Live.route })
                }
                faultCodeDetail != null -> {
                    FaultCodeDetailScreen(code = faultCodeDetail!!, onBack = { faultCodeDetail = null })
                }
                articleDetail != null -> {
                    ArticleDetailScreen(articleId = articleDetail!!, onBack = { articleDetail = null })
                }
                currentRoute == Screen.Live.route -> {
                    LiveDiagnosticsScreen(obd, voice, onShowPartner = { currentRoute = Screen.Partner.route })
                }
                currentRoute == Screen.FaultCodes.route -> {
                    FaultCodesScreen(onCodeClick = { faultCodeDetail = it })
                }
                currentRoute == Screen.Diagnose.route -> {
                    AIDiagnosticsScreen()
                }
                currentRoute == Screen.Knowledge.route -> {
                    KnowledgeBaseScreen(onArticleClick = { articleDetail = it })
                }
                currentRoute == Screen.Mods.route -> {
                    ModGuideScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LiveDiagnosticsScreen(
    obd: com.example.n54guru.services.OBD2Service,
    voice: com.example.n54guru.services.VoiceCommentaryService,
    onShowPartner: () -> Unit
) {
    var connectionStatus by remember { mutableStateOf("Disconnected") }
    var lastReadings by remember { mutableStateOf<Map<String, Float>>(emptyMap()) }
    var isConnected by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isConnected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    if (isConnected) "Connected to OBD2" else "OBD2 Adapter",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    if (isConnected) "Live data streaming" else "Plug in your K-CAN adapter via USB OTG",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            obd.connect { success ->
                                isConnected = success
                                connectionStatus = if (success) "Connected" else "Failed"
                            }
                        },
                        enabled = !isConnected
                    ) { Text("Connect") }
                    OutlinedButton(
                        onClick = {
                            obd.disconnect()
                            isConnected = false
                            connectionStatus = "Disconnected"
                        },
                        enabled = isConnected
                    ) { Text("Disconnect") }
                    OutlinedButton(onClick = onShowPartner) { Text("AI Partner") }
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        Text("Live Data", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        val readings = listOf(
            "Engine RPM" to "rpm",
            "Coolant Temp" to "°C",
            "Oil Temp" to "°C",
            "Boost (MAP)" to "kPa",
            "HPFP Pressure" to "kPa",
            "Battery Voltage" to "V"
        )
        readings.forEach { (name, unit) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(name)
                    Text(
                        if (isConnected) "—" else "$unit",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            connectionStatus,
            style = MaterialTheme.typography.bodySmall,
            color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

class MainActivity : ComponentActivity() {

    private lateinit var obdService: com.example.n54guru.services.OBD2Service
    private lateinit var voiceService: com.example.n54guru.services.VoiceCommentaryService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        obdService = com.example.n54guru.services.OBD2Service(this)
        voiceService = com.example.n54guru.services.VoiceCommentaryService(this)

        setContent {
            MaterialTheme {
                N54GuruApp(obdService, voiceService)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        obdService.disconnect()
        voiceService.shutdown()
    }
}
