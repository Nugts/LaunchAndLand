package com.nugst.launchland.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nugst.launchland.domain.repository.JobRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _currentThreadId = MutableStateFlow<String?>(null)
    val currentThreadId: StateFlow<String?> = _currentThreadId

    private val _allMessages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    
    val messages: StateFlow<List<ChatMessage>> = combine(_currentThreadId, _allMessages) { threadId, allMsgs ->
        if (threadId != null) allMsgs[threadId] ?: emptyList() else emptyList()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey

    private val _recommendations = MutableStateFlow<List<Recommendation>>(emptyList())
    val recommendations: StateFlow<List<Recommendation>> = _recommendations

    private val _errorPopup = MutableStateFlow<String?>(null)
    val errorPopup: StateFlow<String?> = _errorPopup

    fun startNewChat(content: String) {
        val threadId = UUID.randomUUID().toString()
        _currentThreadId.value = threadId
        
        val userMessage = ChatMessage(threadId = threadId, content = content, role = "user")
        updateMessages(threadId, userMessage)
        
        fakeAiResponse(threadId, content)
    }

    fun sendMessage(content: String) {
        val threadId = _currentThreadId.value ?: return
        
        val userMessage = ChatMessage(threadId = threadId, content = content, role = "user")
        updateMessages(threadId, userMessage)

        fakeAiResponse(threadId, content)
    }

    private fun fakeAiResponse(threadId: String, input: String) {
        viewModelScope.launch {
            val response = if (input.startsWith("http")) {
                "I've analyzed the job link! It looks like a great opportunity. I noticed some requirements like teamwork and technical skills. Want me to draft a resume? (Fake AI Analysis)"
            } else {
                "I recognize your input: \"$input\". As your career assistant, I recommend focusing on tailoring your resume for these specific points. (Fake AI Echo)"
            }
            
            val assistantMessage = ChatMessage(threadId = threadId, content = response, role = "assistant")
            updateMessages(threadId, assistantMessage)

            // Mock recommendations
            _recommendations.value = listOf(
                Recommendation("React Basics", "Skill", "The job asks for frontend experience."),
                Recommendation("Agile Workshop", "Activity", "To fill the project management gap.")
            )
        }
    }

    private fun updateMessages(threadId: String, newMessage: ChatMessage) {
        val currentMap = _allMessages.value.toMutableMap()
        val threadMsgs = currentMap[threadId]?.toMutableList() ?: mutableListOf()
        threadMsgs.add(newMessage)
        currentMap[threadId] = threadMsgs
        _allMessages.value = currentMap
    }

    fun saveApiKey(key: String) {
        _apiKey.value = key
    }

    fun saveProfile(profile: UserProfile) {
        _userProfile.value = profile
    }

    fun clearCurrentThread() {
        _currentThreadId.value = null
        _recommendations.value = emptyList()
    }

    fun dismissError() {
        _errorPopup.value = null
    }
}
