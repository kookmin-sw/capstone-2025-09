package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto

import kotlinx.serialization.Serializable

@Serializable
data class VoicepackConvertRequest(
    val name: String,
    val voiceFile: String
)

@Serializable
data class VoicepackConvertResponse(
    val id: Long,
    val status: String
)

@Serializable
data class AIModelRequest(
    val voicepackId: Long,
    val voiceFile: String
)

@Serializable
data class AIModelResponse(
    val outputPath: String
) 