package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.entity

import jakarta.persistence.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums.Categories
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums.WritingStyle
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.Voicepack
import java.time.LocalDateTime

/**
 * 개별 AI 비서 음성 합성 작업을 나타내는 엔티티.
 * 동일한 파라미터(보이스팩, 카테고리, 글쓰기 스타일, 시간대)를 가진 중복 요청을 관리하고,
 * 각 작업의 진행 상태를 추적하는 데 사용됩니다.
 */
@Entity
@Table(name = "ai_assistant_synthesis_job")
data class AiAssistantSynthesisJob(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voicepack_id", nullable = false)
    val voicePack: Voicepack,

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    val category: Categories,

    @Enumerated(EnumType.STRING)
    @Column(name = "writing_style", nullable = false)
    val writingStyle: WritingStyle,

    @Column(name = "prompt_s3_key", nullable = false)
    var promptS3Key: String,

    @Column(name = "prompt_time_slot", nullable = false)
    var promptTimeSlot: String,

    @Column(name = "result_s3_key", nullable = false)
    var resultS3Key: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: SynthesisStatus = SynthesisStatus.PENDING,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

)

/**
 * AI 비서 음성 합성 작업의 상태를 정의하는 Enum.
 */
enum class SynthesisStatus {
    PENDING,    // 작업 대기 중 (MQ 발행 전 또는 발행 직후)
    PROCESSING, // 작업 처리 중 (모델에서 처리 중)
    SUCCESS,    // 작업 성공
    FAILURE     // 작업 실패
} 