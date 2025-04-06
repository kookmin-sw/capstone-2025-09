package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VoiceSynthesisRequestRepository : JpaRepository<VoiceSynthesisRequest, Long> {} 