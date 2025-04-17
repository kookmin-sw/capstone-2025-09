package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.callback

import kotlinx.serialization.Serializable

@Serializable
data class AiAssistantCallbackRequest(
    val id: Long, // 원래 요청의 id
    val success: Boolean, // 처리 성공 여부
    val resultUrl: String? = null, // 성공 시 결과 오디오 파일 URL
    val errorMessage: String? = null // 실패 시 오류 메시지
)