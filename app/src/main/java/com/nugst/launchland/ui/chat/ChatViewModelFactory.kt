package com.nugst.launchland.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nugst.launchland.domain.repository.JobRepository

class ChatViewModelFactory(
    private val jobRepository: JobRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(jobRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
