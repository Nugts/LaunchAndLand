package com.nugst.launchland

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.nugst.launchland.data.local.LaunchlandDatabase
import com.nugst.launchland.data.repository.AiRepositoryImpl
import com.nugst.launchland.data.repository.JobRepositoryImpl
import com.nugst.launchland.ui.chat.ChatScreen
import com.nugst.launchland.ui.chat.ChatViewModel
import com.nugst.launchland.ui.chat.ChatViewModelFactory
import com.nugst.launchland.ui.input.JobInputScreen
import com.nugst.launchland.ui.onboarding.OnboardingScreen
import com.nugst.launchland.ui.settings.SettingsScreen
import com.nugst.launchland.ui.theme.LaunchLandTheme
import com.nugst.launchland.util.SecurityManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val securityManager = SecurityManager(this)
        val aiRepository = AiRepositoryImpl(securityManager)
        val db = Room.databaseBuilder(
            applicationContext,
            LaunchlandDatabase::class.java, "launchland-db"
        ).fallbackToDestructiveMigration().build()
        val chatDao = db.chatDao()
        val userDao = db.userDao()
        
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            val systemDarkMode = isSystemInDarkTheme()
            LaunchedEffect(Unit) {
                isDarkMode = systemDarkMode
            }

            LaunchLandTheme(darkTheme = isDarkMode) {
                PermissionRequestWrapper {
                    MainApp(
                        securityManager = securityManager,
                        aiRepository = aiRepository,
                        chatDao = chatDao,
                        userDao = userDao,
                        isDarkMode = isDarkMode,
                        onThemeToggle = { isDarkMode = it },
                        onFeedback = { sendFeedbackEmail() }
                    )
                }
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

@Composable
fun PermissionRequestWrapper(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var hasStoragePermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) true
            else ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasStoragePermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasStoragePermission && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    content()
}

enum class Screen {
    Onboarding, Input, Chat, Settings
}

@Composable
fun MainApp(
    securityManager: SecurityManager,
    aiRepository: AiRepositoryImpl,
    chatDao: com.nugst.launchland.data.local.dao.ChatDao,
    userDao: com.nugst.launchland.data.local.dao.UserDao,
    isDarkMode: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onFeedback: () -> Unit
) {
    val jobRepository = remember { JobRepositoryImpl() }
    val chatViewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(jobRepository, aiRepository, chatDao, userDao))
    
    val currentThreadId by chatViewModel.currentThreadId.collectAsState()
    val messages by chatViewModel.messages.collectAsState()
    val userProfile by chatViewModel.userProfile.collectAsState()
    
    var currentScreen by remember { 
        mutableStateOf(
            if (securityManager.getApiKey().isNullOrBlank()) Screen.Onboarding 
            else if (currentThreadId == null && messages.isEmpty()) Screen.Input 
            else Screen.Chat
        ) 
    }
    
    // Sync currentScreen with currentThreadId and messages
    LaunchedEffect(currentThreadId, messages) {
        if (currentScreen == Screen.Onboarding) return@LaunchedEffect
        
        if (currentThreadId != null) {
            currentScreen = Screen.Chat
        } else if (messages.isEmpty() && currentScreen != Screen.Settings) {
            currentScreen = Screen.Input
        }
    }

    val recommendations by chatViewModel.recommendations.collectAsState()
    val errorPopup by chatViewModel.errorPopup.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Global Error Popup
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
                            securityManager.saveApiKey(key)
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
                        apiKey = securityManager.getApiKey() ?: "",
                        onApiKeyChange = { securityManager.saveApiKey(it) },
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
