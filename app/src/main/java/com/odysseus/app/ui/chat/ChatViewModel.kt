package com.odysseus.app.ui.chat

import androidx.lifecycle.viewModelScope
import com.odysseus.app.data.ChatRepository
import com.odysseus.app.data.remote.ChatMessage
import com.odysseus.app.haptics.HapticManager
import com.odysseus.app.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val hapticManager: HapticManager
) : BaseViewModel<ChatState, ChatIntent, ChatEffect>() {

    private var streamingJob: Job? = null
    private var sessionsJob: Job? = null
    private var messagesJob: Job? = null

    override fun createInitialState(): ChatState = ChatState()

    init {
        setIntent(ChatIntent.LoadInitialData)
    }

    override suspend fun handleIntent(intent: ChatIntent) {
        when (intent) {
            ChatIntent.LoadInitialData -> handleLoadInitialData()
            is ChatIntent.SendMessage -> handleSendMessage(intent.text, intent.imagePath)
            ChatIntent.StopStreaming -> handleStopStreaming()
            is ChatIntent.CopyMessage -> handleCopyMessage(intent.messageId, intent.content)
            is ChatIntent.SelectSession -> handleSelectSession(intent.sessionId)
            ChatIntent.CreateNewSession -> handleCreateNewSession()
            is ChatIntent.DeleteSession -> handleDeleteSession(intent.sessionId)
            ChatIntent.ClearHistory -> handleClearHistory()
            is ChatIntent.SelectModel -> handleSelectModel(intent.model)
        }
    }

    private fun handleLoadInitialData() {
        // Collect Sessions
        sessionsJob?.cancel()
        sessionsJob = viewModelScope.launch {
            chatRepository.getSessions().collect { sessionEntities ->
                val mappedSessions = sessionEntities.map {
                    Session(it.id, it.title, it.createdAt)
                }
                setState { copy(sessions = mappedSessions) }

                // Auto-select latest session or create one if empty
                if (mappedSessions.isEmpty()) {
                    handleCreateNewSession()
                } else if (state.value.activeSessionId == null) {
                    handleSelectSession(mappedSessions.first().id)
                }
            }
        }

        // Fetch Models
        viewModelScope.launch {
            try {
                val models = chatRepository.fetchModels()
                setState {
                    copy(
                        availableModels = models,
                        selectedModel = if (selectedModel.isBlank() && models.isNotEmpty()) models.first() else selectedModel,
                        error = null
                    )
                }
            } catch (e: Exception) {
                // Fallback to local default models if server is offline
                val defaults = listOf("odysseus-llama3", "odysseus-mistral", "odysseus-phi3")
                setState {
                    copy(
                        availableModels = defaults,
                        selectedModel = if (selectedModel.isBlank()) defaults.first() else selectedModel,
                        error = "Server offline. Using default models."
                    )
                }
            }
        }
    }

    private fun handleSelectSession(sessionId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            chatRepository.getMessages(sessionId).collect { entityList ->
                val mappedMessages = entityList.map { entity ->
                    Message(
                        id = entity.id,
                        content = entity.content,
                        isUser = entity.role == "user",
                        timestamp = entity.timestamp,
                        imagePath = entity.localFilePath
                    )
                }
                setState {
                    copy(
                        messages = mappedMessages,
                        activeSessionId = sessionId
                    )
                }
                setEffect(ChatEffect.ScrollToBottom)
            }
        }
    }

    private fun handleCreateNewSession() {
        viewModelScope.launch {
            val newId = UUID.randomUUID().toString()
            chatRepository.createNewSession(newId, "New Conversation")
            handleSelectSession(newId)
        }
    }

    private fun handleDeleteSession(sessionId: String) {
        viewModelScope.launch {
            chatRepository.deleteSession(sessionId)
            if (state.value.activeSessionId == sessionId) {
                setState { copy(activeSessionId = null, messages = emptyList()) }
            }
            setEffect(ChatEffect.ShowToast("Conversation deleted"))
        }
    }

    private fun handleSendMessage(text: String, imagePath: String?) {
        val sessionId = state.value.activeSessionId ?: return
        if (text.isBlank() && imagePath == null) return

        viewModelScope.launch {
            val userMessageId = UUID.randomUUID().toString()
            // Save user message to database
            chatRepository.saveMessage(
                sessionId = sessionId,
                id = userMessageId,
                role = "user",
                content = text,
                localFilePath = imagePath
            )

            hapticManager.vibrateSend()
            setEffect(ChatEffect.ScrollToBottom)

            // Trigger Assistant Stream
            startResponseStreaming(sessionId, text)
        }
    }

    private fun startResponseStreaming(sessionId: String, lastUserQuery: String) {
        streamingJob?.cancel()
        setState {
            copy(
                isStreaming = true,
                currentStreamedResponse = ""
            )
        }

        streamingJob = viewModelScope.launch {
            hapticManager.vibrateStreamStart()

            try {
                // Prepare conversation context for the model (latest 20 messages for simplicity)
                val chatMessages = state.value.messages.takeLast(20).map { msg ->
                    ChatMessage(
                        role = if (msg.isUser) "user" else "assistant",
                        content = msg.content
                    )
                }

                var fullResponseText = ""
                var tickCounter = 0

                chatRepository.streamChat(
                    model = state.value.selectedModel,
                    messages = chatMessages
                ).collect { chunk ->
                    fullResponseText += chunk
                    setState {
                        copy(currentStreamedResponse = fullResponseText)
                    }

                    // Throttle haptic tick so it doesn't overlap and ruin the feeling
                    if (tickCounter++ % 3 == 0) {
                        hapticManager.vibrateStreamTick()
                    }
                }

                // Stream completed successfully, save response to database
                val assistantMessageId = UUID.randomUUID().toString()
                chatRepository.saveMessage(
                    sessionId = sessionId,
                    id = assistantMessageId,
                    role = "assistant",
                    content = fullResponseText
                )

                setState {
                    copy(
                        isStreaming = false,
                        currentStreamedResponse = ""
                    )
                }
                hapticManager.vibrateStreamFinish()
                setEffect(ChatEffect.ScrollToBottom)

            } catch (e: Exception) {
                setState {
                    copy(
                        isStreaming = false,
                        currentStreamedResponse = "",
                        error = "Streaming failed: ${e.localizedMessage}"
                    )
                }
                hapticManager.vibrateError()
            }
        }
    }

    private fun handleStopStreaming() {
        val sessionId = state.value.activeSessionId ?: return
        if (state.value.isStreaming) {
            streamingJob?.cancel()
            val partiallyGeneratedText = state.value.currentStreamedResponse
            val finalMessageContent = if (partiallyGeneratedText.isNotBlank()) {
                "$partiallyGeneratedText..."
            } else {
                "Generation stopped."
            }

            viewModelScope.launch {
                val assistantMessageId = UUID.randomUUID().toString()
                chatRepository.saveMessage(
                    sessionId = sessionId,
                    id = assistantMessageId,
                    role = "assistant",
                    content = finalMessageContent
                )

                setState {
                    copy(
                        isStreaming = false,
                        currentStreamedResponse = ""
                    )
                }
                hapticManager.vibrateStop()
                setEffect(ChatEffect.ScrollToBottom)
            }
        }
    }

    private fun handleCopyMessage(messageId: String, content: String) {
        hapticManager.vibrateCopy()
        setEffect(ChatEffect.ShowToast("Copied to clipboard!"))
    }

    private fun handleClearHistory() {
        viewModelScope.launch {
            hapticManager.vibrateLongPress()
            chatRepository.clearAllHistory()
            setState {
                copy(
                    messages = emptyList(),
                    sessions = emptyList(),
                    activeSessionId = null,
                    currentStreamedResponse = "",
                    isStreaming = false
                )
            }
            setEffect(ChatEffect.ShowToast("All history cleared"))
        }
    }

    private fun handleSelectModel(model: String) {
        hapticManager.vibrateCopy()
        setState {
            copy(selectedModel = model)
        }
        setEffect(ChatEffect.ShowToast("Switched to model: $model"))
    }

    override fun onCleared() {
        super.onCleared()
        sessionsJob?.cancel()
        messagesJob?.cancel()
        streamingJob?.cancel()
    }
}
