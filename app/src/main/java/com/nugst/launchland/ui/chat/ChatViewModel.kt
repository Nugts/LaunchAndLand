package com.nugst.launchland.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nugst.launchland.data.local.dao.ChatDao
import com.nugst.launchland.data.local.dao.UserDao
import com.nugst.launchland.data.local.entity.ChatMessage
import com.nugst.launchland.data.local.entity.ChatThread
import com.nugst.launchland.data.local.entity.UserProfile
import com.nugst.launchland.data.repository.AiRepositoryImpl
import com.nugst.launchland.domain.repository.JobRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val jobRepository: JobRepository,
    private val aiRepository: AiRepositoryImpl,
    private val chatDao: ChatDao,
    private val userDao: UserDao
) : ViewModel() {

    private val _currentThreadId = MutableStateFlow<String?>(null)
    val currentThreadId: StateFlow<String?> = _currentThreadId

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<ChatMessage>> = _currentThreadId
        .flatMapLatest { threadId ->
            if (threadId != null) {
                chatDao.getMessagesForThread(threadId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val userProfile: StateFlow<UserProfile?> = userDao.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _recommendations = MutableStateFlow<List<Recommendation>>(emptyList())
    val recommendations: StateFlow<List<Recommendation>> = _recommendations

    private val _errorPopup = MutableStateFlow<String?>(null)
    val errorPopup: StateFlow<String?> = _errorPopup

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing

    fun startNewChat(content: String) {
        viewModelScope.launch {
            val threadId = UUID.randomUUID().toString()
            val thread = ChatThread(id = threadId, title = content.take(30))
            chatDao.insertThread(thread)
            _currentThreadId.value = threadId
            
            sendMessageInternal(content)
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            sendMessageInternal(content)
        }
    }

    private suspend fun sendMessageInternal(content: String) {
        val threadId = _currentThreadId.value ?: return
        val userMessage = ChatMessage(
            threadId = threadId,
            content = content,
            role = "user"
        )
        chatDao.insertMessage(userMessage)

        if (content.startsWith("http")) {
            extractAndAnalyze(content)
        } else {
            generateAiResponse(content)
        }
    }

    private fun extractAndAnalyze(url: String) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            val result = jobRepository.extractJobDescription(url)
            result.onSuccess { description ->
                generateAiResponse("Analyze this job description and suggest 3 specific activities/certifications to fill gaps in my profile. Job: $description")
            }.onFailure { error ->
                addAssistantMessage("Extraction failed: ${error.message}. Please paste the description manually.")
            }
            _isAnalyzing.value = false
        }
    }

    private fun generateAiResponse(prompt: String) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            val profile = userProfile.value
            val studentContext = if (profile != null) {
                "User Profile: Name: ${profile.name}, Education: ${profile.education}, Skills: ${profile.skills}, Experience: ${profile.experience}. "
            } else {
                "Context: The user is a student with no prior professional job experience. "
            }
            
            val instructions = "Format your response naturally, but if you have specific recommendations, include them at the end starting with '[REC]' on a new line for each."
            val fullPrompt = studentContext + instructions + "\n\nUser Query: " + prompt
            
            val result = aiRepository.generateResponse(fullPrompt)
            result.onSuccess { response ->
                parseAndShowResponse(response)
            }.onFailure { error ->
                if (error.message == "MISSING_API_KEY") {
                    _errorPopup.value = "Missing API Key. Please go to Settings to add your Gemini API Key."
                } else {
                    addAssistantMessage("AI Error: ${error.message}")
                }
            }
            _isAnalyzing.value = false
        }
    }

    private suspend fun parseAndShowResponse(response: String) {
        val lines = response.split("\n")
        val chatContent = lines.filter { !it.trim().startsWith("[REC]") }.joinToString("\n").trim()
        val recLines = lines.filter { it.trim().startsWith("[REC]") }
        
        val newRecs = recLines.mapNotNull { line ->
            val content = line.replace("[REC]", "").trim()
            val parts = content.split(":", limit = 2)
            if (parts.size == 2) {
                Recommendation(parts[0].trim(), "Suggested", parts[1].trim())
            } else {
                Recommendation(content, "Suggested", "Based on job analysis")
            }
        }

        if (newRecs.isNotEmpty()) {
            _recommendations.value = newRecs
        }
        
        addAssistantMessage(chatContent.ifBlank { response })
    }

    private suspend fun addAssistantMessage(content: String) {
        val threadId = _currentThreadId.value ?: return
        val assistantMessage = ChatMessage(
            threadId = threadId,
            content = content,
            role = "assistant"
        )
        chatDao.insertMessage(assistantMessage)
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            userDao.saveUserProfile(profile)
        }
    }

    fun loadThread(threadId: String) {
        _currentThreadId.value = threadId
    }

    fun clearCurrentThread() {
        _currentThreadId.value = null
        _recommendations.value = emptyList()
    }

    fun dismissError() {
        _errorPopup.value = null
    }
}
