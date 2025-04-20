package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.repository

import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.entity.AiAssistantSynthesisRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository

interface AiAssistantSynthesisRequestRepository : JpaRepository<AiAssistantSynthesisRequest, Long> {
    fun findByIdOrNull(id: Long): AiAssistantSynthesisRequest?
}