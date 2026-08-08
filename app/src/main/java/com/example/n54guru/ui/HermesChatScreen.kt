package com.example.n54guru.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.n54guru.ui.theme.N54Colors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Hermes Chat — real AI partner screen, wired to the local Hermes relay
 * at http://127.0.0.1:11435/v1/chat. The relay proxies to the Ollama
 * daemon already running on the device, so messages stay on-device.
 *
 * The static credit page (AiPartnerScreen) is preserved as a separate route.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HermesChatScreen() {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var input by remember { mutableStateOf("") }
    var pending by remember { mutableStateOf(false) }
    var relayOnline by remember { mutableStateOf<Boolean?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Greet on first load + ping the relay
    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            messages += ChatMessage(
                role = "assistant",
                text = "Kane. Hermes here. I'm running locally on your Termux. " +
                    "Ask me about N54 fault codes, UDS service IDs, K+DCAN wiring, " +
                    "or what I'm seeing in your live OBD2 data. Type to start."
            )
        }
        relayOnline = checkRelay()
    }

    fun send() {
        val text = input.trim()
        if (text.isEmpty() || pending) return
        messages += ChatMessage(role = "user", text = text)
        input = ""
        pending = true
        scope.launch {
            val reply = withContext(Dispatchers.IO) {
                try {
                    val resp = httpPost(
                        "http://127.0.0.1:11435/v1/chat",
                        JSONObject().apply {
                            put("messages", JSONArray().apply {
                                messages.forEach { m ->
                                    put(JSONObject().apply {
                                        put("role", m.role)
                                        put("text", m.text)
                                    })
                                }
                            })
                        }.toString()
                    )
                    JSONObject(resp).optString("text", "(empty response)")
                } catch (e: Exception) {
                    "Relay offline. Start it with: hermes-relay &\n(error: ${e.message})"
                }
            }
            messages += ChatMessage(role = "assistant", text = reply)
            pending = false
            // Scroll to bottom
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(N54Colors.background)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        when (relayOnline) {
                            true -> Color(0xFF4ADE80)
                            false -> Color(0xFFEF5350)
                            null -> Color(0xFF9AA3B2)
                        }
                    )
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Hermes",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = N54Colors.textPrimary
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "· local AI partner",
                fontSize = 13.sp,
                color = N54Colors.textMuted
            )
        }

        // Conversation
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 6.dp)
        ) {
            items(messages) { m -> MessageBubble(m) }
        }

        // Input bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask Hermes…", color = N54Colors.textMuted) },
                enabled = !pending,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { send() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = N54Colors.textPrimary,
                    unfocusedTextColor = N54Colors.textPrimary,
                    focusedBorderColor = N54Colors.primary,
                    unfocusedBorderColor = N54Colors.border
                )
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { send() },
                enabled = !pending && input.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (!pending && input.isNotBlank()) N54Colors.primary
                        else N54Colors.surfaceVariant
                    )
            ) {
                if (pending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = N54Colors.textPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (input.isNotBlank()) Color.White else N54Colors.textMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(m: ChatMessage) {
    val isUser = m.role == "user"
    val align = if (isUser) Alignment.End else Alignment.Start
    val bg = if (isUser) N54Colors.primary else N54Colors.surface
    val fg = if (isUser) Color.White else N54Colors.textPrimary

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(bg)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text = m.text, color = fg, fontSize = 14.sp)
        }
    }
}

private data class ChatMessage(val role: String, val text: String)

private fun checkRelay(): Boolean {
    return try {
        val conn = URL("http://127.0.0.1:11435/health").openConnection() as HttpURLConnection
        conn.connectTimeout = 1000
        conn.readTimeout = 1000
        conn.requestMethod = "GET"
        conn.responseCode == 200
    } catch (_: Exception) {
        false
    }
}

private fun httpPost(url: String, body: String): String {
    val conn = URL(url).openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.connectTimeout = 30000
    conn.readTimeout = 120000
    conn.doOutput = true
    conn.setRequestProperty("Content-Type", "application/json")
    conn.outputStream.use { it.write(body.toByteArray()) }
    val code = conn.responseCode
    val stream = if (code in 200..299) conn.inputStream else conn.errorStream
    return stream.bufferedReader().use { it.readText() }
}
