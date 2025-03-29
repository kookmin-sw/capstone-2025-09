package kr.ac.kookmin.cs.capstone.voicepack_platform.domain.voicepack.entity

import jakarta.persistence.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.domain.user.entity.User
import java.time.OffsetDateTime

@Entity
@Table(name = "voicepack_request")
data class VoicepackRequest(
    @Id
    @Column(name = "request_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "name", nullable = false)
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    val author: User,

    @Column(name = "s3_path", nullable = true)
    var s3Path: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: VoicepackRequestStatus = VoicepackRequestStatus.PROCESSING,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "completed_at", nullable = true)
    var completedAt: OffsetDateTime? = null
)

enum class VoicepackRequestStatus {
    PROCESSING,
    COMPLETED,
    FAILED
} 