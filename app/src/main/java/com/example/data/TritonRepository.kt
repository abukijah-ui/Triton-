package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.SecurityUtils
import com.example.data.local.TritonDatabase
import com.example.data.local.UserEntity
import com.example.model.ArtifactType
import com.example.model.ChatMessage
import com.example.model.ChatSession
import com.example.model.MessageFeedback
import com.example.model.PromptSuggestion
import com.example.model.Role
import com.example.model.TritonArtifact
import com.example.model.TritonModel
import com.example.model.TritonProject
import com.example.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class TritonRepository(private val context: Context? = null) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database: TritonDatabase? = context?.let {
        TritonDatabase.getInstance(it)
    }

    private val prefs: SharedPreferences? = context?.getSharedPreferences("triton_auth_prefs", Context.MODE_PRIVATE)

    private val _sessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val sessions: StateFlow<List<ChatSession>> = _sessions.asStateFlow()

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    private val _selectedModel = MutableStateFlow(TritonModel.TRITON_3_7_SONNET)
    val selectedModel: StateFlow<TritonModel> = _selectedModel.asStateFlow()

    private val _isThinkingEnabled = MutableStateFlow(true)
    val isThinkingEnabled: StateFlow<Boolean> = _isThinkingEnabled.asStateFlow()

    private val _activeArtifact = MutableStateFlow<TritonArtifact?>(null)
    val activeArtifact: StateFlow<TritonArtifact?> = _activeArtifact.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _projects = MutableStateFlow<List<TritonProject>>(emptyList())
    val projects: StateFlow<List<TritonProject>> = _projects.asStateFlow()

    // Authentication State
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    val promptSuggestions = listOf(
        PromptSuggestion(
            id = "p1",
            category = "Coding",
            title = "High-performance shader in Kotlin & GLSL",
            promptText = "Write a complete shimmering gold iridescent particle shader in Kotlin with GLSL. Include an interactive visualizer."
        ),
        PromptSuggestion(
            id = "p2",
            category = "Analysis",
            title = "Architectural breakdown of Claude vs Triton",
            promptText = "Provide an in-depth comparison of Claude 3.7 Sonnet hybrid reasoning architecture versus speculative decoding models."
        ),
        PromptSuggestion(
            id = "p3",
            category = "Creative",
            title = "Sci-fi screenplay on the Abyssal Core",
            promptText = "Draft a cinematic, suspenseful screenplay scene about an oceanic AI named Triton awakening at 11,000 meters deep in the Mariana Trench."
        ),
        PromptSuggestion(
            id = "p4",
            category = "Strategy",
            title = "Zero-knowledge proofs in distributed ledger systems",
            promptText = "Explain zk-SNARKs vs zk-STARKs with concrete mathematical proofs, algorithmic complexities, and real-world trade-offs."
        )
    )

    init {
        initProjects()
        initData()
    }

    private fun initProjects() {
        _projects.value = listOf(
            TritonProject(
                id = "proj-1",
                name = "Neptune Engine",
                description = "Deep-sea underwater robotics telemetry & neural navigation",
                instructions = "Respond with high mathematical rigor and nautical terminology.",
                fileCount = 6,
                iconEmoji = "🔱"
            ),
            TritonProject(
                id = "proj-2",
                name = "Aureate Design System",
                description = "Jetpack Compose design tokens, golden palettes, and typography",
                instructions = "Always output clean Kotlin Compose code adhering to M3 standards.",
                fileCount = 14,
                iconEmoji = "✨"
            )
        )
    }

    private fun initData() {
        scope.launch {
            if (database != null) {
                // Initialize default user if none exists
                val userCount = database.userDao().getUserCount()
                if (userCount == 0) {
                    val defaultSalt = SecurityUtils.generateSalt()
                    val defaultHash = SecurityUtils.hashPassword("triton123", defaultSalt)
                    val defaultUser = UserEntity(
                        id = "user_default_1",
                        email = "Abukijah@gmail.com",
                        displayName = "Abukijah",
                        passwordHash = defaultHash,
                        saltHex = SecurityUtils.bytesToHex(defaultSalt),
                        createdAt = System.currentTimeMillis(),
                        tier = "Triton Pro",
                        avatarInitials = "AB"
                    )
                    database.userDao().insertUser(defaultUser)
                }

                // Restore active user from preferences
                val savedUserId = prefs?.getString("active_user_id", "user_default_1") ?: "user_default_1"
                val loggedInEntity = database.userDao().getUserById(savedUserId)
                    ?: database.userDao().getUserByEmail("Abukijah@gmail.com")

                if (loggedInEntity != null) {
                    _currentUser.value = UserProfile(
                        id = loggedInEntity.id,
                        email = loggedInEntity.email,
                        displayName = loggedInEntity.displayName,
                        tier = loggedInEntity.tier,
                        avatarInitials = loggedInEntity.avatarInitials,
                        createdAt = loggedInEntity.createdAt
                    )
                }

                // Check sessions count
                val sessionCount = database.chatDao().getSessionCount()
                if (sessionCount == 0) {
                    seedDefaultSessions(database)
                }

                // Observe sessions reactively from Room
                database.chatDao().getAllSessionsFlow().collect { sessionEntities ->
                    val domainSessions = sessionEntities.map { entity ->
                        val model = TritonModel.entries.find { it.id == entity.modelId }
                            ?: TritonModel.TRITON_3_7_SONNET
                        ChatSession(
                            id = entity.id,
                            title = entity.title,
                            createdAt = entity.createdAt,
                            updatedAt = entity.updatedAt,
                            previewSnippet = entity.previewSnippet,
                            isPinned = entity.isPinned,
                            model = model
                        )
                    }
                    _sessions.value = domainSessions

                    if (_currentSessionId.value == null && domainSessions.isNotEmpty()) {
                        val firstId = domainSessions.first().id
                        _currentSessionId.value = firstId
                        loadMessagesForSession(firstId)
                    } else if (_currentSessionId.value != null) {
                        loadMessagesForSession(_currentSessionId.value!!)
                    }
                }
            } else {
                // In-memory fallback (for unit tests / previews)
                initDefaultInMemoryData()
            }
        }
    }

    private suspend fun seedDefaultSessions(db: TritonDatabase) {
        val sampleSessionId = "sample-session-1"
        val sampleSession = ChatSessionEntity(
            id = sampleSessionId,
            title = "High-Performance Gold Shader",
            createdAt = System.currentTimeMillis() - 3600000 * 2,
            updatedAt = System.currentTimeMillis() - 3600000 * 2,
            previewSnippet = "Here is the shimmering gold iridescent particle pipeline...",
            modelId = TritonModel.TRITON_3_7_SONNET.id,
            isPinned = true,
            userId = "user_default_1"
        )

        val codeContent = """
// Triton Shimmering Gold Particle Pipeline
precision highp float;
uniform vec2 u_resolution;
uniform float u_time;

const vec3 GOLD_ACCENT = vec3(0.85, 0.65, 0.18);
const vec3 GOLD_SPEC   = vec3(0.98, 0.85, 0.45);

void main() {
    vec2 p = (gl_FragCoord.xy * 2.0 - u_resolution) / min(u_resolution.x, u_resolution.y);
    float d = length(p);
    float glow = 0.05 / (d + 0.02);
    vec3 col = GOLD_ACCENT * glow + GOLD_SPEC * pow(glow, 2.5);
    gl_FragColor = vec4(col, 1.0);
}
        """.trimIndent()

        val sampleMessages = listOf(
            ChatMessageEntity(
                id = "msg-1",
                sessionId = sampleSessionId,
                role = "USER",
                content = "Write a complete shimmering gold iridescent particle shader in Kotlin with GLSL. Include an interactive visualizer.",
                timestamp = System.currentTimeMillis() - 3600000 * 2,
                modelName = "Triton 3.7 Sonnet"
            ),
            ChatMessageEntity(
                id = "msg-2",
                sessionId = sampleSessionId,
                role = "ASSISTANT",
                content = """
Here is the **shimmering gold iridescent particle pipeline** designed for real-time mobile execution.

### Architectural Overview
1. **Photometric Accuracy**: Reflectance spectra calibrated to authentic 24k gold refractive indices.
2. **Computational Budget**: Zero branching within the fragment pipeline to guarantee 120 FPS.
3. **Specular Caustics**: Subtle stochastic shimmer computed via analytic pseudorandom frequency synthesis.

```glsl
// Triton Shimmering Gold Particle Pipeline
precision highp float;
uniform vec2 u_resolution;
uniform float u_time;

const vec3 GOLD_ACCENT = vec3(0.85, 0.65, 0.18);
const vec3 GOLD_SPEC   = vec3(0.98, 0.85, 0.45);

void main() {
    vec2 p = (gl_FragCoord.xy * 2.0 - u_resolution) / min(u_resolution.x, u_resolution.y);
    float d = length(p);
    float glow = 0.05 / (d + 0.02);
    vec3 col = GOLD_ACCENT * glow + GOLD_SPEC * pow(glow, 2.5);
    gl_FragColor = vec4(col, 1.0);
}
```

Triton has compiled this component into an interactive **Artifact**. Tap the preview card to view the live rendered output and code inspector.
                """.trimIndent(),
                timestamp = System.currentTimeMillis() - 3600000 * 2 + 4000,
                modelName = "Triton 3.7 Sonnet",
                thoughtProcess = "1. Deconstructed user prompt for real-time GLSL requirements.\n2. Calibrated gold chromaticity and metallic reflectance.\n3. Packaged into a self-contained Triton Artifact.",
                thoughtDurationSec = 3.6,
                isThinkingExpanded = false,
                hasArtifact = true,
                artifactId = "art-sample-1",
                artifactTitle = "Gold Shimmer Particle Shader",
                artifactLanguage = "glsl",
                artifactContent = codeContent,
                artifactType = "CODE",
                artifactSummary = "Real-time mobile auric fragment shader with shimmering caustics"
            )
        )

        val secondSessionId = "sample-session-2"
        val secondSession = ChatSessionEntity(
            id = secondSessionId,
            title = "Zero-Knowledge STARKs Proofs",
            createdAt = System.currentTimeMillis() - 86400000,
            updatedAt = System.currentTimeMillis() - 86400000,
            previewSnippet = "zk-STARKs rely on collision-resistant hash functions...",
            modelId = TritonModel.TRITON_3_5_SONNET.id,
            isPinned = false,
            userId = "user_default_1"
        )

        val secondMessages = listOf(
            ChatMessageEntity(
                id = "msg-201",
                sessionId = secondSessionId,
                role = "USER",
                content = "Explain zk-SNARKs vs zk-STARKs with concrete mathematical proofs.",
                timestamp = System.currentTimeMillis() - 86400000,
                modelName = "Triton 3.5 Sonnet"
            ),
            ChatMessageEntity(
                id = "msg-202",
                sessionId = secondSessionId,
                role = "ASSISTANT",
                content = """
### Fundamental Divergence: SNARKs vs STARKs

The primary cryptographic distinction lies in the **trusted setup** and **underlying hardness assumptions**.

1. **zk-SNARKs** (Succinct Non-Interactive Arguments of Knowledge):
   * Rely on elliptic curve pairings and discrete logarithm problem over finite fields.
   * Require a Common Reference String (CRS) or trusted ceremony (Powers of Tau).
   * Proof sizes are extraordinarily compact (~200 to 300 bytes).

2. **zk-STARKs** (Scalable Transparent Arguments of Knowledge):
   * Rely strictly on collision-resistant hash functions and the FRI protocol (Fast Reed-Solomon Interactive Oracle Proofs of Proximity).
   * **Transparent**: Require no trusted setup whatsoever.
   * Post-quantum secure by design.
                """.trimIndent(),
                timestamp = System.currentTimeMillis() - 86400000 + 3000,
                modelName = "Triton 3.5 Sonnet",
                thoughtProcess = "1. Isolated algebraic geometry differences.\n2. Summarized transparency & quantum hardness guarantees.",
                thoughtDurationSec = 2.4
            )
        )

        db.chatDao().insertAllSessions(listOf(sampleSession, secondSession))
        db.chatDao().insertAllMessages(sampleMessages + secondMessages)
    }

    private suspend fun loadMessagesForSession(sessionId: String) {
        if (database == null) return
        val entities = database.chatDao().getMessagesForSession(sessionId)
        val domainMessages = entities.map { it.toDomain() }
        _messages.value = _messages.value + (sessionId to domainMessages)
    }

    // --- Authentication Operations ---

    suspend fun signUp(name: String, email: String, password: String): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            val cleanEmail = email.trim().lowercase()
            val cleanName = name.trim()

            if (!SecurityUtils.isValidEmail(cleanEmail)) {
                return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address"))
            }

            val (isPwValid, pwError) = SecurityUtils.isValidPassword(password)
            if (!isPwValid) {
                return@withContext Result.failure(IllegalArgumentException(pwError))
            }

            if (database != null) {
                val existing = database.userDao().getUserByEmail(cleanEmail)
                if (existing != null) {
                    return@withContext Result.failure(IllegalStateException("An account with this email already exists"))
                }

                val salt = SecurityUtils.generateSalt()
                val hash = SecurityUtils.hashPassword(password, salt)
                val initials = computeInitials(cleanName)

                val newEntity = UserEntity(
                    id = UUID.randomUUID().toString(),
                    email = cleanEmail,
                    displayName = cleanName,
                    passwordHash = hash,
                    saltHex = SecurityUtils.bytesToHex(salt),
                    createdAt = System.currentTimeMillis(),
                    tier = "Triton Pro",
                    avatarInitials = initials
                )

                database.userDao().insertUser(newEntity)

                val profile = UserProfile(
                    id = newEntity.id,
                    email = newEntity.email,
                    displayName = newEntity.displayName,
                    tier = newEntity.tier,
                    avatarInitials = newEntity.avatarInitials,
                    createdAt = newEntity.createdAt
                )

                prefs?.edit()?.putString("active_user_id", newEntity.id)?.apply()
                _currentUser.value = profile
                Result.success(profile)
            } else {
                val profile = UserProfile(
                    id = UUID.randomUUID().toString(),
                    email = cleanEmail,
                    displayName = cleanName,
                    tier = "Triton Pro",
                    avatarInitials = computeInitials(cleanName)
                )
                _currentUser.value = profile
                Result.success(profile)
            }
        }
    }

    suspend fun logIn(email: String, password: String): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            val cleanEmail = email.trim().lowercase()

            if (!SecurityUtils.isValidEmail(cleanEmail)) {
                return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address"))
            }

            if (database != null) {
                val user = database.userDao().getUserByEmail(cleanEmail)
                    ?: return@withContext Result.failure(IllegalArgumentException("No account found with this email"))

                val salt = SecurityUtils.hexToBytes(user.saltHex)
                val isMatch = SecurityUtils.verifyPassword(password, salt, user.passwordHash)

                if (!isMatch) {
                    return@withContext Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
                }

                val profile = UserProfile(
                    id = user.id,
                    email = user.email,
                    displayName = user.displayName,
                    tier = user.tier,
                    avatarInitials = user.avatarInitials,
                    createdAt = user.createdAt
                )

                prefs?.edit()?.putString("active_user_id", user.id)?.apply()
                _currentUser.value = profile
                Result.success(profile)
            } else {
                val profile = UserProfile(
                    id = "test-user",
                    email = cleanEmail,
                    displayName = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                    tier = "Triton Pro",
                    avatarInitials = "TR"
                )
                _currentUser.value = profile
                Result.success(profile)
            }
        }
    }

    fun logOut() {
        prefs?.edit()?.remove("active_user_id")?.apply()
        _currentUser.value = null
    }

    private fun computeInitials(name: String): String {
        val parts = name.trim().split("\\s+".toRegex())
        return when {
            parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercase() ?: ""}${parts[1].firstOrNull()?.uppercase() ?: ""}"
            parts.size == 1 && parts[0].isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "TR"
        }
    }

    // --- Session & Chat Operations ---

    fun setModel(model: TritonModel) {
        _selectedModel.value = model
    }

    fun toggleThinking() {
        _isThinkingEnabled.value = !_isThinkingEnabled.value
    }

    fun setThinking(enabled: Boolean) {
        _isThinkingEnabled.value = enabled
    }

    fun openArtifact(artifact: TritonArtifact) {
        _activeArtifact.value = artifact
    }

    fun closeArtifact() {
        _activeArtifact.value = null
    }

    fun createNewSession(initialModel: TritonModel = _selectedModel.value): String {
        val newId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val session = ChatSession(
            id = newId,
            title = "New conversation",
            createdAt = now,
            updatedAt = now,
            previewSnippet = "Start typing to begin...",
            model = initialModel
        )

        _sessions.value = listOf(session) + _sessions.value
        _currentSessionId.value = newId
        _messages.value = _messages.value + (newId to emptyList())

        scope.launch {
            database?.chatDao()?.insertOrUpdateSession(
                ChatSessionEntity(
                    id = newId,
                    title = session.title,
                    createdAt = now,
                    updatedAt = now,
                    previewSnippet = session.previewSnippet,
                    modelId = initialModel.id,
                    isPinned = false,
                    userId = _currentUser.value?.id
                )
            )
        }

        return newId
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
        val session = _sessions.value.find { it.id == sessionId }
        if (session != null) {
            _selectedModel.value = session.model
        }
        scope.launch {
            loadMessagesForSession(sessionId)
        }
    }

    fun deleteSession(sessionId: String) {
        _sessions.value = _sessions.value.filterNot { it.id == sessionId }
        _messages.value = _messages.value - sessionId
        if (_currentSessionId.value == sessionId) {
            _currentSessionId.value = _sessions.value.firstOrNull()?.id
            _currentSessionId.value?.let { nextId ->
                selectSession(nextId)
            }
        }
        scope.launch {
            database?.chatDao()?.deleteSession(sessionId)
        }
    }

    fun togglePinSession(sessionId: String) {
        _sessions.value = _sessions.value.map {
            if (it.id == sessionId) it.copy(isPinned = !it.isPinned) else it
        }
        scope.launch {
            database?.chatDao()?.togglePinSession(sessionId)
        }
    }

    fun toggleMessageThinking(messageId: String) {
        val currentId = _currentSessionId.value ?: return
        val currentMsgs = _messages.value[currentId] ?: return
        _messages.value = _messages.value + (currentId to currentMsgs.map {
            if (it.id == messageId) it.copy(isThinkingExpanded = !it.isThinkingExpanded) else it
        })
        scope.launch {
            database?.chatDao()?.toggleThinkingExpanded(messageId)
        }
    }

    fun setMessageFeedback(messageId: String, feedback: MessageFeedback) {
        val currentId = _currentSessionId.value ?: return
        val currentMsgs = _messages.value[currentId] ?: return
        _messages.value = _messages.value + (currentId to currentMsgs.map {
            if (it.id == messageId) it.copy(feedback = feedback) else it
        })
        scope.launch {
            database?.chatDao()?.updateMessageFeedback(messageId, feedback.name)
        }
    }

    suspend fun sendMessage(content: String) {
        if (content.isBlank() || _isGenerating.value) return

        var sessionId = _currentSessionId.value
        if (sessionId == null) {
            sessionId = createNewSession()
        }

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            role = Role.USER,
            content = content.trim(),
            timestamp = System.currentTimeMillis()
        )

        val existingMessages = _messages.value[sessionId] ?: emptyList()
        val updatedWithUser = existingMessages + userMessage
        _messages.value = _messages.value + (sessionId to updatedWithUser)

        val isFirst = existingMessages.isEmpty()
        val generatedTitle = if (content.length > 36) content.take(34) + "..." else content
        val preview = content.take(60)
        val now = System.currentTimeMillis()

        if (isFirst) {
            _sessions.value = _sessions.value.map {
                if (it.id == sessionId) it.copy(
                    title = generatedTitle,
                    previewSnippet = preview,
                    updatedAt = now
                ) else it
            }
        }

        // Persist User Message to Room
        scope.launch {
            database?.let { db ->
                if (isFirst) {
                    val s = _sessions.value.find { it.id == sessionId }
                    if (s != null) {
                        db.chatDao().insertOrUpdateSession(
                            ChatSessionEntity(
                                id = sessionId,
                                title = generatedTitle,
                                createdAt = s.createdAt,
                                updatedAt = now,
                                previewSnippet = preview,
                                modelId = s.model.id,
                                isPinned = s.isPinned,
                                userId = _currentUser.value?.id
                            )
                        )
                    }
                }
                db.chatDao().insertMessage(
                    ChatMessageEntity(
                        id = userMessage.id,
                        sessionId = sessionId,
                        role = "USER",
                        content = userMessage.content,
                        timestamp = userMessage.timestamp,
                        modelName = _selectedModel.value.displayName
                    )
                )
            }
        }

        _isGenerating.value = true

        val model = _selectedModel.value
        val thinkEnabled = _isThinkingEnabled.value && model.supportsThinking

        val thoughtDuration = if (thinkEnabled) (2.5 + Math.random() * 2.0).let { Math.round(it * 10.0) / 10.0 } else null
        val thinkingText = if (thinkEnabled) generateThinkingProcess(content) else null

        if (thinkEnabled) {
            delay(1200)
        } else {
            delay(500)
        }

        val responseTuple = generateTritonResponse(content, model)

        val assistantMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            role = Role.ASSISTANT,
            content = responseTuple.text,
            timestamp = System.currentTimeMillis(),
            thoughtProcess = thinkingText,
            thoughtDurationSec = thoughtDuration,
            isThinkingExpanded = false,
            artifact = responseTuple.artifact,
            modelName = model.displayName
        )

        val updatedWithAssistant = updatedWithUser + assistantMessage
        _messages.value = _messages.value + (sessionId to updatedWithAssistant)

        if (responseTuple.artifact != null) {
            _activeArtifact.value = responseTuple.artifact
        }

        _isGenerating.value = false

        // Persist Assistant Message to Room
        scope.launch {
            database?.chatDao()?.insertMessage(
                ChatMessageEntity(
                    id = assistantMessage.id,
                    sessionId = sessionId,
                    role = "ASSISTANT",
                    content = assistantMessage.content,
                    timestamp = assistantMessage.timestamp,
                    modelName = assistantMessage.modelName,
                    thoughtProcess = assistantMessage.thoughtProcess,
                    thoughtDurationSec = assistantMessage.thoughtDurationSec,
                    isThinkingExpanded = assistantMessage.isThinkingExpanded,
                    hasArtifact = assistantMessage.artifact != null,
                    artifactId = assistantMessage.artifact?.id,
                    artifactTitle = assistantMessage.artifact?.title,
                    artifactLanguage = assistantMessage.artifact?.language,
                    artifactContent = assistantMessage.artifact?.content,
                    artifactType = assistantMessage.artifact?.type?.name,
                    artifactSummary = assistantMessage.artifact?.summary,
                    feedback = assistantMessage.feedback.name
                )
            )
        }
    }

    private fun generateThinkingProcess(prompt: String): String {
        return buildString {
            append("1. **Deconstruct User Intent**: Analyzed prompt \"$prompt\" for fundamental technical requirements and structural boundaries.\n")
            append("2. **Evaluate Architecture**: Checked constraint space for algorithmic elegance, memory bounds, and computational efficiency.\n")
            append("3. **Formulate Shimmering Synthetics**: Selected bespoke mathematical abstractions and gold-standard implementation patterns.\n")
            append("4. **Synthesize Artifact & Verification**: Verified code syntax and state consistency before output generation.")
        }
    }

    private suspend fun generateTritonResponse(prompt: String, model: TritonModel): ResponseTuple {
        if (GeminiService.isApiKeyConfigured()) {
            val systemInstruction = """
                You are Triton, a world-class premier AI intelligence inspired by Claude.
                You are regal, deeply analytical, concise, yet eloquently expressive.
                Use Markdown with headers, bold emphasis, and formatted code blocks.
                When appropriate, create modular, production-ready code.
            """.trimIndent()

            val result = GeminiService.generateContent(
                prompt = prompt,
                systemInstruction = systemInstruction,
                modelName = "gemini-3.5-flash"
            )

            if (result.isSuccess) {
                val text = result.getOrNull() ?: ""
                val artifact = extractOrGenerateArtifact(prompt, text)
                return ResponseTuple(text, artifact)
            }
        }

        return synthesizeNativeTritonResponse(prompt, model)
    }

    private fun extractOrGenerateArtifact(prompt: String, responseText: String): TritonArtifact? {
        val lower = prompt.lowercase()
        if (lower.contains("code") || lower.contains("shader") || lower.contains("app") || lower.contains("visualizer") || lower.contains("artifact")) {
            val codeRegex = Regex("```([a-zA-Z0-9_-]+)?\\s*([\\s\\S]*?)```")
            val match = codeRegex.find(responseText)
            if (match != null) {
                val lang = match.groupValues[1].ifBlank { "kotlin" }
                val code = match.groupValues[2].trim()
                return TritonArtifact(
                    id = UUID.randomUUID().toString(),
                    title = "Triton Synthesized Component",
                    type = ArtifactType.CODE,
                    language = lang,
                    content = code,
                    summary = "Self-contained executable artifact synthesized for $lang"
                )
            }
        }
        return null
    }

    private fun synthesizeNativeTritonResponse(prompt: String, model: TritonModel): ResponseTuple {
        val lower = prompt.lowercase()

        if (lower.contains("shader") || lower.contains("particle") || lower.contains("glsl") || lower.contains("gold")) {
            val code = """
// Triton Shimmering Gold Particle Vertex & Fragment Pipeline
precision highp float;

uniform vec2 u_resolution;
uniform float u_time;
uniform vec2 u_mouse;

const vec3 GOLD_SPECULAR = vec3(0.98, 0.82, 0.42);
const vec3 GOLD_DIFFUSE  = vec3(0.85, 0.65, 0.18);
const vec3 OBSIDIAN_VOID = vec3(0.08, 0.08, 0.10);

float goldNoise(vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453123);
}

void main() {
    vec2 uv = (gl_FragCoord.xy * 2.0 - u_resolution.xy) / min(u_resolution.x, u_resolution.y);
    float dist = length(uv);
    
    float wave = sin(dist * 12.0 - u_time * 2.4) * 0.5 + 0.5;
    float shimmer = pow(wave, 4.0) * goldNoise(uv + u_time * 0.05);
    
    vec3 col = mix(OBSIDIAN_VOID, GOLD_DIFFUSE, smoothstep(0.8, 0.1, dist));
    col += GOLD_SPECULAR * shimmer * 1.4;
    
    gl_FragColor = vec4(col, 1.0);
}
            """.trimIndent()

            val artifact = TritonArtifact(
                id = UUID.randomUUID().toString(),
                title = "Gold Shimmer Particle Shader",
                type = ArtifactType.CODE,
                language = "glsl",
                content = code,
                summary = "High-precision auric fragment shader with turbulent iridescent lighting"
            )

            val text = """
Here is the **shimmering gold iridescent particle pipeline** designed for real-time mobile execution.

### Architectural Overview
1. **Photometric Accuracy**: Reflectance spectra calibrated to authentic 24k gold refractive indices (n ≈ 0.18, k ≈ 3.42).
2. **Computational Budget**: Zero branching within the innermost ray-march loop to guarantee steady 120 FPS on Vulkan/OpenGL ES 3.1.
3. **Specular Caustics**: Subtle stochastic shimmer computed via analytic pseudorandom frequency synthesis.

```glsl
$code
```

Triton has compiled this component into an interactive **Artifact**. You can inspect the source code, copy snippets, or preview the dynamic render state using the artifact panel above.
            """.trimIndent()

            return ResponseTuple(text, artifact)
        } else if (lower.contains("claude") || lower.contains("triton") || lower.contains("comparison") || lower.contains("reasoning")) {
            val text = """
### Comparative Synthesis: Triton vs. Claude 3.7 Architecture

The modern paradigm of large multimodal models relies heavily on **hybrid test-time compute**—dynamically balancing token budget between internal reasoning trajectories and direct generation tokens.

#### 1. Test-Time Compute Allocation
- **Claude 3.7 Sonnet**: Introduces hybrid reasoning that unifies instant conversational fluidity with an adaptive thinking budget (0 to 128k tokens).
- **Triton 3.7 Sonnet**: Employs an auric dual-phase pipeline. The *Cognitive Prelude* decomposes structural constraints, while the *Resonance Engine* emits deterministic, crystal-clear output without recursive hallucination.

| Metric | Triton 3.7 Sonnet | Claude 3.7 Sonnet | Standard LLMs |
| :--- | :--- | :--- | :--- |
| **Reasoning Mode** | Adaptive Golden Chain | Hybrid Extended Thinking | Fixed / CoT |
| **Artifact Generation** | Dual Code/Preview | Dedicated Workspace | Plain Markdown |
| **Aesthetic Tone** | Regal, Statuesque | Warm, Scholarly | Generic |

#### 2. Key Insights for Developers
When leveraging Triton's extended thinking:
* **Deep Architectural Refactoring**: Provide the entire interface contract and allow Triton's reasoning steps to explore edge cases before generating the drop-in class.
* **Complex Proofs**: Triton exposes its intermediate deduction steps, allowing full auditability of each logical inference.
            """.trimIndent()
            return ResponseTuple(text, null)
        } else {
            val text = """
### Triton Response

I have analyzed your query with **${model.displayName}**.

Here is the structured solution tailored to your specifications:

1. **Strategic Foundation**: We isolate the essential invariants to ensure modularity and maximum resilience under high concurrency.
2. **Implementation Patterns**: Prioritizing zero-allocation datatypes and clean separation between domain logic and presentation layers.
3. **Verification**: Every pipeline stage guarantees fault isolation and graceful degradation.

```kotlin
// Example Triton Production Pipeline
data class TritonExecutionState<out T>(
    val isEvaluating: Boolean = false,
    val data: T? = null,
    val auricSignal: Float = 1.0f
)
```

Is there a specific constraint or component you would like to explore deeper?
            """.trimIndent()
            return ResponseTuple(text, null)
        }
    }

    private fun initDefaultInMemoryData() {
        val sampleSessionId = "sample-session-1"
        val sampleSession = ChatSession(
            id = sampleSessionId,
            title = "High-Performance Gold Shader",
            createdAt = System.currentTimeMillis() - 3600000 * 2,
            updatedAt = System.currentTimeMillis() - 3600000 * 2,
            previewSnippet = "Here is the shimmering gold iridescent particle pipeline...",
            isPinned = true,
            model = TritonModel.TRITON_3_7_SONNET
        )
        _sessions.value = listOf(sampleSession)
        _currentSessionId.value = sampleSessionId
    }

    private fun ChatMessageEntity.toDomain(): ChatMessage {
        val artifact = if (hasArtifact && artifactId != null) {
            TritonArtifact(
                id = artifactId,
                title = artifactTitle ?: "Artifact",
                type = ArtifactType.entries.find { it.name == artifactType } ?: ArtifactType.CODE,
                language = artifactLanguage ?: "kotlin",
                content = artifactContent ?: "",
                summary = artifactSummary ?: ""
            )
        } else null

        val fb = MessageFeedback.entries.find { it.name == feedback } ?: MessageFeedback.NONE

        return ChatMessage(
            id = id,
            sessionId = sessionId,
            role = if (role == "USER") Role.USER else Role.ASSISTANT,
            content = content,
            timestamp = timestamp,
            thoughtProcess = thoughtProcess,
            thoughtDurationSec = thoughtDurationSec,
            isThinkingExpanded = isThinkingExpanded,
            artifact = artifact,
            modelName = modelName,
            feedback = fb
        )
    }

    private data class ResponseTuple(
        val text: String,
        val artifact: TritonArtifact?
    )
}
