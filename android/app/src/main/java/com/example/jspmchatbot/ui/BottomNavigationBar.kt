package com.example.jspmchatbot.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jspmchatbot.theme.*

enum class PortalTab {
    HOME,
    TIMETABLE,
    CHATBOT,
    ATTENDANCE,
    NOTICES
}

@Composable
fun PortalBottomBar(
    currentTab: PortalTab,
    onTabSelected: (PortalTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTabItem("Home", "🏠", currentTab == PortalTab.HOME) { onTabSelected(PortalTab.HOME) }
            BottomTabItem("Timetable", "📅", currentTab == PortalTab.TIMETABLE) { onTabSelected(PortalTab.TIMETABLE) }

            // Highlighted AI Chatbot center button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = (-8).dp)
                    .clickable { onTabSelected(PortalTab.CHATBOT) }
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PortalPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🤖", fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "AI Chat",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentTab == PortalTab.CHATBOT) PortalPrimary else TextSecondary
                )
            }

            BottomTabItem("Attendance", "📊", currentTab == PortalTab.ATTENDANCE) { onTabSelected(PortalTab.ATTENDANCE) }
            BottomTabItem("Notices", "📢", currentTab == PortalTab.NOTICES) { onTabSelected(PortalTab.NOTICES) }
        }
    }
}

@Composable
fun BottomTabItem(
    title: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) PortalPrimary else TextSecondary
        )
    }
}
