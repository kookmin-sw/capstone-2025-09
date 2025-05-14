package kr.ac.kookmin.cs.capstone.voicepack_platform.video2voicepack.presignedurl

import kr.ac.kookmin.cs.capstone.voicepack_platform.common.util.S3PresignedUrlGenerator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PresignedUrlService(
    private val presignedUrlRepository: PresignedUrlRepository,
    private val s3PresignedUrlGenerator: S3PresignedUrlGenerator
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun generateAndSavePresignedUrls(expirationInMinutes: Long = 10): String {
        // 객체 키 생성 (랜덤)
        val objectKey = "video2voicepack/${java.util.UUID.randomUUID()}"
        
        // S3 Presigned PUT/GET URL 생성
        val putUrl = s3PresignedUrlGenerator.generatePutPresignedUrl(objectKey, expirationInMinutes)
        val getUrl = s3PresignedUrlGenerator.generatePresignedUrl(objectKey, expirationInMinutes)
        
        // 만료 시간 계산
        val expiresAt = LocalDateTime.now().plusMinutes(expirationInMinutes)
        
        // 엔티티 생성 및 저장
        val presignedUrlEntity = PresignedUrlEntity(
            putUrl = putUrl,
            getUrl = getUrl,
            expiresAt = expiresAt
        )
        
        presignedUrlRepository.save(presignedUrlEntity)
        logger.info("새로운 Presigned URL 생성 완료: putUrl={}, getUrl={}", putUrl, getUrl)
        
        return putUrl
    }
    
    @Transactional(readOnly = true)
    fun getPresignedUrlDetailByPutUrl(putUrl: String): PresignedUrlDetailDto {
        val presignedUrlEntity = presignedUrlRepository.findById(putUrl)
            .orElseThrow { IllegalArgumentException("해당하는 PUT URL로 저장된 Presigned URL이 없습니다: $putUrl") }
            
        // 만료 시간 확인
        if (presignedUrlEntity.expiresAt.isBefore(LocalDateTime.now())) {
            throw IllegalStateException("Presigned URL이 만료되었습니다. putUrl: $putUrl, 만료시간: ${presignedUrlEntity.expiresAt}")
        }
        
        return presignedUrlEntity.toDetailDto()
    }
} 