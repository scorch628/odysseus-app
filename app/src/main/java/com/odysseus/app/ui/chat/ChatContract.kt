package com.odysseus.app.ui.chat

import com.odysseus.app.ui.base.UiIntent
import com.odysseus.app.ui.base.UiSideEffect
import com.odysseus.app.ui.base.UiState

sealed class ChatIntent : UiIntent {
    object LoadInitialData : ChatIntent()
    data class SendMessage(val text: String, val imagePath: String? = null) : ChatIntent()
    object StopStreaming : ChatIntent()
    data class CopyMessage(val messageId: String, val content: String) : ChatIntent()
    data class SelectSession(val sessionId: String) : ChatIntent()
    object CreateNewSession : ChatIntent()
    data class DeleteSession(val sessionId: String) : ChatIntent()
    object ClearHistory : ChatIntent()
    data class SelectModel(val model: String) : ChatIntent()
}

data class Message(
    val id: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imagePath: String? = null
)

data class Session(
    val id: String,
    val title: String,
    val createdAt: Long
)

data class ChatState(
    val sessions: List<Session> = emptyList(),
    val activeSessionId: String? = null,
    val messages: List<Message> = emptyList(),
    val input: String = "",
    val isStreaming: Boolean = false,
    val currentStreamedResponse: String = "",
    val availableModels: List<String> = emptyList(),
    val selectedModel: String = "",
    val error: String? = null
) : UiState

sealed class ChatEffect : UiSideEffect {
    data class ShowToast(val message: String) : ChatEffect()
    object ScrollToBottom : ChatEffect()
}
