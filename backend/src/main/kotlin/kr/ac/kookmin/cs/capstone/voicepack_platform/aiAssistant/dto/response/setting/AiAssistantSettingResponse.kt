package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.response.setting

import kotlinx.serialization.Serializable
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums.Categories
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums.WritingStyle

@Serializable
data class AiAssistantSettingResponse(
    val userId: Long,
    val voicepackId: Long,
    val writingStyle: WritingStyle,
    val categories: Set<Categories>
)
