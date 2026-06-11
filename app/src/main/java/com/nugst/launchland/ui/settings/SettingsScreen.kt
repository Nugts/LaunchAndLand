package com.nugst.launchland.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.nugst.launchland.ui.chat.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    userProfile: UserProfile?,
    onProfileSave: (UserProfile) -> Unit,
    isDarkMode: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onFeedbackClick: () -> Unit
) {
    var name by remember(userProfile) { mutableStateOf(userProfile?.name ?: "") }
    var email by remember(userProfile) { mutableStateOf(userProfile?.email ?: "") }
    var education by remember(userProfile) { mutableStateOf(userProfile?.education ?: "") }
    var skills by remember(userProfile) { mutableStateOf(userProfile?.skills ?: "") }
    var experience by remember(userProfile) { mutableStateOf(userProfile?.experience ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // User Profile Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Your Profile", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = education, onValueChange = { education = it }, label = { Text("Education") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = skills, onValueChange = { skills = it }, label = { Text("Skills") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = experience, onValueChange = { experience = it }, label = { Text("Experience/Projects") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                
                Button(
                    onClick = {
                        onProfileSave(UserProfile(name = name, email = email, education = education, skills = skills, experience = experience))
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save Profile")
                }
            }

            HorizontalDivider()

            // Gemini API Key Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Gemini API Configuration", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = onApiKeyChange,
                    label = { Text("API Key") },
                    placeholder = { Text("Enter your Gemini API Key") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Your API key is stored securely using EncryptedSharedPreferences.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // Appearance Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Dark Mode", style = MaterialTheme.typography.titleMedium)
                    Text("Toggle between light and dark themes", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onThemeToggle
                )
            }

            HorizontalDivider()

            // Support Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Support & Feedback", style = MaterialTheme.typography.titleMedium)
                Button(
                    onClick = onFeedbackClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Send Feedback")
                }
            }
        }
    }
}
