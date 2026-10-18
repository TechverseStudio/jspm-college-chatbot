package com.example.jspmchatbot.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

data class TimetableRow(
    val timeSlot: String,
    val mon: String,
    val tue: String,
    val wed: String,
    val thu: String,
    val fri: String,
    val isBreak: Boolean = false
)

val SAMPLE_TIMETABLE = listOf(
    TimetableRow(
        "10:30 - 11:30",
        "STE (ARS)\n(F-111)",
        "STE (ARS)\n(F-111)",
        "ACN (SSG)\n(F-111)",
        "STE(ARS)-T3-L3\nEDE(MSP)-T2-205",
        "STE(ARS)-T2-212\nEDE(TRP)-T1-212"
    ),
    TimetableRow(
        "11:30 - 12:30",
        "OSY (PVP)\n(F-111)",
        "ACN (SSG)\n(F-111)",
        "STE (ARS)\n(F-111)",
        "Practicals\n(Cont.)",
        "Practicals\n(Cont.)"
    ),
    TimetableRow(
        "12:30 - 1:15",
        "LONG BREAK",
        "LONG BREAK",
        "LONG BREAK",
        "LONG BREAK",
        "LONG BREAK",
        isBreak = true
    ),
    TimetableRow(
        "1:15 - 2:15",
        "EDE (TRP)\n(F-205)",
        "OSY (PVP)\n(F-205)",
        "OSY (PVP)\n(F-205)",
        "ACN (SSG)\n(F-205)",
        "ACN (SSG)\n(F-205)"
    ),
    TimetableRow(
        "2:15 - 3:15",
        "ACN (SSG)\n(F-205)",
        "STE (ARS)\n(F-205)",
        "OSY (PVP)\n(F-205)",
        "OSY (PVP)\n(F-205)",
        "OSY (PVP)\n(F-205)"
    ),
    TimetableRow(
        "3:15 - 3:30",
        "SHORT BREAK",
        "SHORT BREAK",
        "SHORT BREAK",
        "SHORT BREAK",
        "SHORT BREAK",
        isBreak = true
    ),
    TimetableRow(
        "3:30 - 5:30",
        "OSY-T1-L3\nSTE-T2-L2\nACN-T3-L1",
        "ACN-T1-L1\nOSY-T2-L3\nSTE-T3-L2",
        "STE(ARS)-T1-206\nEDE(TRP)-T3-111",
        "STE-T1-L2\nACN-T2-L1\nOSY-T3-L3",
        "STE (ARS)\n(F-111) [3:30-4:30]"
    )
)

@Composable
fun TimetableScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDivision by remember { mutableStateOf("TYCO-A (Computer - 61)") }
    val divisions = listOf(
        "TYCO-A (Computer - 61)",
        "TYCO-B (Computer - 58)",
        "TYEJ (E&TC - 61)"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PortalBg)
            .padding(14.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📅 Academic Timetable",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(PortalSurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "w.e.f. 10/8/2026",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PortalPrimary
                )
            }
        }

        Text(
            text = "Fifth Term (CO5K Scheme) • AY 2026–27 • JSPM RSCOE Polytechnic",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
        )

        // Division selector chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            divisions.forEach { div ->
                FilterChip(
                    selected = selectedDivision == div,
                    onClick = { selectedDivision = div },
                    label = { Text(div, fontSize = 11.sp) }
                )
            }
        }

        // Timetable Scrollable Matrix
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            val horizontalScrollState = rememberScrollState()
            val verticalScrollState = rememberScrollState()

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScrollState)
                        .horizontalScroll(horizontalScrollState)
                        .padding(10.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .background(PortalSurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp, horizontal = 6.dp)
                    ) {
                        Text("Day / Time", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(95.dp))
                        Text("Monday", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(110.dp))
                        Text("Tuesday", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(110.dp))
                        Text("Wednesday", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(110.dp))
                        Text("Thursday", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(120.dp))
                        Text("Friday", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(120.dp))
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = PortalCardBorder)

                    // Data Rows
                    SAMPLE_TIMETABLE.forEach { row ->
                        if (row.isBreak) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(StatusAmberBg, RoundedCornerShape(6.dp))
                                    .padding(vertical = 6.dp, horizontal = 6.dp)
                            ) {
                                Text(row.timeSlot, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = StatusAmber, modifier = Modifier.width(95.dp))
                                Text(
                                    "☕ ${row.mon}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.width(450.dp)
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 6.dp, horizontal = 6.dp)
                            ) {
                                Text(row.timeSlot, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary, modifier = Modifier.width(95.dp))
                                Text(row.mon, fontSize = 10.sp, modifier = Modifier.width(110.dp))
                                Text(row.tue, fontSize = 10.sp, modifier = Modifier.width(110.dp))
                                Text(row.wed, fontSize = 10.sp, modifier = Modifier.width(110.dp))
                                Text(row.thu, fontSize = 10.sp, modifier = Modifier.width(120.dp))
                                Text(row.fri, fontSize = 10.sp, modifier = Modifier.width(120.dp))
                            }
                        }
                        HorizontalDivider(color = PortalCardBorder.copy(alpha = 0.5f))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Subjects & Staff Legend Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PortalSurfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Subjects & Faculty:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = PortalPrimary
                        )
                        Text("• OSY: Operating System — Msr. P.V. Patil (PVP)", fontSize = 10.sp, color = TextPrimary)
                        Text("• STE: Software Engineering — Mrs. A.R. Sonawane (ARS)", fontSize = 10.sp, color = TextPrimary)
                        Text("• ACN: Advanced Computer Network — Mr. S.S. Ghosh (SSG)", fontSize = 10.sp, color = TextPrimary)
                        Text("• EDE: Entrepreneurship Dev. & Startups — Ms. T.R. Patil (TRP) / Mrs. M.S. Patil (MSP)", fontSize = 10.sp, color = TextPrimary)
                        Text("• Labs: L1: F-05 • L2: F-06 • L3: F-104", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = PortalAccent)
                        Text("• Signatories: Mrs. H.N. Bhandare (Timetable) • Mrs. V.I. Thakare (HOD) • Mrs. S.U. Puri (DAC) • Dr. S.S. Gaikwad (Principal)", fontSize = 9.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}
