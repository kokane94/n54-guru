package com.example.n54guru.knowledge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.n54guru.MainActivity
import com.example.n54guru.Screen
import com.example.n54guru.protocol.N54DmeLiveDataSource
import com.example.n54guru.ui.theme.*

@Composable
fun HomeScreen(
    source: N54DmeLiveDataSource,
    onNavigate: (String) -> Unit,
    onShowPartner: () -> Unit,
    onLiveData: () -> Unit,
    onLogs: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(N54Colors.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Title
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "N54",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = N54Colors.textPrimary,
                letterSpacing = (-1).sp
            )
            Text(
                text = "Guru",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = N54Colors.primary,
                letterSpacing = (-1).sp
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Your complete garage companion — diagnostics, mods, maintenance, and knowledge for the legendary N54 engine.",
            style = MaterialTheme.typography.bodyMedium,
            color = N54Colors.textSecondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        // Menu cards
        HomeMenuCard(
            title = "Fault Codes",
            subtitle = "Look up N54 fault codes with causes & fixes",
            icon = Icons.Filled.Speed,
            iconBg = Color(0xFF2D1F1F),
            onClick = { onNavigate(Screen.Codes.route) }
        )
        Spacer(Modifier.height(12.dp))
        HomeMenuCard(
            title = "Mod Guide",
            subtitle = "Builds by stage — Stock to 700+ WHP",
            icon = Icons.Filled.Build,
            iconBg = Color(0xFF2D251F),
            onClick = { onNavigate(Screen.Mods.route) }
        )
        Spacer(Modifier.height(12.dp))
        HomeMenuCard(
            title = "Maintenance",
            subtitle = "Track services & stay on schedule",
            icon = Icons.Filled.DateRange,
            iconBg = Color(0xFF1F2D2A),
            onClick = { onNavigate(Screen.Service.route) }
        )
        Spacer(Modifier.height(12.dp))
        HomeMenuCard(
            title = "Knowledge Base",
            subtitle = "How-tos, tips, and deep dives",
            icon = Icons.Filled.MenuBook,
            iconBg = Color(0xFF1F2D2A),
            onClick = { onNavigate(Screen.Wiki.route) }
        )
        Spacer(Modifier.height(12.dp))
        HomeMenuCard(
            title = "AI Diagnostics",
            subtitle = "Describe symptoms, get likely causes",
            icon = Icons.Filled.AutoAwesome,
            iconBg = Color(0xFF25203A),
            onClick = { onNavigate(Screen.Diagnose.route) }
        )

        Spacer(Modifier.height(20.dp))

        // Stats
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            N56StatCard(value = "18+", label = "Fault Codes", modifier = Modifier.weight(1f))
            N56StatCard(value = "4", label = "Build Stages", modifier = Modifier.weight(1f))
            N56StatCard(value = "7+", label = "Guides", modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun HomeMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    onClick: () -> Unit
) {
    N54Card(onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(iconBg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = N54Colors.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = N54Colors.textPrimary)
                Text(subtitle, fontSize = 13.sp, color = N54Colors.textSecondary)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = N54Colors.textMuted, modifier = Modifier.size(22.dp))
        }
    }
}
