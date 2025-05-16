package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto

import kotlinx.serialization.Serializable
import org.springframework.web.multipart.MultipartFile

@Serializable
data class VoicepackSynthesisRequest(
    val voicepackId: Long,
    val emotionIndex: Int,
    val prompt: String
)

@Serializable
data class VoicepackSynthesisResponse(
    // S3 Presigned URL
    val synthesis_result: String 
)

@Serializable
data class VoicepackSynthesisAIModelRequest(
    val userId: Long,
    val voicepackId: String,
    val prompt: String,
    val emotionIndex: Int
)

@Serializable
data class VoicepackSynthesisAIModelResponse(
    val audio_url: String,
    val duration: Double
)