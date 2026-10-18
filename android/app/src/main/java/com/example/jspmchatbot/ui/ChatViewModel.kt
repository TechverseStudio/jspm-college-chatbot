package com.example.jspmchatbot.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jspmchatbot.data.model.*
import com.example.jspmchatbot.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository(application.applicationContext)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _selectedInstitute = MutableStateFlow("ALL")
    val selectedInstitute: StateFlow<String> = _selectedInstitute.asStateFlow()

    private val _isServerConnected = MutableStateFlow(false)
    val isServerConnected: StateFlow<Boolean> = _isServerConnected.asStateFlow()

    private val _serverUrl = MutableStateFlow(repository.currentBaseUrl)
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _quickQuestions = MutableStateFlow<List<QuickQuestion>>(emptyList())
    val quickQuestions: StateFlow<List<QuickQuestion>> = _quickQuestions.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private var lastUserMessage: String? = null

    init {
        // Post Initial Welcome Message
        val welcomeMsg = ChatMessage(
            text = "### Welcome to JSPM Tathawade Campus Chatbot! 🎓\n\n" +
                    "I am the official institutional assistant for **JSPM Tathawade Campus**, including:\n" +
                    "- **RSCOE** (Engineering & Technology, Autonomous)\n" +
                    "- **RSCOPR** (Pharmacy & Research)\n" +
                    "- **JIMS** (Management Studies - MBA & MCA)\n" +
                    "- **RSCOED** (College of Education - B.Ed)\n" +
                    "- **Polytechnic** (Engineering Diploma)\n\n" +
                    "Ask me about approved **2026–27 Fees**, **Placement Statistics**, **Cutoffs & CAP rounds**, **Direct Second Year (DSY)** admissions, **Hostel/Bus contacts**, or select a quick question below.",
            role = MessageRole.ASSISTANT
        )
        _messages.value = listOf(welcomeMsg)

        // Check Server Health & Load Quick Questions
        viewModelScope.launch {
            checkConnection()
            loadQuickQuestions()
        }
    }

    suspend fun checkConnection(): Boolean {
        val connected = repository.checkBackendHealth()
        _isServerConnected.value = connected
        return connected
    }

    private suspend fun loadQuickQuestions() {
        val questions = repository.getQuickQuestions(_selectedInstitute.value)
        _quickQuestions.value = questions
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun selectInstitute(instituteCode: String) {
        _selectedInstitute.value = instituteCode
        viewModelScope.launch {
            loadQuickQuestions()
        }
    }

    fun updateServerUrl(url: String) {
        repository.currentBaseUrl = url.trim()
        _serverUrl.value = repository.currentBaseUrl
        viewModelScope.launch {
            checkConnection()
            loadQuickQuestions()
        }
    }

    suspend fun testConnection(url: String): Boolean {
        val orig = repository.currentBaseUrl
        repository.currentBaseUrl = url.trim()
        val ok = repository.checkBackendHealth()
        if (ok) {
            _serverUrl.value = repository.currentBaseUrl
            _isServerConnected.value = true
        } else {
            repository.currentBaseUrl = orig
        }
        return ok
    }

    fun sendMessage(queryText: String? = null) {
        val query = (queryText ?: _inputText.value).trim()
        if (query.isBlank() || _isLoading.value) return

        lastUserMessage = query
        _inputText.value = ""

        val userMessage = ChatMessage(
            text = query,
            role = MessageRole.USER
        )
        _messages.value = _messages.value + userMessage
        _isLoading.value = true

        viewModelScope.launch {
            val response = repository.sendMessage(query, _selectedInstitute.value)
            _messages.value = _messages.value + response
            _isLoading.value = false
            // Periodically refresh connection state
            _isServerConnected.value = repository.checkBackendHealth()
        }
    }

    fun retryLast() {
        lastUserMessage?.let { sendMessage(it) }
    }

    fun clearChat() {
        val welcomeMsg = ChatMessage(
            text = "Started a new inquiry session. How can I assist you with JSPM Tathawade Campus today?",
            role = MessageRole.ASSISTANT
        )
        _messages.value = listOf(welcomeMsg)
    }
}
