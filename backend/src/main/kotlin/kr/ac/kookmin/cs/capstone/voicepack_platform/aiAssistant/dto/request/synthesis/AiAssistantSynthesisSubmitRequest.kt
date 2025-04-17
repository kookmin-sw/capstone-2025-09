package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.synthesis

import kotlinx.serialization.Serializable


@Serializable
data class AiAssistantSynthesisSubmitRequest(
    val voicepackId: Long,
)