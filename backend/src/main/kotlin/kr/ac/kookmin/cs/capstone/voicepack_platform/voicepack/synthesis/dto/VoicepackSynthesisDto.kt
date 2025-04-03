package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto

import kotlinx.serialization.Serializable

// 비동기 합성 요청 시 응답 DTO
@Serializable
data class VoicepackSynthesisSubmitResponse(
    val jobId: String, // 요청 추적을 위한 ID
    val message: String
)

// 콜백 요청 DTO
@Serializable
data class VoicepackCallbackRequest(
    val jobId: String, // 원래 요청의 jobId
    val success: Boolean, // 처리 성공 여부
    val resultUrl: String? = null, // 성공 시 결과 오디오 파일 URL (S3 Presigned 등)
    val errorMessage: String? = null // 실패 시 오류 메시지
) 