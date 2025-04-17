package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.response.synthesis

import kotlinx.serialization.Serializable

@Serializable
data class AiAssistantSynthesisAIModelResponse(
    val audio_url: String,
    val duration: Double
)
