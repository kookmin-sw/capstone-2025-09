package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack

import jakarta.persistence.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.User
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRight
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.VoiceSynthesisRequest
import java.time.OffsetDateTime

@Entity
@Table(name = "voicepack")
data class Voicepack(
    @Id
    @Column(name = "voicepack_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "name", nullable = false, unique = true)
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    val author: User,

    @Column(name = "s3_path", nullable = false)
    val s3Path: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "price")
    var price: Int? = 1000,

    @Column(name = "is_public", nullable = false)
    var isPublic: Boolean = true,

    @OneToMany(mappedBy = "voicepack", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    val usageRights: MutableList<VoicepackUsageRight> = mutableListOf(),

    @OneToMany(mappedBy = "voicepack", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    val synthesisRequests: MutableList<VoiceSynthesisRequest> = mutableListOf()

) 

/**
 * 보이스팩 DTO 클래스
 * s3Path를 제외한 보이스팩 정보를 담는 데이터 클래스입니다.
 */
data class VoicepackDto(
    val id: Long,
    val name: String,
    val author: String,
    val createdAt: OffsetDateTime,
    val price: Int?,
    val isPublic: Boolean
) {
    companion object {
        fun fromEntity(voicepack: Voicepack): VoicepackDto {
            return VoicepackDto(
                id = voicepack.id,
                name = voicepack.name,
                author = voicepack.author.email,
                createdAt = voicepack.createdAt,
                price = voicepack.price,
                isPublic = voicepack.isPublic
            )
        }
    }
}
