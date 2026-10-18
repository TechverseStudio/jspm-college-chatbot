package com.example.jspmchatbot.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jspmchatbot.ui.components.*

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val selectedInstitute by viewModel.selectedInstitute.collectAsStateWithLifecycle()
    val isServerConnected by viewModel.isServerConnected.collectAsStateWithLifecycle()
    val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val quickQuestions by viewModel.quickQuestions.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                ChatTopBar(
                    isServerConnected = isServerConnected,
                    onOpenSettings = { showSettingsDialog = true },
                    onClearChat = { viewModel.clearChat() }
                )
                InstituteFilterRow(
                    selectedInstitute = selectedInstitute,
                    onSelectInstitute = { viewModel.selectInstitute(it) }
                )
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                QuickQuestionsBar(
                    questions = quickQuestions,
                    onQuestionClick = { question -> viewModel.sendMessage(question) }
                )
                ChatInputBar(
                    inputText = inputText,
                    onInputTextChanged = { viewModel.onInputTextChanged(it) },
                    onSendMessage = { viewModel.sendMessage() },
                    isLoading = isLoading
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    onRetry = { viewModel.retryLast() }
                )
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentUrl = serverUrl,
            onSaveUrl = { viewModel.updateServerUrl(it) },
            onTestConnection = { url -> viewModel.testConnection(url) },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
