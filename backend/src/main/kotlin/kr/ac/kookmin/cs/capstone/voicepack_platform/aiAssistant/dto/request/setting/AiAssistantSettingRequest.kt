package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.setting


data class AiAssistantSettingRequest (
    val voicepackId: Long,
    val writingStyle: Int,
    val categories: List<Int>,
)