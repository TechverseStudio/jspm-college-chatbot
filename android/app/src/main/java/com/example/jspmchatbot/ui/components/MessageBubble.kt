package com.example.jspmchatbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jspmchatbot.data.model.ChatMessage
import com.example.jspmchatbot.data.model.MessageRole
import com.example.jspmchatbot.theme.*

@Composable
fun MessageBubble(
    message: ChatMessage,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isUser = message.role == MessageRole.USER

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            // Assistant Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CollegeNavyPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text("🎓", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) BubbleUser else BubbleAssistant,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header label if assistant
                    if (!isUser) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = "JSPM Official Assistant",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CollegeCyan
                            )
                            if (message.isOffline) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "[Offline Cache]",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarningBorder
                                )
                            }
                        }
                    }

                    // Main text
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp,
                        color = Color.White
                    )

                    // Embedded Warnings (Discrepancies / Disclaimers)
                    if (message.warnings.isNotEmpty()) {
                        message.warnings.forEach { warning ->
                            WarningCalloutCard(warning = warning)
                        }
                    }

                    // Embedded Placement Statistics Table
                    if (message.placementStats != null && message.placementStats.isNotEmpty()) {
                        PlacementTableCard(stats = message.placementStats)
                    }

                    // Embedded Fee Breakdown Cards
                    if (message.feeBreakdown != null && message.feeBreakdown.isNotEmpty()) {
                        FeeBreakdownCard(fees = message.feeBreakdown)
                    }

                    // Embedded Contact Cards
                    if (message.contactCards != null && message.contactCards.isNotEmpty()) {
                        ContactCardsSection(contacts = message.contactCards)
                    }

                    // Source Citation Links
                    if (message.citations.isNotEmpty()) {
                        CitationChips(citations = message.citations)
                    }
                }
            }

            // Retry action if message resulted in error
            if (message.isError) {
                TextButton(
                    onClick = onRetry,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text("⚠️ Connection Error. Tap to Retry.", fontSize = 11.sp, color = ErrorRed)
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 14.sp)
            }
        }
    }
}
