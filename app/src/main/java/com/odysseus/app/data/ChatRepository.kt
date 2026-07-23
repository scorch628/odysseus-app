package com.odysseus.app.data

import com.odysseus.app.data.local.ChatDao
import com.odysseus.app.data.local.ChatSessionEntity
import com.odysseus.app.data.local.MessageEntity
import com.odysseus.app.data.remote.ChatCompletionRequest
import com.odysseus.app.data.remote.ChatCompletionResponse
import com.odysseus.app.data.remote.ChatMessage
import com.odysseus.app.data.remote.ModelListResponse
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatDao: ChatDao,
    private val settingsRepository: SettingsRepository,
    private val httpClient: HttpClient,
    private val json: Json
) {

    fun getSessions(): Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getMessages(sessionId: String): Flow<List<MessageEntity>> = chatDao.getMessagesForSession(sessionId)

    suspend fun createNewSession(id: String, title: String) {
        chatDao.insertSession(
            ChatSessionEntity(
                id = id,
                title = title,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun saveMessage(sessionId: String, id: String, role: String, content: String, localFilePath: String? = null) {
        chatDao.insertMessage(
            MessageEntity(
                id = id,
                sessionId = sessionId,
                role = role,
                content = content,
                timestamp = System.currentTimeMillis(),
                localFilePath = localFilePath
            )
        )
    }

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteMessagesForSession(sessionId)
        chatDao.deleteSession(sessionId)
    }

    suspend fun clearAllHistory() {
        chatDao.deleteAllMessages()
        chatDao.deleteAllSessions()
    }

    suspend fun fetchModels(): List<String> {
        val baseUrl = settingsRepository.apiUrl.first()
        val apiKey = settingsRepository.apiKey.first()

        val response: ModelListResponse = httpClient.get {
            url("$baseUrl/models")
            if (apiKey.isNotBlank()) {
                header("Authorization", "Bearer $apiKey")
            }
        }.body()

        return response.data.map { it.id }
    }

    fun streamChat(
        model: String,
        messages: List<ChatMessage>
    ): Flow<String> = flow {
        val baseUrl = settingsRepository.apiUrl.first()
        val apiKey = settingsRepository.apiKey.first()

        val request = ChatCompletionRequest(
            model = model,
            messages = messages,
            stream = true
        )

        httpClient.preparePost {
            url("$baseUrl/chat/completions")
            contentType(ContentType.Application.Json)
            if (apiKey.isNotBlank()) {
                header("Authorization", "Bearer $apiKey")
            }
            setBody(request)
        }.execute { response ->
            if (response.status != HttpStatusCode.OK) {
                val errorBody = response.bodyAsText()
                throw Exception("API returned error ${response.status}: $errorBody")
            }

            val channel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                if (line.startsWith("data: ")) {
                    val data = line.substring(6).trim()
                    if (data == "[DONE]") {
                        break
                    }
                    try {
                        val chunk = json.decodeFromString<ChatCompletionResponse>(data)
                        val text = chunk.choices.firstOrNull()?.delta?.content
                        if (!text.isNullOrEmpty()) {
                            emit(text)
                        }
                    } catch (e: Exception) {
                        // Skip parsing errors on partial or invalid chunks
                    }
                }
            }
        }
    }
}
