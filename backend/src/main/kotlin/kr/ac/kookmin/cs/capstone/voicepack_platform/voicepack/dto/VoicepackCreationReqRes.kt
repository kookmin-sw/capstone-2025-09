package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto

import kotlinx.serialization.Serializable
import org.springframework.web.multipart.MultipartFile
@Serializable
data class VoicepackConvertRequest(
    val name: String,
    val voiceFile: MultipartFile
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