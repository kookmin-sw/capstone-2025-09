package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.request

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VoicepackRequestRepository : JpaRepository<VoicepackRequest, Long> {
    fun findByAuthorId(authorId: Long): List<VoicepackRequest>
    fun existsByNameAndAuthorId(name: String, authorId: Long): Boolean
} 