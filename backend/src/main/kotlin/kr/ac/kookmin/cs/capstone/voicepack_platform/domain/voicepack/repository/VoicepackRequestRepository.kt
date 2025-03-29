package kr.ac.kookmin.cs.capstone.voicepack_platform.domain.voicepack.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import kr.ac.kookmin.cs.capstone.voicepack_platform.domain.voicepack.entity.VoicepackRequest

@Repository
interface VoicepackRequestRepository : JpaRepository<VoicepackRequest, Long> {
    fun findByAuthorId(authorId: Long): List<VoicepackRequest>
    fun existsByNameAndAuthorId(name: String, authorId: Long): Boolean
} 