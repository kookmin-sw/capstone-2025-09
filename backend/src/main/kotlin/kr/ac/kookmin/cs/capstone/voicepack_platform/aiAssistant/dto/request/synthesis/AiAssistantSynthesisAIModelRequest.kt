package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.synthesis

import kotlinx.serialization.Serializable
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums.Categories
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums.WritingStyle

@Serializable
data class AiAssistantSynthesisAIModelRequest(
    val voicepackId: Long,
    val timestamp: String, // yyyyMMddHH
    val writingStyle: WritingStyle,
    val categories: Set<Categories>
)
