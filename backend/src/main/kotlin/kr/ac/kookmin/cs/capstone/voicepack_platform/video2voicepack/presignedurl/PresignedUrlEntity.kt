package kr.ac.kookmin.cs.capstone.voicepack_platform.video2voicepack.presignedurl

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "presigned_urls")
class PresignedUrlEntity(
    @Id
    @Column(nullable = false, length = 1024)
    val putUrl: String,
    
    @Column(nullable = false, length = 1024)
    val getUrl: String,
    
    @Column(nullable = false)
    val expiresAt: LocalDateTime,
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDetailDto(): PresignedUrlDetailDto {
        return PresignedUrlDetailDto(
            putUrl = putUrl,
            getUrl = getUrl,
            expiresAt = expiresAt,
            createdAt = createdAt
        )
    }
    
    fun toResponseDto(): PresignedUrlResponseDto {
        return PresignedUrlResponseDto(
            putUrl = putUrl,
            expiresAt = expiresAt
        )
    }
} 