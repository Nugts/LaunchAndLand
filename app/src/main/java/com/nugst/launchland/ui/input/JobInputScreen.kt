package com.nugst.launchland.ui.input

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobInputScreen(
    onStartAnalysis: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    var jobUrl by remember { mutableStateOf("") }
    var manualDescription by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Job Analysis") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp) {
                Button(
                    onClick = {
                        val input = jobUrl.ifBlank { manualDescription }
                        if (input.isNotBlank()) {
                            onStartAnalysis(input)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = jobUrl.isNotBlank() || manualDescription.isNotBlank()
                ) {
                    Text("Start AI Analysis")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Paste a job link or description to begin.",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = jobUrl,
                onValueChange = { jobUrl = it },
                label = { Text("Job Application URL") },
                placeholder = { Text("LinkedIn, Indeed, etc.") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("OR", style = MaterialTheme.typography.labelLarge)

            OutlinedTextField(
                value = manualDescription,
                onValueChange = { manualDescription = it },
                label = { Text("Paste Job Description") },
                placeholder = { Text("Copy and paste the full job text here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 250.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
