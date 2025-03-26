package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack

import jakarta.persistence.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.User
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
    val createdAt: OffsetDateTime = OffsetDateTime.now()
) 

/**
 * 보이스팩 DTO 클래스
 * s3Path를 제외한 보이스팩 정보를 담는 데이터 클래스입니다.
 */
data class VoicepackDto(
    val id: Long,
    val name: String,
    val author: String,
    val createdAt: OffsetDateTime
) {
    companion object {
        fun fromEntity(voicepack: Voicepack): VoicepackDto {
            return VoicepackDto(
                id = voicepack.id,
                name = voicepack.name,
                author = voicepack.author.email,
                createdAt = voicepack.createdAt
            )
        }
    }
}
