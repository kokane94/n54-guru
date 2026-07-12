package com.example.n54guru.knowledge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.n54guru.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIDiagnosticsScreen() {
    var symptoms by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(N54Colors.background)
            .padding(16.dp)
    ) {
        N54ScreenHeader(
            title = "AI Diagnostics",
            subtitle = "Describe your symptoms — get N54-specific diagnosis",
            icon = Icons.Filled.AutoAwesome
        )

        Spacer(Modifier.height(20.dp))

        // Dark input card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(N54Colors.surface, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = symptoms,
                onValueChange = { symptoms = it },
                placeholder = {
                    Column {
                        Text("Describe what's happening...", color = N54Colors.textMuted)
                        Spacer(Modifier.height(12.dp))
                        Text("Example: Car has a rough idle when cold, and I hear a metallic rattling sound. Also getting occasional misfires on cylinder 3.", color = N54Colors.textMuted, fontSize = 13.sp)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = N54Colors.textPrimary,
                    unfocusedTextColor = N54Colors.textPrimary,
                    focusedBorderColor = N54Colors.border,
                    unfocusedBorderColor = N54Colors.border,
                    containerColor = N54Colors.surfaceVariant
                ),
                minLines = 6,
                maxLines = 8
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    loading = true
                    result = ""
                    result = diagnose(symptoms)
                    loading = false
                },
                enabled = symptoms.isNotBlank() && !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = N54Colors.primary, contentColor = N54Colors.onPrimary)
            ) {
                if (loading) {
                    CircularProgressIndicator(color = N54Colors.onPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Diagnose", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }

        if (result.isNotBlank()) {
            Spacer(Modifier.height(20.dp))
            N54Card {
                Text("Diagnosis", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = N54Colors.textPrimary)
                Spacer(Modifier.height(10.dp))
                Text(result, color = N54Colors.textSecondary, fontSize = 14.sp)
            }
        }
    }
}

private fun diagnose(input: String): String {
    val lowered = input.lowercase()
    return when {
        lowered.contains("rattle") || lowered.contains("metal") || lowered.contains("turbine") -> "Likely wastegate rattle or turbo bearing wear.\n\nCheck:\n• Wastegate flap play with pry bar\n• Smoke test for boost leaks\n• Turbo shaft play (cold engine)\n\nIf play is severe, plan for turbo repair/replacement."
        lowered.contains("misfire") || lowered.contains("rough idle") -> "Misfires often come from ignition, fuel, or carbon buildup.\n\nCheck:\n• Swap coil/plug to another cylinder to isolate\n• Fuel injector balance test\n• Compression test\n• Walnut blast intake valves if idle is rough and no ignition fault moves\n\nIf misfire follows a coil/plug, replace that component."
        lowered.contains("29cd") || lowered.contains("fuel rail") || lowered.contains("rail pressure") -> "Low rail pressure usually means HPFP or LPFP.\n\nCheck:\n• Actual vs requested rail pressure under WOT\n• LPFP pressure at WOT\n• HPFP volume test\n\nIf rail pressure drops under load with good LPFP, replace HPFP."
        lowered.contains("overheat") || lowered.contains("coolant") || lowered.contains("water pump") -> "Overheating on N54 is commonly the electric water pump, thermostat, or expansion tank/cap.\n\nCheck:\n• Water pump operation/noise\n• Coolant level and expansion tank cracks\n• Thermostat opens properly\n• Fan comes on\n\nReplace pump + thermostat preventively if near 60k-80k miles."
        lowered.contains("boost") || lowered.contains("2a99") || lowered.contains("charge pressure") -> "Boost-related faults usually mean a leak, wastegate, or solenoid issue.\n\nCheck:\n• Smoke test all boost hoses, charge pipe, intercooler\n• Wastegate actuator movement and vacuum lines\n• Boost solenoid function\n• Turbo shaft play"
        else -> "Based on the symptoms, start with the basics:\n\n1. Pull codes with MHD/INPA/ISTA\n2. Check fluids, oil condition, coolant level\n3. Log fuel pressure, boost, and ignition corrections\n4. Post the codes and logs in an N54 forum for deeper help."
    }
}
