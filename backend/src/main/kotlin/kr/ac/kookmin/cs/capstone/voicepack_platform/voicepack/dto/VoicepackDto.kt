package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto

import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.Voicepack
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.request.VoicepackRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.request.VoicepackRequestStatus
import java.time.OffsetDateTime

data class VoicepackDto(
    val id: Long,
    val name: String,
    val authorId: Long,
    val authorName: String,
    val s3Path: String,
    val createdAt: OffsetDateTime
) {
    companion object {
        fun fromEntity(voicepack: Voicepack): VoicepackDto {
            return VoicepackDto(
                id = voicepack.id,
                name = voicepack.name,
                authorId = voicepack.author.id,
                authorName = voicepack.author.username,
                s3Path = voicepack.s3Path,
                createdAt = voicepack.createdAt
            )
        }
    }
}

data class VoicepackRequestDto(
    val id: Long,
    val name: String,
    val authorId: Long,
    val authorName: String,
    val s3Path: String?,
    val status: String,
    val createdAt: OffsetDateTime,
    val completedAt: OffsetDateTime?
) {
    companion object {
        fun fromEntity(request: VoicepackRequest): VoicepackRequestDto {
            return VoicepackRequestDto(
                id = request.id,
                name = request.name,
                authorId = request.author.id,
                authorName = request.author.username,
                s3Path = request.s3Path,
                status = request.status.name,
                createdAt = request.createdAt,
                completedAt = request.completedAt
            )
        }
    }
} 