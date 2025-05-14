package kr.ac.kookmin.cs.capstone.voicepack_platform.video2voicepack.presignedurl

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PresignedUrlRepository : JpaRepository<PresignedUrlEntity, Long> {
    fun findByPutUrl(putUrl: String): Optional<PresignedUrlEntity>
    fun findByPutUrlHash(putUrlHash: String): Optional<PresignedUrlEntity>
} 