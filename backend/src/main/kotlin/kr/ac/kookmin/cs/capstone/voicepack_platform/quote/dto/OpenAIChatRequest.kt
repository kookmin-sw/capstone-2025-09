package kr.ac.kookmin.cs.capstone.voicepack_platform.quote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OpenAIChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val max_tokens: Int = 150, // 응답 최대 길이 제한
    val tools: List<Tool>? = null,
    val tool_choice: String? = null // "none", "auto", or {"type": "function", "function": {"name": "my_function"}}
)

@Serializable
data class ChatMessage(
    val role: String, // "system", "user", "assistant", "tool"
    val content: String?,
    val tool_calls: List<ToolCall>? = null,
    val tool_call_id: String? = null
)

@Serializable
data class Tool(
    val type: String,
    val function: FunctionTool
)

@Serializable
data class FunctionTool(
    val name: String,
    val description: String,
    val parameters: FunctionParameters
)

@Serializable
data class FunctionParameters(
    val type: String, // "object"
    val properties: Map<String, PropertyDetail>,
    val required: List<String>
)

@Serializable
data class PropertyDetail(
    val type: String,
    val description: String? = null
)

@Serializable
data class ToolCall(
    val id: String,
    val type: String, // "function"
    val function: FunctionCallData
)

@Serializable
data class FunctionCallData(
    val name: String,
    val arguments: String // JSON string
) 