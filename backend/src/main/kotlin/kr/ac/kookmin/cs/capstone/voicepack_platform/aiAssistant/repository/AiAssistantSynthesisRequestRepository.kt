package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.repository

import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.entity.AiAssistantSynthesisRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository

interface AiAssistantSynthesisRequestRepository : JpaRepository<AiAssistantSynthesisRequest, Long> {
    override fun findById(id: Long): Optional<AiAssistantSynthesisRequest>
}