package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.response

import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums.Categories
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums.WritingStyle

data class AiAssistantSettingResponse(
    val userId: Long,
    val voicepackId: Long,
    val writingStyle: WritingStyle,
    val categories: Set<Categories>
)
