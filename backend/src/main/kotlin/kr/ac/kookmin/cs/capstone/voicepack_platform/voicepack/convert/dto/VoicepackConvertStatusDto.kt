package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.convert.dto

import kotlinx.serialization.Serializable
import io.swagger.v3.oas.annotations.media.Schema

// 상태 조회 응답 DTO
@Serializable
@Schema(description = "보이스팩 생성 상태 조회 결과 DTO")
data class VoicepackConvertStatusDto(
    @field:Schema(description = "해당 요청으로 생성된 보이스팩 고유 ID")
    val voicepackId: Long?,
    
    @field:Schema(description = "생성 요청의 현재 상태 (PENDING, PROCESSING, COMPLETED, FAILED)")
    val status: String,
) 