package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.convert

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VoicepackConvertRequestRepository : JpaRepository<VoicepackRequest, Long> {
    fun findByAuthorId(authorId: Long): List<VoicepackRequest>
    fun existsByNameAndAuthorId(name: String, authorId: Long): Boolean
} 