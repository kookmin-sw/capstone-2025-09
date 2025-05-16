package kr.ac.kookmin.cs.capstone.voicepack_platform.video2voicepack.presignedurl

import java.time.LocalDateTime

data class PresignedUrlResponseDto(
    val putUrl: String,
    val expiresAt: LocalDateTime
)

data class PresignedUrlDetailDto(
    val putUrl: String,
    val getUrl: String,
    val expiresAt: LocalDateTime,
    val createdAt: LocalDateTime
) 