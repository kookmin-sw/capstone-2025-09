package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto

import kotlinx.serialization.Serializable
import io.swagger.v3.oas.annotations.media.Schema

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

// 상태 조회 응답 DTO
@Serializable
@Schema(description = "음성 합성 상태 조회 결과 DTO")
data class VoicepackSynthesisStatusDto(
    @field:Schema(description = "음성 합성 요청의 고유 Job ID")
    val jobId: String,
    
    @field:Schema(description = "합성 요청의 현재 상태 (PENDING, PROCESSING, COMPLETED, FAILED)")
    val status: String, 
    
    @field:Schema(description = "합성 완료 시 결과 오디오 파일 URL (상태가 COMPLETED일 때 유효)")
    val resultUrl: String? = null, 
    
    @field:Schema(description = "합성 실패 시 오류 메시지 (상태가 FAILED일 때 유효)")
    val errorMessage: String? = null
) 