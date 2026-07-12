package com.example.n54guru

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.n54guru.knowledge.*
import com.example.n54guru.protocol.N54DmeLiveDataSource
import com.example.n54guru.ui.AiPartnerScreen
import com.example.n54guru.ui.theme.N54Colors
import com.example.n54guru.ui.theme.N54GuruTheme

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Codes : Screen("codes", "Codes", Icons.Filled.Speed)
    object Mods : Screen("mods", "Mods", Icons.Filled.Build)
    object Service : Screen("service", "Service", Icons.Filled.DateRange)
    object Wiki : Screen("wiki", "Wiki", Icons.Filled.MenuBook)
    object Diagnose : Screen("diagnose", "AI Diag", Icons.Filled.AutoAwesome)
    object Live : Screen("live", "Live", Icons.Filled.Speed)
    object Logs : Screen("logs", "Logs", Icons.Filled.List)
    object Partner : Screen("partner", "Partner", Icons.Filled.Info)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun N54GuruApp(source: N54DmeLiveDataSource) {
    var currentRoute by remember { mutableStateOf(Screen.Home.route) }
    var faultCodeDetail by remember { mutableStateOf<String?>(null) }
    var articleDetail by remember { mutableStateOf<String?>(null) }
    var modDetail by remember { mutableStateOf<String?>(null) }
    var serviceDetail by remember { mutableStateOf<String?>(null) }

    val tabs = listOf(Screen.Home, Screen.Codes, Screen.Mods, Screen.Service, Screen.Wiki, Screen.Diagnose)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = N54Colors.surface,
                contentColor = N54Colors.textSecondary,
                tonalElevation = 0.dp
            ) {
                tabs.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            currentRoute = screen.route
                            faultCodeDetail = null
                            articleDetail = null
                            modDetail = null
                            serviceDetail = null
                        },
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.title,
                                tint = if (currentRoute == screen.route) N54Colors.primary else N54Colors.textMuted
                            )
                        },
                        label = {
                            Text(
                                screen.title,
                                fontSize = 10.sp,
                                color = if (currentRoute == screen.route) N54Colors.primary else N54Colors.textMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = N54Colors.primary,
                            selectedTextColor = N54Colors.primary,
                            indicatorColor = N54Colors.primarySoft,
                            unselectedIconColor = N54Colors.textMuted,
                            unselectedTextColor = N54Colors.textMuted
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(N54Colors.background)
                .padding(padding)
        ) {
            when {
                currentRoute == Screen.Partner.route -> AiPartnerScreen(onBack = { currentRoute = Screen.Home.route })
                faultCodeDetail != null -> FaultCodeDetailScreen(code = faultCodeDetail!!, onBack = { faultCodeDetail = null })
                articleDetail != null -> ArticleDetailScreen(articleId = articleDetail!!, onBack = { articleDetail = null })
                modDetail != null -> ModDetailScreen(modId = modDetail!!, onBack = { modDetail = null })
                serviceDetail != null -> ServiceDetailScreen(serviceId = serviceDetail!!, onBack = { serviceDetail = null })
                currentRoute == Screen.Home.route -> HomeScreen(
                    source = source,
                    onNavigate = { currentRoute = it },
                    onShowPartner = { currentRoute = Screen.Partner.route },
                    onLiveData = { currentRoute = Screen.Live.route },
                    onLogs = { currentRoute = Screen.Logs.route }
                )
                currentRoute == Screen.Codes.route -> FaultCodesScreen(onDetail = { faultCodeDetail = it })
                currentRoute == Screen.Mods.route -> ModGuideScreen(onDetail = { modDetail = it })
                currentRoute == Screen.Service.route -> MaintenanceScreen(onDetail = { serviceDetail = it })
                currentRoute == Screen.Wiki.route -> KnowledgeBaseScreen(onDetail = { articleDetail = it })
                currentRoute == Screen.Diagnose.route -> AIDiagnosticsScreen()
                currentRoute == Screen.Live.route -> LiveDataScreen(source = source, onShowPartner = { currentRoute = Screen.Partner.route })
                currentRoute == Screen.Logs.route -> LogDashboardScreen(source = source)
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    private val source by lazy { N54DmeLiveDataSource(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            N54GuruTheme {
                N54GuruApp(source)
            }
        }
    }
}
