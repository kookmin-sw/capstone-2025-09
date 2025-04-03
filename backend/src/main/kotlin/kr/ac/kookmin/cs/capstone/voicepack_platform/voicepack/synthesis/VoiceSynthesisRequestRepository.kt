package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface VoiceSynthesisRequestRepository : JpaRepository<VoiceSynthesisRequest, Long> {
    // jobId로 요청 찾기 (콜백 처리 시 사용)
    fun findByJobId(jobId: String): Optional<VoiceSynthesisRequest>
} 