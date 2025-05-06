package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.repository

import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.entity.AiAssistantSynthesisJob
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.entity.SynthesisStatus
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums.Categories
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums.WritingStyle
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface AiAssistantSynthesisJobRepository : JpaRepository<AiAssistantSynthesisJob, Long> {
    // 특정 파라미터 조합 및 시간대로 실패하지 않은 최근 Job 검색 (중복 확인용)
    fun findTopByVoicePackIdAndCategoryAndWritingStyleAndPromptTimeSlotAndStatusInOrderByCreatedAtDesc(
        voicePackId: Long,
        category: Categories,
        writingStyle: WritingStyle,
        promptTimeSlot: String,
        statuses: List<SynthesisStatus>
    ): Optional<AiAssistantSynthesisJob>
}