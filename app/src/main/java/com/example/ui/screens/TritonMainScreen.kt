package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.data.TritonRepository
import com.example.ui.components.TritonArtifactViewer
import com.example.ui.components.TritonDrawer
import com.example.ui.components.TritonSettingsDialog
import com.example.ui.components.TritonTopBar
import kotlinx.coroutines.launch

@Composable
fun TritonMainScreen(
    repository: TritonRepository,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val sessions by repository.sessions.collectAsState()
    val currentSessionId by repository.currentSessionId.collectAsState()
    val messagesMap by repository.messages.collectAsState()
    val selectedModel by repository.selectedModel.collectAsState()
    val isThinkingEnabled by repository.isThinkingEnabled.collectAsState()
    val activeArtifact by repository.activeArtifact.collectAsState()
    val isGenerating by repository.isGenerating.collectAsState()
    val projects by repository.projects.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }

    val currentMessages = currentSessionId?.let { messagesMap[it] } ?: emptyList()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                TritonDrawer(
                    sessions = sessions,
                    currentSessionId = currentSessionId,
                    projects = projects,
                    onSelectSession = { sessionId ->
                        repository.selectSession(sessionId)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onNewChat = {
                        repository.createNewSession()
                        coroutineScope.launch { drawerState.close() }
                    },
                    onDeleteSession = { sessionId ->
                        repository.deleteSession(sessionId)
                    },
                    onTogglePinSession = { sessionId ->
                        repository.togglePinSession(sessionId)
                    },
                    onOpenSettings = {
                        coroutineScope.launch { drawerState.close() }
                        showSettingsDialog = true
                    }
                )
            }
        },
        modifier = modifier.testTag("triton_main_drawer_container")
    ) {
        Scaffold(
            topBar = {
                TritonTopBar(
                    selectedModel = selectedModel,
                    onModelSelected = { model -> repository.setModel(model) },
                    activeArtifact = activeArtifact,
                    onOpenArtifact = {
                        if (activeArtifact != null) {
                            repository.openArtifact(activeArtifact!!)
                        }
                    },
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    },
                    onNewChat = {
                        repository.createNewSession()
                    },
                    onOpenSettings = {
                        showSettingsDialog = true
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (currentMessages.isEmpty()) {
                    TritonHomeScreen(
                        selectedModel = selectedModel,
                        isThinkingEnabled = isThinkingEnabled,
                        onToggleThinking = { repository.toggleThinking() },
                        promptSuggestions = repository.promptSuggestions,
                        recentSessions = sessions,
                        onSelectSession = { sessionId ->
                            repository.selectSession(sessionId)
                        },
                        onSendMessage = { prompt ->
                            coroutineScope.launch {
                                repository.sendMessage(prompt)
                            }
                        },
                        isGenerating = isGenerating
                    )
                } else {
                    TritonChatScreen(
                        messages = currentMessages,
                        selectedModel = selectedModel,
                        isThinkingEnabled = isThinkingEnabled,
                        onToggleThinking = { repository.toggleThinking() },
                        onSendMessage = { prompt ->
                            coroutineScope.launch {
                                repository.sendMessage(prompt)
                            }
                        },
                        isGenerating = isGenerating,
                        onToggleMessageThinking = { msgId ->
                            repository.toggleMessageThinking(msgId)
                        },
                        onSetMessageFeedback = { msgId, feedback ->
                            repository.setMessageFeedback(msgId, feedback)
                        },
                        onOpenArtifact = { artifact ->
                            repository.openArtifact(artifact)
                        }
                    )
                }

                // Artifact Viewer Modal
                if (activeArtifact != null) {
                    TritonArtifactViewer(
                        artifact = activeArtifact!!,
                        onClose = { repository.closeArtifact() }
                    )
                }

                // Settings Dialog
                if (showSettingsDialog) {
                    TritonSettingsDialog(
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = onToggleDarkTheme,
                        selectedModel = selectedModel,
                        onModelSelected = { repository.setModel(it) },
                        isThinkingEnabled = isThinkingEnabled,
                        onToggleThinking = { repository.toggleThinking() },
                        onDismiss = { showSettingsDialog = false }
                    )
                }
            }
        }
    }
}
