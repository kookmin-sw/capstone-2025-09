package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.convert

import jakarta.persistence.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.User
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
    var status: VoicepackRequestStatus = VoicepackRequestStatus.PENDING,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "completed_at", nullable = true)
    var completedAt: OffsetDateTime? = null
)

enum class VoicepackRequestStatus {
    PENDING,    // 요청 생성됨, Lambda 호출 대기 또는 진행 중
    PROCESSING, // Cloud Run에서 처리 중 (콜백에서 상태 업데이트 시 사용 가능)
    COMPLETED,  // 성공적으로 완료됨
    FAILED      // 처리 실패
} 