package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto

import io.swagger.v3.oas.annotations.media.Schema

data class UpdateVoicepackPublicRequest(
    @Schema(description = "변경할 공개 여부 값", example = "true")
    val isPublic: Boolean
) 