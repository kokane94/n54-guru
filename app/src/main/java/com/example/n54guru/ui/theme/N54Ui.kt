package com.example.n54guru.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared UI primitives for the N54 Guru Base44-inspired design system.
 *
 * These keep every screen consistent: dark cards, orange primary, cyan
 * accents, rounded corners, and Inter-style typography.
 */

private val defaultShape = RoundedCornerShape(12.dp)

@Composable
fun N54ScreenHeader(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = N54Colors.primary,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon?.let {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(iconTint.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        subtitle?.let {
            Spacer(Modifier.height(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = N54Colors.mutedForeground
            )
        }
    }
}

@Composable
fun N54BackButton(onBack: () -> Unit) {
    TextButton(onClick = onBack) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = N54Colors.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text("Back", color = N54Colors.primary)
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
        .background(N54Colors.surface, defaultShape)
        .border(1.dp, N54Colors.border, defaultShape)

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = cardModifier,
            shape = defaultShape,
            color = N54Colors.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
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
fun N54SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
    )
}

@Composable
fun N54PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = N54Colors.primary,
            contentColor = N54Colors.background,
            disabledContainerColor = N54Colors.primary.copy(alpha = 0.35f),
            disabledContentColor = N54Colors.mutedForeground
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
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
        modifier = modifier,
        colors = OutlinedButtonDefaults.colors(
            contentColor = N54Colors.primary,
            disabledContentColor = N54Colors.mutedForeground
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(N54Colors.border)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun N54TextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        modifier = modifier,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = N54Colors.surface,
            unfocusedContainerColor = N54Colors.surface,
            disabledContainerColor = N54Colors.surface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedIndicatorColor = N54Colors.primary,
            unfocusedIndicatorColor = N54Colors.border,
            focusedLabelColor = N54Colors.primary,
            unfocusedLabelColor = N54Colors.mutedForeground,
            cursorColor = N54Colors.primary
        ),
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
fun N54FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = N54Colors.surface,
            labelColor = N54Colors.mutedForeground,
            selectedContainerColor = N54Colors.primary.copy(alpha = 0.18f),
            selectedLabelColor = N54Colors.primary,
            selectedLeadingIconColor = N54Colors.primary
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = N54Colors.border,
            selectedBorderColor = N54Colors.primary.copy(alpha = 0.5f),
            enabled = true,
            selected = selected
        )
    )
}

@Composable
fun N54AssistChip(label: String, containerColor: Color = N54Colors.surface) {
    AssistChip(
        onClick = {},
        label = { Text(label, fontSize = 10.sp) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = N54Colors.mutedForeground
        ),
        border = AssistChipDefaults.assistChipBorder(borderColor = N54Colors.border)
    )
}

@Composable
fun N54PriorityBadge(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun N54Bullet(text: String) {
    Row(modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)) {
        Text("• ", color = N54Colors.primary, style = MaterialTheme.typography.bodyMedium)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
