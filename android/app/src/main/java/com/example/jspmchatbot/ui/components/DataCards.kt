package com.example.jspmchatbot.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jspmchatbot.data.model.*
import com.example.jspmchatbot.theme.*

@Composable
fun PlacementTableCard(stats: List<PlacementStatModel>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "📊 Year-Wise Placement Statistics",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Year", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                Text("Visited", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.0f))
                Text("Placed", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.0f))
                Text("Offers", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.0f))
                Text("Max Salary", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.4f))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

            // Table Rows
            stats.forEach { s ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(s.academicYear, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.2f))
                    Text(s.companiesVisited, fontSize = 11.sp, modifier = Modifier.weight(1.0f))
                    Text(s.studentsPlaced, fontSize = 11.sp, modifier = Modifier.weight(1.0f))
                    Text(s.offers, fontSize = 11.sp, modifier = Modifier.weight(1.0f))
                    Text(
                        s.maxSalary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1.4f)
                    )
                }
            }
        }
    }
}

@Composable
fun FeeBreakdownCard(fees: List<FeeItemModel>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "💰 Approved 2026–27 Fee Breakdown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Display each category card
            fees.forEach { fee ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(fee.programme, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text(fee.totalFee, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Text(fee.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tuition: ${fee.tuitionFee}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Dev: ${fee.developmentFee}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Univ: ${fee.universityFee}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun ContactCardsSection(contacts: List<ContactCardModel>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = "📞 Verified Institutional Contacts",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        contacts.forEach { contact ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(contact.role, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (!contact.department.isNullOrBlank()) {
                            Text(contact.department, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Row {
                        if (!contact.phone.isNullOrBlank()) {
                            Button(
                                onClick = { launchDialer(context, contact.phone) },
                                modifier = Modifier.padding(end = 4.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Call", fontSize = 11.sp)
                            }
                        }
                        if (!contact.email.isNullOrBlank()) {
                            OutlinedButton(
                                onClick = { launchEmail(context, contact.email) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Email", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WarningCalloutCard(warning: WarningModel, modifier: Modifier = Modifier) {
    val isConflict = warning.severity == "conflict"
    val bgColor: Color = if (isConflict) WarningBgDark else WarningBg
    val borderColor: Color = if (isConflict) WarningBorderDark else WarningBorder
    val textColor: Color = if (isConflict) WarningTextDark else WarningText

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isConflict) "⚠️ DATA DISCREPANCY NOTICE" else "ℹ️ INSTITUTIONAL NOTICE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = borderColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = warning.message,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = textColor
            )
            if (warning.sources.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Conflicting Sources: " + warning.sources.joinToString(", "),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun CitationChips(citations: List<CitationModel>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    if (citations.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth().padding(top = 6.dp)) {
        Text(
            text = "🔗 Verified Sources:",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            citations.take(3).forEach { cit ->
                AssistChip(
                    onClick = { launchBrowser(context, cit.url) },
                    label = {
                        Text(
                            text = cit.title.take(24) + if (cit.title.length > 24) "..." else "",
                            fontSize = 10.sp
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

private fun launchDialer(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone.replace(" ", "")))
        context.startActivity(intent)
    } catch (e: Exception) {}
}

private fun launchEmail(context: Context, email: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
        context.startActivity(intent)
    } catch (e: Exception) {}
}

private fun launchBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {}
}
