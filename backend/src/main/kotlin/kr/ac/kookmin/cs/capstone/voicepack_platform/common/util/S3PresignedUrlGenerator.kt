package kr.ac.kookmin.cs.capstone.voicepack_platform.common.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.time.Duration

/**
 * S3 Presigned URL을 생성하는 유틸리티 클래스
 */
@Component
class S3PresignedUrlGenerator(
    @Value("\${aws.access-key}") private val accessKey: String,
    @Value("\${aws.secret-key}") private val secretKey: String,
    @Value("\${aws.region}") private val regionName: String,
    @Value("\${aws.s3.bucket-name}") private val bucketName: String
) {

    /**
     * S3 객체에 대한 Presigned URL을 생성합니다.
     *
     * @param objectKey S3 객체 키
     * @param expirationInMinutes URL 만료 시간(분)
     * @return Presigned URL 문자열
     */
    fun generatePresignedUrl(objectKey: String, expirationInMinutes: Long = 10): String {
        val credentials = AwsBasicCredentials.create(accessKey, secretKey)
        val region = Region.of(regionName)

        S3Presigner.builder()
            .region(region)
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build().use { presigner ->
                val getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build()

                val presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationInMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build()

                return presigner.presignGetObject(presignRequest).url().toString()
            }
    }
} 