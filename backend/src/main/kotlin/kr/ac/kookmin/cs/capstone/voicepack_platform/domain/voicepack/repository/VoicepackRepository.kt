package kr.ac.kookmin.cs.capstone.voicepack_platform.domain.voicepack.repository

import kr.ac.kookmin.cs.capstone.voicepack_platform.domain.voicepack.entity.Voicepack
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VoicepackRepository : JpaRepository<Voicepack, Long> {
    fun existsByName(name: String): Boolean
    fun findByAuthorId(authorId: Long): List<Voicepack>
} 