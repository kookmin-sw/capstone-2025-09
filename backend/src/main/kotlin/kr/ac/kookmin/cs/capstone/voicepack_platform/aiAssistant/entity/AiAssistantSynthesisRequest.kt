package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.entity

import jakarta.persistence.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums.Categories
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums.WritingStyle
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.User
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.Voicepack
import java.time.OffsetDateTime

@Entity
@Table(name = "ai_assistant_synthesis_request")
data class AiAssistantSynthesisRequest(
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

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열로 DB에 저장
    @Column(nullable = false)
    val categories: Categories,

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열로 DB에 저장
    @Column(nullable = false)
    var writingStyle: WritingStyle, // 사용자가 선택한 문체

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: AiAssistantStatus = AiAssistantStatus.PENDING,

    @Column(name = "result_url", nullable = true)
    var resultUrl: String? = null,

    @Column(name = "error_message", nullable = true, columnDefinition = "TEXT")
    var errorMessage: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)

enum class AiAssistantStatus {
    PENDING,    // 요청 생성됨, Lambda 호출 대기 또는 진행 중
    PROCESSING, // Cloud Run에서 처리 중 (콜백에서 상태 업데이트 시 사용 가능)
    COMPLETED,  // 성공적으로 완료됨
    FAILED      // 처리 실패
}