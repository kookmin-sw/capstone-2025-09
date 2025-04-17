package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.callback

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable


@Serializable
@Schema(description = "AI 비서 음성 합성 상태 조회 결과 DTO")
data class AiAssistantStatusDto(
    @field:Schema(description = "AI 비서 음성 합성 요청의 고유 ID")
    val id: Long,

    @field:Schema(description = "AI 비서 음성 합성 요청의 현재 상태 (PENDING, PROCESSING, COMPLETED, FAILED)")
    val status: String,

    @field:Schema(description = "AI 비서 음성 합성 완료 시 결과 오디오 파일 URL (상태가 COMPLETED일 때 유효)")
    val resultUrl: String? = null,

    @field:Schema(description = "AI 비서 음성 합성 실패 시 오류 메시지 (상태가 FAILED일 때 유효)")
    val errorMessage: String? = null
)