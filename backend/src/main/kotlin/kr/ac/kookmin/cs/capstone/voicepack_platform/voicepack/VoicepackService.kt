package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack

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
import kr.ac.kookmin.cs.capstone.voicepack_platform.notification.NotificationService
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.UserRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.User
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRightDto
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRight
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.request.VoicepackRequestStatus
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.request.VoicepackRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.request.VoicepackRequestRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRightRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRightBriefDto
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.VoiceSynthesisRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.VoiceSynthesisRequestRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.SynthesisStatus
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto.VoicepackSynthesisSubmitResponse
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto.VoicepackCallbackRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto.VoicepackSynthesisStatusDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.OffsetDateTime
import kr.ac.kookmin.cs.capstone.voicepack_platform.common.util.S3PresignedUrlGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Service
class VoicepackService(
        private val voicepackRepository: VoicepackRepository,
        private val voicepackRequestRepository: VoicepackRequestRepository,
        private val voicepackUsageRightRepository: VoicepackUsageRightRepository,
        private val voiceSynthesisRequestRepository: VoiceSynthesisRequestRepository,
        private val userRepository: UserRepository,
        private val notificationService: NotificationService,
        // private val creditService: CreditService,
        @Value("\${ai.model.service.voicepack_creation}") private val voicepackCreationEndpoint: String,
        @Value("\${aws.lambda.endpoint.synthesis}") private val awsLambdaSynthesisEndpoint: String,
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
            voicepackId = voicepackRequest.name,
            voiceFile = voiceFile
        )
        
        logger.info("AI 모델 요청: requestId={}, request={}", voicepackRequest.id, aiModelRequest)
        
        try {
            val response = httpClient.post("$voicepackCreationEndpoint") {
                contentType(ContentType.MultiPart.FormData)
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("voicepackId", aiModelRequest.voicepackId)
                            append("voiceFile", aiModelRequest.voiceFile.bytes, Headers.build {
                                append(HttpHeaders.ContentDisposition, "form-data; name=\"voiceFile\"; filename=\"audio.wav\"")
                                append(HttpHeaders.ContentType, "audio/wav") // 필요 시 파일 확장자 변경 가능
                })}))
            }
            
            // 응답 상태 코드 확인
            if (!response.status.isSuccess()) {
                val errorBody = response.body<String>()
                logger.error("AI 모델 서비스 오류: HTTP ${response.status.value}, 응답: $errorBody")
                throw RuntimeException("AI 모델 서비스 호출 실패: HTTP ${response.status.value}, 응답: $errorBody")
            }
            
            // 성공 시 응답 파싱
            val aiModelResponse: String = response.body()
            logger.info("AI 모델 응답: requestId={}, response={}", voicepackRequest.id, aiModelResponse)
            
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
     * 보이스팩 합성 요청 (비동기 방식)
     * =========== START ===========
     */
    @Transactional
    suspend fun submitSynthesisRequest(userId: Long, request: VoicepackSynthesisRequest): VoicepackSynthesisSubmitResponse {
        logger.info("보이스팩 합성 비동기 요청 시작: userId={}, request={}", userId, request)

        val user = findUser(userId)
        val voicepack = findVoicepack(request.voicepackId)

        // TODO: 사용권 확인 로직 강화 (사용자가 이 보이스팩에 대한 사용권을 가지고 있는지 확인)
        if (!voicepackUsageRightRepository.existsByUserIdAndVoicepackId(userId, request.voicepackId)) {
            logger.warn("사용 권한 없는 보이스팩 합성 시도: userId={}, voicepackId={}", userId, request.voicepackId)
            throw SecurityException("해당 보이스팩에 대한 사용 권한이 없습니다.")
        }

        // 1. 합성 요청 엔티티 생성 및 저장
        val synthesisRequest = VoiceSynthesisRequest(
            user = user,
            voicepack = voicepack,
            prompt = request.prompt
            // jobId는 자동 생성됨
        )
        voiceSynthesisRequestRepository.save(synthesisRequest)
        logger.info("음성 합성 요청 저장됨: jobId={}, userId={}, voicepackId={}", 
            synthesisRequest.jobId, userId, request.voicepackId)

        // 2. AWS Lambda 비동기 호출
        val lambdaPayload = mapOf(
            "jobId" to synthesisRequest.jobId,
            "voicepackName" to voicepack.name,
            "prompt" to request.prompt,
            // TODO: 필요한 경우 추가 파라미터 전달 (예: 콜백 URL)
            "callbackUrl" to "/api/voicepack/synthesis/callback" // 경로 변경
        )

        try {
            withContext(Dispatchers.IO) { // 네트워크 호출은 IO 스레드에서
                httpClient.post(awsLambdaSynthesisEndpoint) {
                    contentType(ContentType.Application.Json)
                    setBody(lambdaPayload)
                }
                // Lambda는 비동기 호출이므로 응답 본문은 중요하지 않을 수 있음 (202 Accepted 등 상태 코드 확인 가능)
            }
            logger.info("AWS Lambda 호출 성공: jobId={}", synthesisRequest.jobId)
            synthesisRequest.status = SynthesisStatus.PROCESSING
            synthesisRequest.updatedAt = OffsetDateTime.now()
            voiceSynthesisRequestRepository.save(synthesisRequest)

            // 3. 요청 제출 성공 응답 반환
            return VoicepackSynthesisSubmitResponse(
                jobId = synthesisRequest.jobId,
                message = "음성 합성 요청이 성공적으로 제출되었습니다. 완료 시 알림이 전송됩니다."
            )

        } catch (e: Exception) {
            logger.error("AWS Lambda 호출 실패: jobId={}, error={}", synthesisRequest.jobId, e.message, e)
            // Lambda 호출 실패 시 요청 상태 FAILED로 변경 및 저장
            synthesisRequest.status = SynthesisStatus.FAILED
            synthesisRequest.errorMessage = "Lambda 호출 실패: ${e.message}"
            synthesisRequest.updatedAt = OffsetDateTime.now()
            voiceSynthesisRequestRepository.save(synthesisRequest)
            throw RuntimeException("음성 합성 요청 제출 중 오류 발생 (Lambda 호출 실패)", e)
        }
    }

    /**
     * 음성 합성 콜백 처리
     */
    @Transactional
    fun handleSynthesisCallback(callbackRequest: VoicepackCallbackRequest) {
        logger.info("음성 합성 콜백 수신: jobId={}", callbackRequest.jobId)

        val synthesisRequestOpt = voiceSynthesisRequestRepository.findByJobId(callbackRequest.jobId)
        if (synthesisRequestOpt.isEmpty) {
            logger.error("콜백 처리 실패: 해당 jobId의 요청을 찾을 수 없음 - jobId={}", callbackRequest.jobId)
            // TODO: 적절한 오류 처리 (예: 로깅만 할지, 예외를 던질지)
            return // 혹은 예외 발생
        }
        val synthesisRequest = synthesisRequestOpt.get()

        // 이미 처리된 콜백인지 확인 (멱등성)
        if (synthesisRequest.status == SynthesisStatus.COMPLETED || synthesisRequest.status == SynthesisStatus.FAILED) {
            logger.warn("이미 처리된 콜백 요청입니다: jobId={}, status={}", callbackRequest.jobId, synthesisRequest.status)
            return
        }

        // 콜백 결과에 따라 상태 업데이트
        synthesisRequest.updatedAt = OffsetDateTime.now()
        if (callbackRequest.success) {
            synthesisRequest.status = SynthesisStatus.COMPLETED
            synthesisRequest.resultUrl = callbackRequest.resultUrl // S3 Presigned URL 또는 직접 URL
            logger.info("음성 합성 성공 처리 완료: jobId={}, resultUrl={}", 
                synthesisRequest.jobId, synthesisRequest.resultUrl)
            // TODO: 사용자에게 성공 알림 전송 (notificationService 사용)
            // notificationService.notifySynthesisComplete(synthesisRequest)
        } else {
            synthesisRequest.status = SynthesisStatus.FAILED
            synthesisRequest.errorMessage = callbackRequest.errorMessage ?: "음성 합성 처리 실패 (원인 미상)"
            logger.error("음성 합성 실패 처리 완료: jobId={}, error={}", 
                synthesisRequest.jobId, synthesisRequest.errorMessage)
            // TODO: 사용자에게 실패 알림 전송
            // notificationService.notifySynthesisFailed(synthesisRequest)
        }

        voiceSynthesisRequestRepository.save(synthesisRequest)
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

    /**
     * 보이스팩 사용권 획득 처리 (구매 또는 제작자 획득)
     */
    @Transactional
    fun grantUsageRight(userId: Long, voicepackId: Long): VoicepackUsageRightDto {
        logger.info("보이스팩 사용권 획득 요청: userId={}, voicepackId={}", userId, voicepackId)

        val user = findUser(userId)
        val voicepack = findVoicepack(voicepackId)

        // 1. 이미 사용권을 가지고 있는지 확인
        if (voicepackUsageRightRepository.existsByUserIdAndVoicepackId(userId, voicepackId)) {
            logger.warn("이미 사용권을 가지고 있는 보이스팩입니다: userId={}, voicepackId={}", userId, voicepackId)
            throw IllegalStateException("이미 사용권을 가지고 있는 보이스팩입니다.")
        }

        // 2. (선택 사항) 가격 확인 및 크레딧 차감 (제작자가 아닌 경우에만)
        // if (voicepack.author.id != userId) {
        //     // TODO: 보이스팩 가격 확인 및 크레딧 차감 로직
        //     /*
        //     val voicepackPrice = voicepack.price ?: 0
        //     if (voicepackPrice > 0) { ... }
        //     */
        // }

        // 3. 사용권 정보 생성 및 저장
        val usageRight = VoicepackUsageRight(
            user = user,
            voicepack = voicepack
        )
        val savedUsageRight = voicepackUsageRightRepository.save(usageRight)
        logger.info("보이스팩 사용권 저장 성공: usageRightId={}, userId={}, voicepackId={}", savedUsageRight.id, userId, voicepackId)

        // 4. 결과 반환
        return VoicepackUsageRightDto.fromEntity(savedUsageRight)
    }


    /**
     * 사용자가 보유한 보이스팩 목록 조회
     */
    fun getVoicepacksByUserId(userId: Long): List<VoicepackUsageRightBriefDto> {
        logger.info("사용자의 보이스팩 목록 조회: userId={}", userId)
        return voicepackUsageRightRepository.findVoicepackDtosByUserId(userId)
    }

    /**
     * 음성 합성 상태 조회
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    fun getSynthesisStatus(jobId: String): VoicepackSynthesisStatusDto {
        logger.debug("음성 합성 상태 조회 요청: jobId={}", jobId)
        
        val synthesisRequest = voiceSynthesisRequestRepository.findByJobId(jobId)
            .orElseThrow { 
                logger.warn("상태 조회 실패: 해당 jobId의 요청을 찾을 수 없음 - jobId={}", jobId)
                IllegalArgumentException("해당 Job ID의 합성 요청을 찾을 수 없습니다.") 
            }
        
        logger.debug("음성 합성 상태 조회 성공: jobId={}, status={}", jobId, synthesisRequest.status)
        return VoicepackSynthesisStatusDto(
            jobId = synthesisRequest.jobId,
            status = synthesisRequest.status.name,
            resultUrl = synthesisRequest.resultUrl,
            errorMessage = synthesisRequest.errorMessage
        )
    }

}
