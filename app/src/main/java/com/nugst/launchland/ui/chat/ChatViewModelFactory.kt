package com.nugst.launchland.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nugst.launchland.data.local.dao.ChatDao
import com.nugst.launchland.data.local.dao.UserDao
import com.nugst.launchland.data.repository.AiRepositoryImpl
import com.nugst.launchland.domain.repository.JobRepository

class ChatViewModelFactory(
    private val jobRepository: JobRepository,
    private val aiRepository: AiRepositoryImpl,
    private val chatDao: ChatDao,
    private val userDao: UserDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(jobRepository, aiRepository, chatDao, userDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
