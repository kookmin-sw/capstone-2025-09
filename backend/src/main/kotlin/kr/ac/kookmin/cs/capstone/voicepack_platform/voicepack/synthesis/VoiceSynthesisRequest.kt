package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis

import jakarta.persistence.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.User
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.Voicepack
import java.time.OffsetDateTime

@Entity
@Table(name = "voice_synthesis_requests")
data class VoiceSynthesisRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voicepack_id", nullable = false)
    val voicepack: Voicepack,

    @Column(nullable = false, columnDefinition = "TEXT")
    val prompt: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: SynthesisStatus = SynthesisStatus.PENDING,

    @Column(name = "result_url", nullable = true)
    var resultUrl: String? = null,

    @Column(name = "error_message", nullable = true, columnDefinition = "TEXT")
    var errorMessage: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "emotion_index", nullable = false)
    val emotionIndex: Int
)

enum class SynthesisStatus {
    PENDING,    // 요청 생성됨, Lambda 호출 대기 또는 진행 중
    PROCESSING, // Cloud Run에서 처리 중 (콜백에서 상태 업데이트 시 사용 가능)
    COMPLETED,  // 성공적으로 완료됨
    FAILED      // 처리 실패
} 