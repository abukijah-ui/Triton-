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
import com.example.ui.components.TritonAuthDialog
import com.example.ui.components.TritonDrawer
import com.example.ui.components.TritonHistoryDialog
import com.example.ui.components.TritonSettingsDialog
import com.example.ui.components.TritonTopBar
import kotlinx.coroutines.launch

@Composable
fun TritonMainScreen(
    repository: TritonRepository,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    onSetDarkTheme: (Boolean) -> Unit = {},
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
    val liveThinkingState by repository.liveThinkingState.collectAsState()
    val projects by repository.projects.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }

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
                    currentUser = currentUser,
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = onToggleDarkTheme,
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
                    },
                    onOpenHistory = {
                        coroutineScope.launch { drawerState.close() }
                        showHistoryDialog = true
                    },
                    onOpenAuth = {
                        coroutineScope.launch { drawerState.close() }
                        showAuthDialog = true
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
                    },
                    onOpenHistory = {
                        showHistoryDialog = true
                    },
                    onOpenAuth = {
                        showAuthDialog = true
                    },
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = onToggleDarkTheme,
                    currentUser = currentUser
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
                        isGenerating = isGenerating,
                        onSelectModel = { repository.setModel(it) }
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
                        liveThinkingState = liveThinkingState,
                        onToggleLiveThinkingExpanded = { repository.toggleLiveThinkingExpanded() },
                        onToggleMessageThinking = { msgId ->
                            repository.toggleMessageThinking(msgId)
                        },
                        onSetMessageFeedback = { msgId, feedback ->
                            repository.setMessageFeedback(msgId, feedback)
                        },
                        onOpenArtifact = { artifact ->
                            repository.openArtifact(artifact)
                        },
                        onSelectModel = { repository.setModel(it) }
                    )
                }

                // Artifact Viewer Modal
                if (activeArtifact != null) {
                    TritonArtifactViewer(
                        artifact = activeArtifact!!,
                        onClose = { repository.closeArtifact() }
                    )
                }

                // Chat History Dialog
                if (showHistoryDialog) {
                    TritonHistoryDialog(
                        sessions = sessions,
                        currentSessionId = currentSessionId,
                        onSelectSession = { sessionId ->
                            repository.selectSession(sessionId)
                        },
                        onNewChat = {
                            repository.createNewSession()
                        },
                        onDeleteSession = { sessionId ->
                            repository.deleteSession(sessionId)
                        },
                        onTogglePinSession = { sessionId ->
                            repository.togglePinSession(sessionId)
                        },
                        onDismiss = { showHistoryDialog = false }
                    )
                }

                // User Authentication & Profile Dialog
                if (showAuthDialog) {
                    TritonAuthDialog(
                        currentUser = currentUser,
                        onSignUp = { name, email, password ->
                            repository.signUp(name, email, password)
                        },
                        onLogIn = { email, password ->
                            repository.logIn(email, password)
                        },
                        onLogOut = {
                            repository.logOut()
                        },
                        onDismiss = { showAuthDialog = false }
                    )
                }

                // Settings Dialog with Theme Switcher
                if (showSettingsDialog) {
                    TritonSettingsDialog(
                        isDarkTheme = isDarkTheme,
                        onSetDarkTheme = onSetDarkTheme,
                        selectedModel = selectedModel,
                        onModelSelected = { repository.setModel(it) },
                        isThinkingEnabled = isThinkingEnabled,
                        onToggleThinking = { repository.toggleThinking() },
                        currentUser = currentUser,
                        onOpenAuth = { showAuthDialog = true },
                        onDismiss = { showSettingsDialog = false }
                    )
                }
            }
        }
    }
}
