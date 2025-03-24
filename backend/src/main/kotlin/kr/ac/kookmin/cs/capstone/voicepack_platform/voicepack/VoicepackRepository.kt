package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VoicepackRepository : JpaRepository<Voicepack, Long> {
    fun existsByNameAndAuthorId(name: String, authorId: Long): Boolean
    fun findByNameAndAuthorId(name: String, authorId: Long): Voicepack?
    fun findByAuthorId(authorId: Long): List<Voicepack>
    fun findByIdAndAuthorId(id: Long, authorId: Long): Voicepack?
} 