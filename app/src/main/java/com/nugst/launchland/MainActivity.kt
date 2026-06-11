package com.nugst.launchland

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nugst.launchland.data.repository.JobRepositoryImpl
import com.nugst.launchland.ui.chat.ChatScreen
import com.nugst.launchland.ui.chat.ChatViewModel
import com.nugst.launchland.ui.chat.ChatViewModelFactory
import com.nugst.launchland.ui.chat.UserProfile
import com.nugst.launchland.ui.input.JobInputScreen
import com.nugst.launchland.ui.onboarding.OnboardingScreen
import com.nugst.launchland.ui.settings.SettingsScreen
import com.nugst.launchland.ui.theme.LaunchLandTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            val systemDarkMode = isSystemInDarkTheme()
            LaunchedEffect(Unit) {
                isDarkMode = systemDarkMode
            }

            LaunchLandTheme(darkTheme = isDarkMode) {
                MainApp(
                    isDarkMode = isDarkMode,
                    onThemeToggle = { isDarkMode = it },
                    onFeedback = { sendFeedbackEmail() }
                )
            }
        }
    }

    private fun sendFeedbackEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:me@nugst.site")
            putExtra(Intent.EXTRA_SUBJECT, "Launch & Land Feedback")
        }
        startActivity(Intent.createChooser(intent, "Send Email"))
    }
}

enum class Screen {
    Onboarding, Input, Chat, Settings
}

@Composable
fun MainApp(
    isDarkMode: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onFeedback: () -> Unit
) {
    // Simplified Repository without dependencies for now
    val jobRepository = remember { JobRepositoryImpl() }
    val chatViewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(jobRepository))
    
    val currentThreadId by chatViewModel.currentThreadId.collectAsState()
    val messages by chatViewModel.messages.collectAsState()
    val userProfile by chatViewModel.userProfile.collectAsState()
    val apiKey by chatViewModel.apiKey.collectAsState()
    
    var currentScreen by remember { 
        mutableStateOf(if (apiKey.isBlank()) Screen.Onboarding else Screen.Input)
    }
    
    // Sync navigation
    LaunchedEffect(currentThreadId) {
        if (currentThreadId != null && currentScreen == Screen.Input) {
            currentScreen = Screen.Chat
        }
    }

    val recommendations by chatViewModel.recommendations.collectAsState()
    val errorPopup by chatViewModel.errorPopup.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    if (errorPopup != null) {
        AlertDialog(
            onDismissRequest = { chatViewModel.dismissError() },
            title = { Text("Error") },
            text = { Text(errorPopup!!) },
            confirmButton = {
                TextButton(onClick = { chatViewModel.dismissError() }) {
                    Text("OK")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentScreen != Screen.Onboarding,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Launch & Land", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Make new chat") },
                    selected = false,
                    onClick = {
                        chatViewModel.clearCurrentThread()
                        currentScreen = Screen.Input
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("View History") },
                    selected = false,
                    onClick = {
                        if (currentThreadId != null || messages.isNotEmpty()) {
                            currentScreen = Screen.Chat
                        }
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.History, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        currentScreen = Screen.Settings
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) }
                )
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                if (currentScreen != Screen.Onboarding) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentScreen == Screen.Chat || currentScreen == Screen.Input,
                            onClick = { 
                                if (currentThreadId == null && messages.isEmpty()) currentScreen = Screen.Input 
                                else currentScreen = Screen.Chat
                            },
                            icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat") },
                            label = { Text("Chat") }
                        )
                        NavigationBarItem(
                            selected = currentScreen == Screen.Settings,
                            onClick = { currentScreen = Screen.Settings },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings") }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                when (currentScreen) {
                    Screen.Onboarding -> OnboardingScreen(
                        onComplete = { key ->
                            chatViewModel.saveApiKey(key)
                            currentScreen = Screen.Input
                        }
                    )
                    Screen.Input -> JobInputScreen(
                        onStartAnalysis = { input ->
                            chatViewModel.startNewChat(input)
                        },
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                    Screen.Chat -> ChatScreen(
                        messages = messages,
                        recommendations = recommendations,
                        onSendMessage = { chatViewModel.sendMessage(it) },
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                    Screen.Settings -> SettingsScreen(
                        apiKey = apiKey,
                        onApiKeyChange = { chatViewModel.saveApiKey(it) },
                        userProfile = userProfile,
                        onProfileSave = { chatViewModel.saveProfile(it) },
                        isDarkMode = isDarkMode,
                        onThemeToggle = onThemeToggle,
                        onFeedbackClick = onFeedback
                    )
                }
            }
        }
    }
}
