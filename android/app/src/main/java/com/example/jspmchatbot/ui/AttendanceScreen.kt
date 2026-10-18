package com.example.jspmchatbot.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

data class SubjectAttendance(
    val code: String,
    val name: String,
    val percentage: Int,
    val attended: Int,
    val total: Int
)

val SAMPLE_ATTENDANCE = listOf(
    SubjectAttendance("OSY", "Operating System", 42, 11, 26),
    SubjectAttendance("STE", "Software Engineering", 85, 22, 26),
    SubjectAttendance("ACN", "Advanced Computer Network", 76, 19, 25),
    SubjectAttendance("EDE", "Entrepreneurship Development", 68, 15, 22)
)

@Composable
fun AttendanceScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PortalBg)
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📊 Attendance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Circular Progress Ring Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular Ring Box
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(PortalSurfaceVariant)
                        .border(10.dp, PortalPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "78%",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PortalPrimary
                        )
                        Text(
                            text = "Overall",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PortalSurfaceVariant, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatCounter("40", "Total Lectures")
                    StatCounter("31", "Attended", StatusGreen)
                    StatCounter("9", "Pending", StatusAmber)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Warning Alert Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StatusRedBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⚠️", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "You are below 45% in OSY (Operating System). Please maintain at least 75% to avoid term-grant detention.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFF991B1B)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subject-wise Attendance Header
        Text(
            text = "Subject-wise Breakdown",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subject list cards
        SAMPLE_ATTENDANCE.forEach { sub ->
            val barColor = if (sub.percentage < 50) StatusRed else (if (sub.percentage < 75) StatusAmber else StatusGreen)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${sub.code} (${sub.name})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${sub.percentage}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = barColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { sub.percentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = barColor,
                        trackColor = PortalSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Attended: ${sub.attended} / ${sub.total} sessions",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun StatCounter(count: String, label: String, color: Color = TextPrimary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 10.sp, color = TextSecondary)
    }
}
