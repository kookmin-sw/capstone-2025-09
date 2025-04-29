package kr.ac.kookmin.cs.capstone.voicepack_platform.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient

@Configuration
class AwsConfig {
    @Value("\${aws.region}")
    private lateinit var region: String
    
    @Value("\${aws.s3.access-key}")
    private lateinit var accessKey: String
    
    @Value("\${aws.s3.secret-key}")
    private lateinit var secretKey: String

    @Bean
    fun sqsClient(): SqsClient {
        val credentials = AwsBasicCredentials.create(accessKey, secretKey)
        
        return SqsClient.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .httpClient(UrlConnectionHttpClient.builder().build())
            .build()
    }
} 