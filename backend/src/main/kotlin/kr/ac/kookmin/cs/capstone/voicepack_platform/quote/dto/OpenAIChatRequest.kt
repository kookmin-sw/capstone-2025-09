package kr.ac.kookmin.cs.capstone.voicepack_platform.quote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OpenAIChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val max_tokens: Int = 150 // 응답 최대 길이 제한
)

@Serializable
data class ChatMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
) 