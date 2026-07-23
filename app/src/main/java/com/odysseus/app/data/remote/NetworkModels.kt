package com.odysseus.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = false,
    val temperature: Double? = null
)

@Serializable
data class ChatCompletionResponse(
    val id: String,
    val choices: List<Choice>
)

@Serializable
data class Choice(
    val index: Int,
    val message: ChatMessage? = null,
    val delta: ChatMessage? = null,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class ModelListResponse(
    val data: List<ModelItem>
)

@Serializable
data class ModelItem(
    val id: String,
    @SerialName("object") val objectType: String = "model",
    val created: Long? = null,
    @SerialName("owned_by") val ownedBy: String? = null
)
