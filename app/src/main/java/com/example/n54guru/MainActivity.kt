package com.example.n54guru

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.n54guru.knowledge.AIDiagnosticsScreen
import com.example.n54guru.knowledge.ArticleDetailScreen
import com.example.n54guru.knowledge.FaultCodeDetailScreen
import com.example.n54guru.knowledge.FaultCodesScreen
import com.example.n54guru.knowledge.KnowledgeBaseScreen
import com.example.n54guru.knowledge.LiveDataScreen
import com.example.n54guru.knowledge.LogDashboardScreen
import com.example.n54guru.knowledge.ModGuideScreen
import com.example.n54guru.protocol.N54DmeLiveDataSource
import com.example.n54guru.ui.AiPartnerScreen
import com.example.n54guru.ui.theme.N54GuruTheme

import com.example.n54guru.ui.theme.N54Colors

/**
 * Top-level navigation for the N54 Guru app.
 *
 * Six core tabs (Live / Codes / AI / Logs / Knowledge / Mods) plus
 * the AI Partner credit screen and detail screens for each knowledge
 * base item.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Live : Screen("live", "Live", Icons.Filled.Speed)
    object Codes : Screen("codes", "Codes", Icons.Filled.Warning)
    object Diagnose : Screen("diagnose", "AI", Icons.Filled.AutoAwesome)
    object Logs : Screen("logs", "Logs", Icons.Filled.List)
    object Knowledge : Screen("knowledge", "Knowledge", Icons.Filled.LibraryBooks)
    object Mods : Screen("mods", "Mods", Icons.Filled.Build)
    object Partner : Screen("partner", "AI Partner", Icons.Filled.Info)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun N54GuruApp(source: N54DmeLiveDataSource) {
    var currentRoute by remember { mutableStateOf(Screen.Live.route) }
    var faultCodeDetail by remember { mutableStateOf<String?>(null) }
    var articleDetail by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("N54 Guru") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = N54Colors.surface,
                    titleContentColor = N54Colors.primary,
                    navigationIconContentColor = N54Colors.primary,
                    actionIconContentColor = N54Colors.primary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = N54Colors.surface,
                contentColor = N54Colors.primary,
                tonalElevation = 0.dp
            ) {
                val tabs = listOf(
                    Screen.Live,
                    Screen.Codes,
                    Screen.Diagnose,
                    Screen.Logs,
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
                        label = { Text(screen.title, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = N54Colors.background,
                            selectedTextColor = N54Colors.primary,
                            indicatorColor = N54Colors.primary,
                            unselectedIconColor = N54Colors.mutedForeground,
                            unselectedTextColor = N54Colors.mutedForeground
                        )
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
                    LiveDataScreen(source = source, onShowPartner = { currentRoute = Screen.Partner.route })
                }
                currentRoute == Screen.Codes.route -> {
                    FaultCodesScreen(onCodeClick = { faultCodeDetail = it })
                }
                currentRoute == Screen.Diagnose.route -> {
                    AIDiagnosticsScreen()
                }
                currentRoute == Screen.Logs.route -> {
                    LogDashboardScreen(source = source)
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

class MainActivity : ComponentActivity() {

    private lateinit var source: N54DmeLiveDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        source = N54DmeLiveDataSource(this)

        setContent {
            N54GuruTheme {
                N54GuruApp(source)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        source.disconnect()
    }
}
