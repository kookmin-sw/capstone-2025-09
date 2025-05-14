package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto

import kotlinx.serialization.Serializable
import org.springframework.web.multipart.MultipartFile
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid

@Serializable
data class VoicepackConvertRequest(
    @Schema(description = "보이스팩 이름", example = "내 첫 보이스팩")
    val name: String,
    
    @Schema(description = "영상 기반 여부", example = "false")
    val isVideoBased: Boolean = false,

    @Schema(description = "임시 파일 경로", example = "/tmp/voicepack_request_1234567890.wav")
    val tempFilePath: String? = null,  // video2voicepack에서만 사용

    @Schema(description = "변환할 원본 음성 파일")
    val voiceFile: MultipartFile,

    @Schema(description = "보이스팩 대표 이미지 파일 (선택 사항)")
    val imageFile: MultipartFile? = null,

    @Schema(description = "카테고리 목록 (필수 항목)", example = "[\"상남자\", \"부산 사투리\"]")
    val categories: List<String>
)

@Serializable
data class VoicepackConvertResponse(
    val id: Long,
    val status: String
)

@Serializable
data class AIModelRequest(
    val voicepackId: String,
    val voiceFile: MultipartFile,
    val voicepackRequestId: Long
)