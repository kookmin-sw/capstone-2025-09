package kr.ac.kookmin.cs.capstone.voicepack_platform.domain.voicepack.service

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.FormDataContent
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.LogLevel
import kr.ac.kookmin.cs.capstone.voicepack_platform.domain.notification.NotificationService
import kr.ac.kookmin.cs.capstone.voicepack_platform.domain.user.repository.UserRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.domain.user.entity.User
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.domain.voicepack.entity.VoicepackRequestStatus
import kr.ac.kookmin.cs.capstone.voicepack_platform.domain.voicepack.entity.VoicepackRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.domain.voicepack.repository.VoicepackRequestRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.OffsetDateTime
import kr.ac.kookmin.cs.capstone.voicepack_platform.common.util.S3PresignedUrlGenerator
import kr.ac.kookmin.cs.capstone.voicepack_platform.domain.voicepack.dto.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.domain.voicepack.repository.VoicepackRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.domain.voicepack.entity.Voicepack
import kr.ac.kookmin.cs.capstone.voicepack_platform.domain.voicepack.entity.VoicepackDto

@Service
class VoicepackService(
    private val voicepackRepository: VoicepackRepository,
    private val voicepackRequestRepository: VoicepackRequestRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService,
    @Value("\${aws.lambda.endpoint}") private val lambdaEndpoint: String,
    @Value("\${ai.model.service.voicepack_synthesis}") private val voicepackSynthesisEndpoint: String,
    private val s3PresignedUrlGenerator: S3PresignedUrlGenerator
) {
    
    private val httpClient = HttpClient(Java) {
        install(ContentNegotiation) { json() }
        
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
    }
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * 보이스팩 변환 요청 및 처리
     * =========== START ===========
     */
    @Transactional
    suspend fun convertVoicepack(
            userId: Long,
            request: VoicepackConvertRequest
    ): VoicepackConvertResponse {
        logger.info("보이스팩 변환 요청: userId={}, request={}", userId, request)
        val user = findUser(userId)

        // 패키지 이름 유효성 검사
        validatePackName(request.name)
        
        // 진행 중인 요청이 있는지 확인
        checkOngoingRequests(userId)

        // 보이스팩 요청 엔티티 생성
        val voicepackRequest = createVoicepackRequest(user, request.name)
        try {
            // AI 모델 서비스 호출 및 결과 처리
            callAiModelService(voicepackRequest, request.voiceFile)
            
            // 변환 성공 시 처리
            handleSuccessfulConversion(voicepackRequest)
            return VoicepackConvertResponse(voicepackRequest.id, VoicepackRequestStatus.COMPLETED.name)
            
        } catch (e: Exception) {
            // 실패 시 처리
            handleFailedConversion(voicepackRequest, e)       
            return VoicepackConvertResponse(voicepackRequest.id, VoicepackRequestStatus.FAILED.name)
        }
    }
    

    // 보이스팩 요청 엔티티 생성
    private fun createVoicepackRequest(user: User, name: String): VoicepackRequest {
        val voicepackRequest = VoicepackRequest(
            name = name,
            author = user,
            status = VoicepackRequestStatus.PROCESSING
        )
        return voicepackRequestRepository.save(voicepackRequest)
    }

    // AI 모델 서비스 호출
    private suspend fun callAiModelService(voicepackRequest: VoicepackRequest, voiceFile: MultipartFile) {
        val aiModelRequest = AIModelRequest(
            voicepackId = voicepackRequest.id,
            voiceFile = voiceFile
        )

        logger.info("AI 모델 요청: requestId={}, request={}", voicepackRequest.id, aiModelRequest)

        try {
            val response = httpClient.post(lambdaEndpoint) {
                contentType(ContentType.MultiPart.FormData)
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("voicepackId", aiModelRequest.voicepackId)
                            append("voiceFile", aiModelRequest.voiceFile.bytes, Headers.build {
                                append(HttpHeaders.ContentDisposition, "form-data; name=\"voiceFile\"; filename=\"audio.wav\"")
                                append(HttpHeaders.ContentType, "audio/wav") // 필요 시 파일 확장자 변경 가능
                            })
                        }
                    )
                )
            }

            // Lambda가 202 Accepted를 반환하면 성공으로 간주
            if (response.status == HttpStatusCode.Accepted) {
                logger.info("AI 모델 응답: requestId={}, response={}", voicepackRequest.id, response.body<String>())
            } else {
                val errorBody = response.body<String>()
                logger.error("AI 모델 서비스 오류: HTTP ${response.status.value}, 응답: $errorBody")
                throw RuntimeException("AI 모델 서비스 호출 실패: HTTP ${response.status.value}, 응답: $errorBody")
            }
        } catch (e: Exception) {
            logger.error("AI 모델 서비스 호출 중 예외 발생: ${e.message}", e)
            throw RuntimeException("AI 모델 서비스 호출 중 오류 발생: ${e.message}", e)
        }
    }

    // 변환 성공 시 처리
    @Transactional
    private fun handleSuccessfulConversion(voicepackRequest: VoicepackRequest) {
        
        val outputPath = "speakers/${voicepackRequest.name}/feature.json"
        val finishedTime = OffsetDateTime.now(); // 완료 시각 일관성 유지
        
        // 요청 상태 업데이트
        voicepackRequest.status = VoicepackRequestStatus.COMPLETED
        voicepackRequest.s3Path = outputPath
        voicepackRequest.completedAt = finishedTime
        voicepackRequestRepository.save(voicepackRequest)
        
        // 완성된 보이스팩 생성
        val voicepack = Voicepack(
            name = voicepackRequest.name,
            author = voicepackRequest.author,
            s3Path = outputPath,
            createdAt = finishedTime
        )
        voicepackRepository.save(voicepack)
        
        // 알림 전송
        notificationService.notifyVoicepackComplete(voicepackRequest)
    }

    // 변환 실패 시 처리
    @Transactional
    private fun handleFailedConversion(voicepackRequest: VoicepackRequest, exception: Exception) {
        logger.error("AI 모델 처리 실패: requestId={}, error={}", voicepackRequest.id, exception.message)
        
        // 실패 상태로 업데이트
        voicepackRequest.status = VoicepackRequestStatus.FAILED
        voicepackRequest.completedAt = OffsetDateTime.now()
        voicepackRequestRepository.save(voicepackRequest)
        
        // 알림 전송
        notificationService.notifyVoicepackFailed(voicepackRequest)
    }

    private fun findUser(userId: Long) =
            userRepository.findById(userId).orElseThrow {
                IllegalArgumentException("User not found")
            }


    /**
     * 유효성 검사
     */


    // 패키지 이름 유효성 검사
    private fun validatePackName(packName: String) {
        if (voicepackRepository.existsByName(packName)) {
            logger.error("이미 같은 이름의 보이스팩이 존재합니다: packName={}", packName)
            throw IllegalArgumentException("이미 같은 이름의 보이스팩이 존재합니다")
        }
    }

    // 진행 중인 요청이 있는지 확인
    private fun checkOngoingRequests(userId: Long) {
        val existingRequests = voicepackRequestRepository.findByAuthorId(userId)
            val hasOngoingRequest = existingRequests.any { req ->
                req.status == VoicepackRequestStatus.PROCESSING
            }
            
            if (hasOngoingRequest) {
                logger.warn("이미 진행 중인 보이스팩 변환 요청이 있습니다: userId={}", userId)
                throw IllegalStateException("이미 진행 중인 보이스팩 변환 요청이 있습니다")
            }
        }


    /**
     * 보이스팩 변환 요청 및 처리
     * =========== END ===========
     */

    /**
     * 보이스팩 합성 요청 및 처리
     * =========== START ===========
     */
    
    @Transactional
    suspend fun synthesisVoicepack(userId: Long, request: VoicepackSynthesisRequest): VoicepackSynthesisResponse {
        logger.info("보이스팩 합성 요청 시작: userId={}, request={}", userId, request)  
        
        logger.info("사용자 정보 조회 중...")
        findUser(userId)
        
        logger.info("보이스팩 정보 조회 중: voicepackId={}", request.voicepackId)
        val voicepack = findVoicepack(request.voicepackId)
        // TODO: 보유한 보이스팩인지 확인
        
        logger.info("AI 모델 서비스 요청 준비 중...")
        val aiModelRequest = VoicepackSynthesisAIModelRequest(
            userId = userId,
            voicepackId = voicepack.name,
            prompt = request.prompt
        )

        logger.info("AI 모델 서비스 호출 중: endpoint={}", voicepackSynthesisEndpoint)
        val response = httpClient.post(voicepackSynthesisEndpoint) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(FormDataContent(Parameters.build {
                append("userId", aiModelRequest.userId.toString())
                append("voicepackId", aiModelRequest.voicepackId)
                append("prompt", aiModelRequest.prompt)
            }))
        }.body<VoicepackSynthesisAIModelResponse>()
        logger.info("AI 모델 서비스 응답 수신 완료: {}", response)

        logger.info("S3 객체 키 생성 중...")
        val s3ObjectKey = try {
            val uri = java.net.URI(response.audio_url)
            val path = uri.path
            if (path.startsWith("/")) path.substring(1) else path
        } catch (e: Exception) {
            logger.error("S3 URL 파싱 실패: {}", response.audio_url, e)
            throw IllegalStateException("잘못된 S3 URL 형식입니다: ${response.audio_url}")
        }
        
        logger.info("S3 Presigned URL 생성 중: objectKey={}", s3ObjectKey)
        val presignedUrl = s3PresignedUrlGenerator.generatePresignedUrl(s3ObjectKey)
        
        logger.info("보이스팩 합성 요청 완료")
        return VoicepackSynthesisResponse(
            synthesis_result = presignedUrl
        )
    }

    private fun findVoicepack(voicepackId: Long) =
        voicepackRepository.findById(voicepackId).orElseThrow {
            IllegalArgumentException("Voicepack not found")
        }

    /**
     * 보이스팩 목록 조회
     */
    fun getVoicepacks(userId: Long?): List<VoicepackDto> {
        logger.info("보이스팩 목록 조회: userId={}", userId)
        
        val voicepacks = if (userId != null) {
            // 특정 사용자의 보이스팩만 조회
            voicepackRepository.findByAuthorId(userId)
        } else {
            // 모든 보이스팩 조회
            voicepackRepository.findAll()
        }
        
        return voicepacks.map { VoicepackDto.fromEntity(it) }
    }

    // 보이스팩 1개만 조회
    fun getVoicepack(voicepackId: Long): VoicepackDto {
        val voicepack = findVoicepack(voicepackId)
        if (voicepack == null) {
            throw IllegalArgumentException("Voicepack not found")
        }
        return VoicepackDto.fromEntity(voicepack)
    }

    // 보이스팩 예시 음성 파일 조회
    fun getExampleVoice(voicepackId: Long): String {
        val voicepack = findVoicepack(voicepackId)
        val s3ObjectKey = "speakers/${voicepack.name}/sample_test.wav"
        return s3PresignedUrlGenerator.generatePresignedUrl(s3ObjectKey)
    }

    // 보이스팩 생성 결과 콜백 처리
    @Transactional
    fun handleCallback(voicepackRequestId: Long, status: String) {
        val voicepackRequest = voicepackRequestRepository.findById(voicepackRequestId).orElseThrow {
            IllegalArgumentException("Voicepack request not found")
        }
        when (status) {
            "success" -> {
                handleSuccessfulConversion(voicepackRequest)
            }
            "failed" -> {
                handleFailedConversion(voicepackRequest, Exception("AI 모델 서비스 호출 실패"))
            }
        }
    }
}