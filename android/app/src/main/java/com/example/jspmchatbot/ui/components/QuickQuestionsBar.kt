package com.example.jspmchatbot.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jspmchatbot.data.model.QuickQuestion

@Composable
fun QuickQuestionsBar(
    questions: List<QuickQuestion>,
    onQuestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (questions.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            questions.forEach { q ->
                SuggestionChip(
                    onClick = { onQuestionClick(q.question) },
                    label = {
                        Text(
                            text = q.question.take(36) + if (q.question.length > 36) "..." else "",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}
