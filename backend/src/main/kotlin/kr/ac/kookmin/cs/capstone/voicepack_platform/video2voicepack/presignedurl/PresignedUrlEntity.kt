package kr.ac.kookmin.cs.capstone.voicepack_platform.video2voicepack.presignedurl

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.Base64

@Entity
@Table(name = "presigned_urls", 
       indexes = [Index(name = "idx_put_url_hash", columnList = "putUrlHash", unique = true)])
class PresignedUrlEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    val putUrl: String,
    
    @Column(nullable = false, length = 64)
    val putUrlHash: String = calculateHash(putUrl),
    
    @Column(nullable = false, columnDefinition = "TEXT")
    val getUrl: String,
    
    @Column(nullable = false)
    val expiresAt: LocalDateTime,
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        // 문자열의 SHA-256 해시를 계산하는 함수
        fun calculateHash(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            return Base64.getEncoder().encodeToString(bytes)
        }
    }
    
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