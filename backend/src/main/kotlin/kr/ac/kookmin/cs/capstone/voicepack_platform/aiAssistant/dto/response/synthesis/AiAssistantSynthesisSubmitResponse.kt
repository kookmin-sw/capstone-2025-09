package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.response.synthesis

import kotlinx.serialization.Serializable

@Serializable
data class AiAssistantSynthesisSubmitResponse(
    val id: Long?, // 요청 추적을 위한 ID (비동기 요청이 일어나지 않는 경우를 고려하여 ? 추가)
    val message: String,
    val resultUrl: String? = null // 이미 존재하는 음성 파일의 경우 바로 URL 반환
)
