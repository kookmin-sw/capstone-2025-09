package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto

data class VoicepackConvertRequest(
    val packName: String,
    val audioFiles: List<String>, // S3에 임시로 업로드된 오디오 파일들의 경로
    val options: Map<String, Any> // AI 모델에 전달할 옵션들
)

data class VoicepackConvertResponse(
    val id: Long,
    val status: String
)

data class AIModelRequest(
    val voicepackId: Long,
    val audioFiles: List<String>,
    val options: Map<String, Any>
)

data class AIModelResponse(
    val outputPath: String
) 