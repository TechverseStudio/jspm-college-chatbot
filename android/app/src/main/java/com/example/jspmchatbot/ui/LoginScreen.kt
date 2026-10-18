package com.example.jspmchatbot.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jspmchatbot.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: (role: String, userName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRole by remember { mutableStateOf("Student") }
    var userId by remember { mutableStateOf("TYCO-2026-042") }
    var password by remember { mutableStateOf("••••••••") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(PortalSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏛️", fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "JSPM's RSCOE\nTathawade, Pune",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = PortalPrimaryDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Login",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "Welcome back! Please login to continue.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            // Middle Form
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Role Selector Tabs (Student, Faculty, Admin)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val roles = listOf("Student" to "🎓", "Faculty" to "👨‍🏫", "Admin" to "🛡️")
                    roles.forEach { (role, icon) ->
                        val isSelected = selectedRole == role
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) PortalSurfaceVariant else Color.Transparent)
                                .border(
                                    1.5.dp,
                                    if (isSelected) PortalPrimary else PortalCardBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedRole = role }
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(icon, fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = role,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PortalPrimary else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Roll Number / User ID
                Text(
                    text = "Roll Number / User ID",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = userId,
                    onValueChange = { userId = it },
                    placeholder = { Text("Enter your ID", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Password
                Text(
                    text = "Password",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Enter your password", fontSize = 13.sp) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(if (passwordVisible) "👁️" else "🙈", fontSize = 16.sp)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Login Button
                Button(
                    onClick = {
                        val name = if (selectedRole == "Student") "Anuja" else (if (selectedRole == "Faculty") "Mrs. A.R. Sonawane" else "Admin")
                        onLoginSuccess(selectedRole, name)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PortalPrimary)
                ) {
                    Text("Login as $selectedRole", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Forgot Password?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PortalPrimary,
                    modifier = Modifier.clickable { }
                )
            }

            // Bottom Demo Bypass
            TextButton(
                onClick = { onLoginSuccess("Student", "Anuja") },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("⚡ Quick Demo Access as Student (Anuja)", fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}
