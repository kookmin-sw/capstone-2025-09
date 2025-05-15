package kr.ac.kookmin.cs.capstone.voicepack_platform.common.util

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.UUID

@Component
class S3ObjectUploader(
    private val s3Client: S3Client,
    @Value("\${aws.s3.bucket-name}") private val bucketName: String
) {
    private val logger = LoggerFactory.getLogger(S3ObjectUploader::class.java)

    /**
     * 이미지를 S3에 업로드하고 S3 객체 키를 반환합니다.
     * @param file 업로드할 이미지 파일
     * @param baseName 이미지 파일의 기본 이름 (예: voicepack 이름)
     * @return 생성된 S3 객체 키
     * @throws RuntimeException S3 업로드 실패 시
     */
    fun uploadImageToS3(file: MultipartFile, baseName: String, urlType: String): String {
        val fileExtension = file.originalFilename?.substringAfterLast('.', "") ?: ""
        val uniqueFileName = "${baseName}_${UUID.randomUUID()}.$fileExtension"
        // S3에 저장될 경로. 예: images/voicepack_name_uuid.jpg
        val objectKey = "$urlType/$uniqueFileName" 

        logger.info("S3 이미지 업로드 시도: objectKey={}, bucketName={}", objectKey, bucketName)

        try {
            val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(file.contentType) // 파일의 MIME 타입 설정
                .build()

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.inputStream, file.size))
            logger.info("S3 이미지 업로드 성공: objectKey={}", objectKey)
            return objectKey
        } catch (e: Exception) {
            logger.error("S3 이미지 업로드 실패: objectKey={}, error={}", objectKey, e.message, e)
            throw RuntimeException("S3 이미지 업로드에 실패했습니다: ${e.message}", e)
        }
    }
} 