package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request


data class AiAssistantSettingRequest (
    val voicepackId: Long,
    val writingStyle: Int,
    val categories: List<Int>,
)