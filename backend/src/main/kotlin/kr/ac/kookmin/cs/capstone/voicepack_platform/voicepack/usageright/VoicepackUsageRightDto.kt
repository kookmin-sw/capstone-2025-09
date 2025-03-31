package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright

import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRight
import kotlinx.serialization.Serializable

@Serializable
data class VoicepackUsageRightDto(
    val id: Long,
    val userId: Long,
    val voicepackId: Long,
    val voicepackName: String,
    val grantedAt: String
) {
    companion object {
        fun fromEntity(usageRight: VoicepackUsageRight): VoicepackUsageRightDto {
            return VoicepackUsageRightDto(
                id = usageRight.id,
                userId = usageRight.user.id,
                voicepackId = usageRight.voicepack.id,
                voicepackName = usageRight.voicepack.name,
                grantedAt = usageRight.grantedAt.toString()
            )
        }
    }
} 