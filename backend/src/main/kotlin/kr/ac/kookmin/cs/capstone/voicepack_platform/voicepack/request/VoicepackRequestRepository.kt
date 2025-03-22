package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.request

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.request.VoicepackRequest

@Repository
interface VoicepackRequestRepository : JpaRepository<VoicepackRequest, Long> {
    fun findByAuthorId(authorId: Long): List<VoicepackRequest>
    fun existsByNameAndAuthorId(name: String, authorId: Long): Boolean
    fun findByIdAndAuthorId(id: Long, authorId: Long): VoicepackRequest?
} 