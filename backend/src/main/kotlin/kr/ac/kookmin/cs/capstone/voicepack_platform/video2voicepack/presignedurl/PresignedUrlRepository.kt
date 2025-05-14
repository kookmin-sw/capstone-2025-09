package kr.ac.kookmin.cs.capstone.voicepack_platform.video2voicepack.presignedurl

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PresignedUrlRepository : JpaRepository<PresignedUrlEntity, String> 