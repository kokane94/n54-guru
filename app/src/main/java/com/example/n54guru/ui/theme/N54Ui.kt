@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.n54guru.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val cardShape = RoundedCornerShape(16.dp)
private val chipShape = RoundedCornerShape(20.dp)
private val badgeShape = RoundedCornerShape(8.dp)

@Composable
fun N54ScreenHeader(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = N54Colors.primary,
    action: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon?.let {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(iconTint.copy(alpha = 0.18f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = N54Colors.textPrimary
                )
            }
            action?.invoke()
        }
        subtitle?.let {
            Spacer(Modifier.height(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = N54Colors.textSecondary
            )
        }
    }
}

@Composable
fun N54BackButton(onBack: () -> Unit) {
    TextButton(onClick = onBack) {
        Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = "Back",
            tint = N54Colors.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text("Back", color = N54Colors.primary, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun N54Card(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier
        .fillMaxWidth()
        .background(N54Colors.surface, cardShape)
        .border(1.dp, N54Colors.border, cardShape)

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = cardModifier,
            shape = cardShape,
            color = N54Colors.surface,
            contentColor = N54Colors.textPrimary
        ) {
            Column(Modifier.padding(16.dp), content = content)
        }
    } else {
        Column(
            modifier = cardModifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun N54CompactCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .background(N54Colors.surface, cardShape)
            .border(1.dp, N54Colors.border, cardShape)
            .padding(14.dp),
        content = content
    )
}

@Composable
fun N54SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = N54Colors.textSecondary,
        modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
    )
}

@Composable
fun N54PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(46.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = N54Colors.primary,
            contentColor = N54Colors.onPrimary,
            disabledContainerColor = N54Colors.primary.copy(alpha = 0.35f),
            disabledContentColor = N54Colors.textMuted
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        leadingIcon?.let {
            Icon(it, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun N54OutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(40.dp),
        border = BorderStroke(1.dp, N54Colors.border),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, color = N54Colors.primary)
    }
}

@Composable
fun N54TextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it, color = N54Colors.textMuted) } },
        modifier = modifier,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        leadingIcon = leadingIcon,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedTextColor = N54Colors.textPrimary,
            unfocusedTextColor = N54Colors.textPrimary,
            focusedBorderColor = N54Colors.primary,
            unfocusedBorderColor = N54Colors.border,
            focusedLabelColor = N54Colors.primary,
            unfocusedLabelColor = N54Colors.textMuted,
            cursorColor = N54Colors.primary,
            containerColor = N54Colors.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun N54FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) N54Colors.primary else N54Colors.chipUnselectedBg
    val textColor = if (selected) N54Colors.onPrimary else N54Colors.chipUnselectedText

    Surface(
        onClick = onClick,
        color = bg,
        shape = chipShape,
        modifier = Modifier.height(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun N54StageChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    leadingIcon: ImageVector? = null
) {
    val bg = if (selected) N54Colors.primary else N54Colors.chipUnselectedBg
    val textColor = if (selected) N54Colors.onPrimary else N54Colors.textSecondary

    Surface(
        onClick = onClick,
        color = bg,
        shape = chipShape,
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            leadingIcon?.let {
                Icon(it, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor)
        }
    }
}

@Composable
fun N54Badge(
    label: String,
    color: Color,
    bgColor: Color = color.copy(alpha = 0.18f),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(bgColor, badgeShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun N54LevelBadge(level: String) {
    val color = when (level.lowercase()) {
        "beginner" -> N54Colors.beginner
        "intermediate" -> N54Colors.intermediate
        else -> N54Colors.textSecondary
    }
    val bg = when (level.lowercase()) {
        "beginner" -> N54Colors.beginnerBg
        "intermediate" -> N54Colors.intermediateBg
        else -> N54Colors.chipUnselectedBg
    }
    Box(
        modifier = Modifier
            .background(bg, badgeShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(level, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun N54SeverityBadge(severity: String) {
    val (color, bg) = when (severity.lowercase()) {
        "critical" -> Pair(N54Colors.badgeCritical, N54Colors.badgeCriticalBg)
        "high" -> Pair(N54Colors.badgeHigh, N54Colors.badgeHighBg)
        "medium" -> Pair(N54Colors.badgeMedium, N54Colors.badgeMediumBg)
        "low" -> Pair(N54Colors.badgeLow, N54Colors.badgeLowBg)
        "must-have" -> Pair(N54Colors.badgeCritical, N54Colors.badgeCriticalBg)
        "recommended" -> Pair(N54Colors.badgeMedium, N54Colors.badgeMediumBg)
        else -> Pair(N54Colors.textSecondary, N54Colors.chipUnselectedBg)
    }
    N54Badge(label = severity.uppercase(), color = color, modifier = Modifier.background(bg, badgeShape))
}

@Composable
fun N54Bullet(text: String) {
    Row(modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)) {
        Text("• ", color = N54Colors.primary, style = MaterialTheme.typography.bodyMedium)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = N54Colors.textSecondary)
    }
}

@Composable
fun N56StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    N54CompactCard(modifier = modifier) {
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = N54Colors.primary)
        Text(label, fontSize = 13.sp, color = N54Colors.textSecondary)
    }
}
