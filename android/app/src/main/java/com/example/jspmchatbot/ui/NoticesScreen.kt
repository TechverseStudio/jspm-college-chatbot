package com.example.jspmchatbot.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jspmchatbot.theme.*

data class NoticeItem(
    val id: String,
    val category: String,
    val title: String,
    val description: String,
    val date: String,
    val icon: String
)

val SAMPLE_NOTICES = listOf(
    NoticeItem("n1", "Academic", "College Holiday Notice", "College will remain closed on 18 Oct 2026 on account of holiday. Regular lectures resume Monday.", "16 Oct 2026", "🏛️"),
    NoticeItem("n2", "Exam", "Winter 2026 Examination Form Last Date", "Winter 2026 semester examination exam form submission without late fee ends on 25 Oct 2026.", "14 Oct 2026", "📝"),
    NoticeItem("n3", "Academic", "Software Engineering Assignment Submission", "Submit your Software Engineering Assignment by 20 Oct 2026 to Mrs. A.R. Sonawane.", "12 Oct 2026", "📋"),
    NoticeItem("n4", "Others", "Workshop on AI & ML", "Department of Computer Engineering organizes hands-on workshop on AI & ML on 22 Oct 2026.", "10 Oct 2026", "💡"),
    NoticeItem("n5", "Exam", "MHT-CET & DSY Document Verification", "Direct Second Year and CAP round document verification counter open at Admin Block.", "08 Oct 2026", "🎓")
)

@Composable
fun NoticesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Academic", "Exam", "Others")

    val filteredNotices = if (selectedCategory == "All") {
        SAMPLE_NOTICES
    } else {
        SAMPLE_NOTICES.filter { it.category == selectedCategory }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PortalBg)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📢 College Notices & Circulars",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Category Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 11.sp) }
                )
            }
        }

        // Notices List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredNotices, key = { it.id }) { notice ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PortalSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(notice.icon, fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = notice.category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PortalPrimary
                                )
                                Text(
                                    text = notice.date,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = notice.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = notice.description,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
