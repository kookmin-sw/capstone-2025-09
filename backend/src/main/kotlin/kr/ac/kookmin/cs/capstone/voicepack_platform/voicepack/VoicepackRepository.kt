package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VoicepackRepository : JpaRepository<Voicepack, Long> {
    fun existsByPackNameAndAuthorId(packName: String, authorId: Long): Boolean
} 