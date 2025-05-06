package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.entity

import jakarta.persistence.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.User
import java.time.LocalDateTime

/**
 * 사용자의 AI 비서 다중 카테고리 음성 합성 요청 전체를 나타내는 엔티티.
 * 하나의 요청은 여러 카테고리에 대한 개별 합성 작업(AiAssistantSynthesisJob)을 포함할 수 있습니다.
 * 이 엔티티는 요청 자체를 식별하고 관련 Job들을 그룹화하는 역할을 합니다.
 */
@Entity
@Table(name = "ai_assistant_synthesis_request")
data class AiAssistantSynthesisRequest(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null, // 기본 키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    /**
     * 이 요청에 포함된 모든 개별 합성 작업(Job) 목록.
     * 사용자가 여러 카테고리를 요청하면 각 카테고리별 Job이 여기에 연결됩니다.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "request_job_association", // 연결 테이블 이름
        joinColumns = [JoinColumn(name = "request_id")], // AiAssistantSynthesisRequest 측 외래 키
        inverseJoinColumns = [JoinColumn(name = "job_id")] // AiAssistantSynthesisJob 측 외래 키
    )
    val jobs: MutableSet<AiAssistantSynthesisJob> = mutableSetOf(),

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(), // 요청 생성 시간

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now() // 마지막 업데이트 시간 (Job 추가/상태 변경 시 업데이트)

) {
    /**
     * 연관된 모든 Job들의 상태를 종합하여 이 요청의 전체 진행 상태를 계산합니다.
     * - 하나라도 실패(FAILURE)한 Job이 있으면 전체 요청은 FAILURE.
     * - 모든 Job이 성공(SUCCESS)했으면 전체 요청은 SUCCESS.
     * - 그 외 (PENDING 또는 PROCESSING 상태의 Job이 하나라도 있으면) 전체 요청은 PROCESSING.
     * @return SynthesisStatus 요청의 전체 상태
     */
    fun getOverallStatus(): SynthesisStatus {
        if (jobs.isEmpty()) return SynthesisStatus.PENDING // Job이 아직 연결되지 않은 초기 상태

        val statuses = jobs.map { it.status }.toSet()

        return when {
            statuses.contains(SynthesisStatus.FAILURE) -> SynthesisStatus.FAILURE
            statuses.all { it == SynthesisStatus.SUCCESS } -> SynthesisStatus.SUCCESS
            else -> SynthesisStatus.PROCESSING // PENDING 또는 PROCESSING이 하나라도 있으면 PROCESSING
        }
    }
}