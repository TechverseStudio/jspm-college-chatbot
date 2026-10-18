package com.example.jspmchatbot.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jspmchatbot.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsDialog(
    currentUrl: String,
    onSaveUrl: (String) -> Unit,
    onTestConnection: suspend (String) -> Boolean,
    onDismiss: () -> Unit
) {
    var urlText by remember { mutableStateOf(currentUrl) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("⚙️ Backend Server Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column {
                Text(
                    text = "Configure the API base URL for retrieval, live web scraping updates, and AI generation:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = urlText,
                    onValueChange = {
                        urlText = it
                        testResult = null
                    },
                    label = { Text("Server Base URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Presets
                Text("Quick Presets:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { urlText = "http://192.168.1.4:8088"; testResult = null },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Phone (192.168.1.4:8088)", fontSize = 10.sp)
                    }
                    OutlinedButton(
                        onClick = { urlText = "http://10.0.2.2:8088"; testResult = null },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Emulator (:8088)", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Test Connection
                Button(
                    onClick = {
                        isTesting = true
                        testResult = null
                        scope.launch {
                            val ok = onTestConnection(urlText)
                            isTesting = false
                            testResult = if (ok) "✅ Server connected successfully!" else "❌ Unable to connect to server."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Testing...", fontSize = 12.sp)
                    } else {
                        Text("Test Connection", fontSize = 12.sp)
                    }
                }

                if (testResult != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = testResult!!,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (testResult!!.startsWith("✅")) SuccessGreen else ErrorRed
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveUrl(urlText)
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
