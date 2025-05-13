package kr.ac.kookmin.cs.capstone.voicepack_platform.common.util

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest

@Component
class S3ObjectDeleter(
    private val s3Client: S3Client,
    @Value("\${aws.s3.bucket-name}") private val bucketName: String
) {
    private val logger = LoggerFactory.getLogger(S3ObjectDeleter::class.java)

    fun deleteObject(objectKey: String) {
        try {
            val deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build()
            s3Client.deleteObject(deleteObjectRequest)
            logger.info("S3 객체 삭제 성공: objectKey={}", objectKey)
        } catch (e: Exception) {
            logger.error("S3 객체 삭제 실패: objectKey={}, error={}", objectKey, e.message, e)
            // 필요에 따라 예외를 다시 던지거나 특정 비즈니스 로직을 수행할 수 있습니다.
            // 여기서는 로깅만 하고 넘어갑니다.
        }
    }
} 