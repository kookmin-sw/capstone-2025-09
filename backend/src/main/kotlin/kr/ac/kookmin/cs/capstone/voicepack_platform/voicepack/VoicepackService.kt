package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kr.ac.kookmin.cs.capstone.voicepack_platform.common.util.S3PresignedUrlGenerator
import kr.ac.kookmin.cs.capstone.voicepack_platform.notification.NotificationService
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.User
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.UserRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.convert.VoicepackRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.convert.VoicepackConvertRequestRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.convert.VoicepackRequestStatus
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.convert.dto.VoicepackConvertStatusDto
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.SynthesisStatus
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.VoiceSynthesisRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.VoiceSynthesisRequestRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto.VoicepackCallbackRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto.VoicepackSynthesisStatusDto
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto.VoicepackSynthesisSubmitResponse
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRight
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRightBriefDto
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRightDto
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRightRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.dto.UseCreditsRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.ReferenceType
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.service.CreditService
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.TransactionStatus
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.dto.ChargeCreditsRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.sale.Sale
import kr.ac.kookmin.cs.capstone.voicepack_platform.sale.SaleRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.OffsetDateTime
import java.util.*
import com.fasterxml.jackson.core.type.TypeReference
import kr.ac.kookmin.cs.capstone.voicepack_platform.common.util.S3ObjectDeleter
import kr.ac.kookmin.cs.capstone.voicepack_platform.common.util.S3ObjectUploader

@Service
class VoicepackService(
    private val voicepackRepository: VoicepackRepository,
    private val voicepackConvertRequestRepository: VoicepackConvertRequestRepository,
    private val voicepackUsageRightRepository: VoicepackUsageRightRepository,
    private val voiceSynthesisRequestRepository: VoiceSynthesisRequestRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService,
    private val creditService: CreditService,
    private val saleRepository: SaleRepository,
    private val s3PresignedUrlGenerator: S3PresignedUrlGenerator,
    private val rabbitTemplate: RabbitTemplate,
    private val s3ObjectDeleter: S3ObjectDeleter,
    private val s3ObjectUploader: S3ObjectUploader
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val objectMapper = jacksonObjectMapper() // JSON 변환기

    /**
     * Voicepack 엔티티를 VoicepackDto로 변환하는 private 헬퍼 메소드.
     * 카테고리 JSON 파싱 실패 시 IllegalArgumentException을 발생시킵니다.
     */
    private fun convertToDto(voicepack: Voicepack): VoicepackDto {
        val presignedImageUrl = voicepack.imageS3Key?.let {
            this.s3PresignedUrlGenerator.generatePresignedUrl(it)
        }
        
        val parsedCategories: List<String> = try {
            this.objectMapper.readValue(voicepack.categoriesJson, object : TypeReference<List<String>>() {})
        } catch (e: Exception) {
            logger.error("카테고리 JSON 파싱 실패: voicepackId={}, json='{}', error={}", voicepack.id, voicepack.categoriesJson, e.message)
            throw IllegalArgumentException("카테고리 JSON 파싱에 실패했습니다. Voicepack ID: ${voicepack.id}", e)
        }
        return VoicepackDto.fromEntity(voicepack, presignedImageUrl, parsedCategories)
    }

    /**
     * 보이스팩 변환 요청 및 처리
     * =========== START ===========
     */
    @Transactional
    suspend fun convertVoicepack(
            userId: Long,
            request: VoicepackConvertRequest
    ): VoicepackConvertResponse {
        logger.info("보이스팩 변환 요청: userId={}, requestName={}, hasImage={}, categories={}", 
            userId, request.name, request.imageFile?.originalFilename, request.categories)
        val user = findUser(userId)

        // 패키지 이름 유효성 검사
        validatePackName(request.name)
        
        // 진행 중인 요청이 있는지 확인
        checkOngoingRequests(userId)

        // 이미지 처리 및 카테고리 JSON 변환
        val imageS3Key = request.imageFile?.let { s3ObjectUploader.uploadImageToS3(it, request.name, "voicepackImages") }
        val categoriesJson = objectMapper.writeValueAsString(request.categories) 

        // 보이스팩 요청 엔티티 생성
        val voicepackRequest = createVoicepackRequest(user, request.name, imageS3Key, categoriesJson, request.isVideoBased, request.tempFilePath)

        try {
            // ActiveMQ로 메시지 전송
            callAiModelService(voicepackRequest, request.voiceFile)
            return VoicepackConvertResponse(voicepackRequest.id, VoicepackRequestStatus.PROCESSING.name)
            
        } catch (e: Exception) {
            // 변환 요청 실패 시 VoicepackRequest 엔티티 상태 업데이트 등 추가 처리 가능
            voicepackRequest.status = VoicepackRequestStatus.FAILED
            voicepackConvertRequestRepository.save(voicepackRequest)
            
            // 실패 시 S3에 업로드된 이미지 삭제 (imageS3Key가 있다면)
            voicepackRequest.imageS3Key?.let {
                s3ObjectDeleter.deleteObject(it)
                voicepackRequest.imageS3Key = null
                voicepackConvertRequestRepository.save(voicepackRequest)
            }
            logger.error("AI 모델 서비스 호출 실패 또는 기타 변환 오류: requestId={}, error={}", voicepackRequest.id, e.message)
            return VoicepackConvertResponse(voicepackRequest.id, VoicepackRequestStatus.FAILED.name)
        }
    }
    

    // 보이스팩 요청 엔티티 생성
    private fun createVoicepackRequest(user: User, name: String, imageS3Key: String?, categoriesJson: String, isVideoBased: Boolean, tempFilePath: String?): VoicepackRequest {
        val voicepackRequest = VoicepackRequest(
            name = name,
            author = user,
            status = VoicepackRequestStatus.PROCESSING,
            imageS3Key = imageS3Key,
            categoriesJson = categoriesJson,
            isVideoBased = isVideoBased,
            tempFilePath = tempFilePath
        )
        return voicepackConvertRequestRepository.save(voicepackRequest)
    }

    // AI 모델 서비스 호출
    private suspend fun callAiModelService(voicepackRequest: VoicepackRequest, voiceFile: MultipartFile) {
        val aiModelRequest = AIModelRequest(
            voicepackId = voicepackRequest.name,
            voiceFile = voiceFile,
            voicepackRequestId = voicepackRequest.id
        )

        logger.info("AI 모델 요청: requestId={}, request={}", voicepackRequest.id, aiModelRequest)

        try {
            // MultipartFile을 바이너리 데이터로 변환
            val voiceFileBase64 = Base64.getEncoder().encodeToString(voiceFile.bytes)

            // 메시지를 JSON 형식으로 변환
            val messageJson = objectMapper.writeValueAsString(
                mapOf(
                    "voicepackId" to aiModelRequest.voicepackId,
                    "voicepackRequestId" to aiModelRequest.voicepackRequestId,
                    "voiceFile" to voiceFileBase64 // Base64로 변환된 음성 파일
                )
            )

            // MQ로 메시지 전송
            rabbitTemplate.convertAndSend("convert", messageJson)

            logger.info("MQ 메시지 전송 완료: requestId={}, queue={}", voicepackRequest.id, "convert")
            return

        } catch (e: Exception) {
            logger.error("MQ 메시지 전송 중 오류 발생: ${e.message}", e)
            throw RuntimeException("MQ 메시지 전송 실패: ${e.message}", e)
        }
    }

    // 변환 성공 시 처리
    @Transactional
    private fun handleSuccessfulConversion(voicepackRequest: VoicepackRequest) {
        
        val outputPath = "speakers/${voicepackRequest.name}/feature.json"
        val finishedTime = OffsetDateTime.now() // 완료 시각 일관성 유지
        
        // 요청 상태 업데이트
        voicepackRequest.status = VoicepackRequestStatus.COMPLETED
        voicepackRequest.s3Path = outputPath
        voicepackRequest.completedAt = finishedTime
        
        // 완성된 보이스팩 생성
        val voicepack = Voicepack(
            name = voicepackRequest.name,
            author = voicepackRequest.author,
            s3Path = outputPath,
            createdAt = finishedTime,
            imageS3Key = voicepackRequest.imageS3Key,
            categoriesJson = voicepackRequest.categoriesJson,
            isVideoBased = voicepackRequest.isVideoBased
        )
        val savedVoicepack = voicepackRepository.save(voicepack)
        voicepackRequest.voicepackId = savedVoicepack.id
        voicepackConvertRequestRepository.save(voicepackRequest)

        // 사용권 부여
        grantUsageRight(voicepackRequest.author.id, savedVoicepack.id)

        // 알림 전송
        notificationService.notifyVoicepackComplete(voicepackRequest)

        // 임시파일 삭제 (영상기반 보이스팩 등)
        if (voicepackRequest.tempFilePath != null) {
            try {
                val file = java.io.File(voicepackRequest.tempFilePath)
                if (file.exists()) file.delete()
                logger.info("임시파일 삭제 완료: ${voicepackRequest.tempFilePath}")
            } catch (e: Exception) {
                logger.warn("임시파일 삭제 실패: ${voicepackRequest.tempFilePath}")
            }
        }
        
        logger.info("보이스팩 변환 성공 및 저장 완료: voicepackId={}, imageS3Key={}, categoriesJson={}", 
            savedVoicepack.id, savedVoicepack.imageS3Key, savedVoicepack.categoriesJson)
    }

    // 변환 실패 시 처리
    @Transactional
    private fun handleFailedConversion(voicepackRequest: VoicepackRequest, exception: Exception) {
        logger.error("AI 모델 처리 실패: requestId={}, error={}", voicepackRequest.id, exception.message)
        
        // 실패 상태로 업데이트
        voicepackRequest.status = VoicepackRequestStatus.FAILED
        voicepackRequest.completedAt = OffsetDateTime.now()

        // S3에 업로드된 이미지가 있다면 삭제
        voicepackRequest.imageS3Key?.let {
            s3ObjectDeleter.deleteObject(it)
            voicepackRequest.imageS3Key = null
        }
        
        voicepackConvertRequestRepository.save(voicepackRequest)
        
        // 알림 전송
        notificationService.notifyVoicepackFailed(voicepackRequest)

        // 임시파일 삭제 (영상기반 보이스팩 등)
        if (voicepackRequest.tempFilePath != null) {
            try {
                val file = java.io.File(voicepackRequest.tempFilePath)
                if (file.exists()) file.delete()
                logger.info("임시파일 삭제 완료: ${voicepackRequest.tempFilePath}")
            } catch (e: Exception) {
                logger.warn("임시파일 삭제 실패: ${voicepackRequest.tempFilePath}")
            }
        }
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
        val existingRequests = voicepackConvertRequestRepository.findByAuthorId(userId)
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
     * 보이스팩 삭제
     */
    @Transactional
    fun deleteVoicepack(userId: Long, voicepackId: Long) {
        logger.info("보이스팩 삭제 시작: userId={}, voicepackId={}", userId, voicepackId)
        
        // 권한 확인: 요청한 사용자가 보이스팩 작성자인지 확인
        val voicepack = findVoicepack(voicepackId)
        if (voicepack.author.id != userId) {
            logger.warn("보이스팩 삭제 권한 없음: userId={}, voicepackId={}, authorId={}", userId, voicepackId, voicepack.author.id)
            throw SecurityException("해당 보이스팩을 삭제할 권한이 없습니다.")
        }

        // 리팩토링 시 S3 음성 파일 삭제 로직 추가하면 좋을 듯

        // S3에서 보이스팩 이미지 삭제
        voicepack.imageS3Key?.let {
            if (it.isNotBlank()) { // 혹시 모를 공백 체크
                try {
                    s3ObjectDeleter.deleteObject(it)
                    logger.info("S3 대표 이미지 삭제 완료: s3Key={}", it)
                } catch (e: Exception) {
                    logger.error("S3 대표 이미지 삭제 실패: s3Key={}, error={}", it, e.message, e)
                    // S3 파일 삭제 실패 시 처리 정책 결정 필요
                }
            }
        }
        
        voicepackRepository.deleteById(voicepackId)
        logger.info("보이스팩 삭제 완료: voicepackId={}", voicepackId)
    }

    /**
     * 보이스팩 합성 요청 (비동기 방식)
     * =========== START ===========
     */
    @Transactional
    suspend fun submitSynthesisRequest(userId: Long, request: VoicepackSynthesisRequest): VoicepackSynthesisSubmitResponse {
        logger.info("보이스팩 합성 비동기 요청 시작: userId={}, request={}", userId, request)

        val user = findUser(userId)
        val voicepack = findVoicepack(request.voicepackId)
        logger.info("요청 정보 확인 완료: userId={}, voicepackId={}, voicepackName={}", userId, voicepack.id, voicepack.name)

        // TODO: 사용권 확인 로직 강화 (사용자가 이 보이스팩에 대한 사용권을 가지고 있는지 확인)
        if (!voicepackUsageRightRepository.existsByUserIdAndVoicepackId(userId, request.voicepackId)) {
            logger.warn("사용 권한 없는 보이스팩 합성 시도: userId={}, voicepackId={}", userId, request.voicepackId)
            throw SecurityException("해당 보이스팩에 대한 사용 권한이 없습니다.")
        } else {
            logger.info("사용 권한 확인 완료: userId={}, voicepackId={}", userId, request.voicepackId)
        }

        // 1. 합성 요청 엔티티 생성 및 저장
        val synthesisRequest = VoiceSynthesisRequest(
            user = user,
            voicepack = voicepack,
            prompt = request.prompt,
            emotionIndex = request.emotionIndex
        )
        val savedRequest = voiceSynthesisRequestRepository.save(synthesisRequest) // 저장된 엔티티를 받음
        logger.info("음성 합성 요청 엔티티 생성 및 저장 완료: id={}, userId={}, voicepackId={}, status={}", 
            savedRequest.id, userId, request.voicepackId, savedRequest.status)

        // RabbitMQ로 메시지 전송
        logger.info("RabbitMQ 메시지 전송 시도: id={}", savedRequest.id)
        val sendSuccess = sendSynthesisMessageToRabbitMQ(savedRequest, voicepack, request.prompt)
        
        if (sendSuccess) {
            // 성공 시 처리
            logger.info("RabbitMQ 메시지 전송 성공: id={}", savedRequest.id)
            savedRequest.status = SynthesisStatus.PROCESSING
            savedRequest.updatedAt = OffsetDateTime.now()
            voiceSynthesisRequestRepository.save(savedRequest)
            logger.info("합성 요청 상태 업데이트: id={}, status={}", savedRequest.id, SynthesisStatus.PROCESSING)

            val response = VoicepackSynthesisSubmitResponse(
                id = savedRequest.id,
                message = "음성 합성 요청이 성공적으로 제출되었습니다. 완료 시 알림이 전송됩니다."
            )
            logger.info("합성 요청 성공 응답 반환: id={}", savedRequest.id)
            return response
        } else {
            // 실패 시 처리
            logger.error("RabbitMQ 메시지 전송 실패: id={}", savedRequest.id)
            savedRequest.status = SynthesisStatus.FAILED
            savedRequest.errorMessage = "MQ 메시지 전송 실패"
            savedRequest.updatedAt = OffsetDateTime.now()
            voiceSynthesisRequestRepository.save(savedRequest)
            logger.info("합성 요청 상태 업데이트: id={}, status={}, errorMessage={}", savedRequest.id, SynthesisStatus.FAILED, savedRequest.errorMessage)

            val response = VoicepackSynthesisSubmitResponse(
                id = savedRequest.id,
                message = "음성 합성 요청 처리 중 오류가 발생했습니다. 나중에 다시 시도해주세요."
            )
            logger.warn("합성 요청 실패 응답 반환: id={}", savedRequest.id)
            return response
        }
    }

    /**
     * RabbitMQ로 음성 합성 메시지 전송
     * @return 전송 성공 여부 (true: 성공, false: 실패)
     */
    private fun sendSynthesisMessageToRabbitMQ(synthesisRequest: VoiceSynthesisRequest, voicepack: Voicepack, prompt: String): Boolean {
        try {
            // 메시지 생성
            val messageJson = objectMapper.writeValueAsString(
                mapOf(
                    "id" to synthesisRequest.id,
                    "userId" to synthesisRequest.user.id,
                    "voicepackName" to voicepack.name,
                    "prompt" to prompt,
                    "callbackUrl" to "/api/voicepack/synthesis/callback",
                    "emotionIndex" to synthesisRequest.emotionIndex
                )
            )

            // MQ로 메시지 전송
            logger.info("RabbitMQ 전송 시작: id={}", synthesisRequest.id)
            rabbitTemplate.convertAndSend("synthesis", messageJson)
            logger.info("MQ 메시지 전송 완료: id={}, queue={}", synthesisRequest.id, "synthesis")
            
            return true
        } catch (e: Exception) {
            logger.error("MQ 메시지 전송 중 오류 발생: ${e.message}", e)
            return false
        }
    }

    /**
     * 음성 합성 콜백 처리
     */
    @Transactional
    fun handleSynthesisCallback(callbackRequest: VoicepackCallbackRequest) {
        logger.info("음성 합성 콜백 수신: id={}", callbackRequest.id)

        val synthesisRequestOpt = voiceSynthesisRequestRepository.findById(callbackRequest.id)
        if (synthesisRequestOpt.isEmpty) {
            logger.error("콜백 처리 실패: 해당 id의 요청을 찾을 수 없음 - id={}", callbackRequest.id)
            // TODO: 적절한 오류 처리 (예: 로깅만 할지, 예외를 던질지)
            return // 혹은 예외 발생
        }
        val synthesisRequest = synthesisRequestOpt.get()

        // 이미 처리된 콜백인지 확인 (멱등성)
        if (synthesisRequest.status == SynthesisStatus.COMPLETED || synthesisRequest.status == SynthesisStatus.FAILED) {
            logger.warn("이미 처리된 콜백 요청입니다: id={}, status={}", callbackRequest.id, synthesisRequest.status)
            return
        }

        // 콜백 결과에 따라 상태 업데이트
        synthesisRequest.updatedAt = OffsetDateTime.now()
        if (callbackRequest.success) {
            synthesisRequest.status = SynthesisStatus.COMPLETED
            synthesisRequest.resultUrl = callbackRequest.resultUrl // S3 Presigned URL 또는 직접 URL
            logger.info("음성 합성 성공 처리 완료: id={}, resultUrl={}", 
                synthesisRequest.id, synthesisRequest.resultUrl)
            // TODO: 사용자에게 성공 알림 전송 (notificationService 사용)
            // notificationService.notifySynthesisComplete(synthesisRequest)
        } else {
            synthesisRequest.status = SynthesisStatus.FAILED
            synthesisRequest.errorMessage = callbackRequest.errorMessage ?: "음성 합성 처리 실패 (원인 미상)"
            logger.error("음성 합성 실패 처리 완료: id={}, error={}", 
                synthesisRequest.id, synthesisRequest.errorMessage)
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
    fun getVoicepacks(userId: Long?, filter: String?): List<VoicepackDto> {
        logger.info("보이스팩 목록 조회: userId={}, filter={}", userId, filter ?: "all")

        val voicepacks: List<Voicepack> = when (filter?.lowercase()) {
            "mine" -> {
                if (userId == null) throw IllegalArgumentException("'mine' 필터 사용 시 userId는 필수입니다.")
                voicepackRepository.findByAuthorId(userId)
            }
            "purchased" -> {
                if (userId == null) throw IllegalArgumentException("'purchased' 필터 사용 시 userId는 필수입니다.")
                voicepackUsageRightRepository.findDistinctPurchasedVoicepacksByUserId(userId)
            }
            "available" -> { 
                if (userId == null) throw IllegalArgumentException("'available' 필터 사용 시 userId는 필수입니다.")
                voicepackUsageRightRepository.findByUserId(userId)
            }
            else -> { // "all" 또는 그 외 (기본값)
                voicepackRepository.findByIsPublicTrue() // 공개된 것만 조회
            }
        }
        
        return voicepacks.map { entity ->
            convertToDto(entity)
        }
    }

    // 보이스팩 1개만 조회
    fun getVoicepack(voicepackId: Long): VoicepackDto {
        val entity: Voicepack = findVoicepack(voicepackId) 
        return convertToDto(entity)
    }

    // 보이스팩 예시 음성 파일 조회
    fun getExampleVoice(voicepackId: Long): String {
        val voicepack = findVoicepack(voicepackId)
        val s3ObjectKey = "speakers/${voicepack.name}/sample_test.wav"
        return s3PresignedUrlGenerator.generatePresignedUrl(s3ObjectKey)
    }

    // 보이스팩 생성 결과 콜백 처리
    @Transactional
    fun handleCreationCallback(voicepackRequestId: Long, status: String) {
        val voicepackRequest = voicepackConvertRequestRepository.findById(voicepackRequestId).orElseThrow {
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

    /**
     * 보이스팩 사용권 획득 처리 (구매 또는 제작자 획득)
     */
    @Transactional
    fun grantUsageRight(userId: Long, voicepackId: Long): VoicepackUsageRightDto {
        logger.info("보이스팩 사용권 획득 요청: userId={}, voicepackId={}", userId, voicepackId)

        val user = findUser(userId)
        val voicepack = findVoicepack(voicepackId)
        val seller = voicepack.author

        // 1. 이미 사용권을 가지고 있는지 확인
        if (voicepackUsageRightRepository.existsByUserIdAndVoicepackId(userId, voicepackId)) {
            logger.warn("이미 사용권을 가지고 있는 보이스팩입니다: userId={}, voicepackId={}", userId, voicepackId)
            throw IllegalStateException("이미 사용권을 가지고 있는 보이스팩입니다.")
        }

        // 2. (선택 사항) 가격 확인 및 크레딧 차감 (제작자가 아닌 경우에만)
        if (seller.id != userId) {
            // TODO: 보이스팩 가격 확인 및 크레딧 차감 로직
            
            val voicepackPrice = voicepack.price ?: 0 // Nullable price 처리: null이면 0으로 간주
            if (voicepackPrice > 0) {
                // 크레딧 차감 로직 추가
                val result = creditService.useCredits(userId, UseCreditsRequest(
                    amount = voicepackPrice,
                    referenceId = voicepackId,
                    referenceType = ReferenceType.VOICEPACK,
                    description = "보이스팩 구매: ${voicepack.name}"
                ))
                if (result.status == TransactionStatus.FAILED.name) {
                    throw IllegalStateException("크레딧 차감 실패: ${result.message}")
                }
                // 판매자에게 크레딧 지급 (구매 가격만큼)
                // TODO: 판매 수수료 정책 적용 필요 시 여기에 로직 추가 (예: amount * 0.9)
                creditService.chargeCredits(ChargeCreditsRequest(
                    userId = seller.id,
                    amount = voicepackPrice, // 수수료 적용 시 수정 필요
                    paymentMethod = "보이스팩 판매: ${voicepack.name}", // 판매 수익임을 명시
                    paymentReference = voicepackId
                ))
                
                // 판매 기록 생성
                val saleRecord = Sale(
                    seller = seller,
                    buyer = user,
                    voicepack = voicepack,
                    amount = voicepackPrice // 실제 판매 금액 기록
                    // transactionDate는 기본값 사용 (현재 시간)
                )
                saleRepository.save(saleRecord)
                logger.info("판매 기록 저장 완료: saleId={}, sellerId={}, buyerId={}, voicepackId={}, amount={}",
                    saleRecord.id, seller.id, user.id, voicepack.id, voicepackPrice)
            }
        }

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
    fun getVoicepacksByUserId(userId: Long, isVideoBased: Boolean): List<VoicepackUsageRightBriefDto> {
        logger.info("사용자의 보이스팩 목록 조회: userId={}", userId)
        return voicepackUsageRightRepository.findVoicepackDtosByUserIdAndIsVideoBased(userId, isVideoBased)
    }

    /**
     * 보이스팩 변환 상태 조회
     */
    @Transactional(readOnly = true)
    fun getConvertStatus(id: Long): VoicepackConvertStatusDto =
        voicepackConvertRequestRepository.findById(id)
            .orElseThrow {
                logger.warn("변환 조회 실패: 해당 id의 요청을 찾을 수 없음 - id={}", id)
                IllegalArgumentException("해당 id의 변환 요청을 찾을 수 없습니다.")
            }.run {
                logger.debug("보이스팩 변환 상태 조회 성공: id={}, status={}", id, status)
                VoicepackConvertStatusDto(
                    voicepackId = voicepackId,
                    status = status.name,
                )
            }



    /**
     * 음성 합성 상태 조회
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    fun getSynthesisStatus(id: Long): VoicepackSynthesisStatusDto {
        logger.debug("음성 합성 상태 조회 요청: id={}", id)
        
        val synthesisRequest = voiceSynthesisRequestRepository.findById(id)
            .orElseThrow { 
                logger.warn("상태 조회 실패: 해당 id의 요청을 찾을 수 없음 - id={}", id)
                IllegalArgumentException("해당 id의 합성 요청을 찾을 수 없습니다.") 
            }
        
        logger.debug("음성 합성 상태 조회 성공: id={}, status={}", id, synthesisRequest.status)
        return VoicepackSynthesisStatusDto(
            id = synthesisRequest.id,
            status = synthesisRequest.status.name,
            resultUrl = synthesisRequest.resultUrl?.let { s3Key -> s3PresignedUrlGenerator.generatePresignedUrl(s3Key) },
            errorMessage = synthesisRequest.errorMessage
        )
    }


    // 디버그용 보이스팩 생성 메소드. 브랜치 병합 후 삭제 필요.
    @Transactional
    fun createVoicepackForDebug(userId: Long, voicepackId: Long) {
        val user = try {
            findUser(userId)
        } catch (e: Exception) {
            // 새 유저 생성
            val newUser = User(
                email = "test@test.com",
                password = "test",
                name = "test",
            )
            userRepository.save(newUser)
            newUser
        }
        val voicepack = try {
            findVoicepack(voicepackId)
        } catch (e: Exception) {
            // 새 보이스팩 생성
            val newVoicepack = Voicepack(
                name = "test",
                author = user as User,
                s3Path = "test",
                createdAt = OffsetDateTime.now(),
                categoriesJson = "[]" // categoriesJson 파라미터 추가 (기본값으로 빈 배열)
            )
            voicepackRepository.save(newVoicepack)
            newVoicepack
        }
        // 사용권 정보 생성 및 저장
        val usageRight = VoicepackUsageRight(
            user = user as User,
            voicepack = voicepack as Voicepack
        )
        voicepackUsageRightRepository.save(usageRight)
    }

    /**
     * 보이스팩 공개 여부 변경
     */
    @Transactional
    fun updateVoicepackPublicStatus(userId: Long, voicepackId: Long, isPublic: Boolean): VoicepackDto {
        logger.info("보이스팩 공개 여부 변경 요청: userId={}, voicepackId={}, isPublic={}", userId, voicepackId, isPublic)

        val voicepack = findVoicepack(voicepackId) // voicepackId로 조회

        // 권한 확인: 요청한 사용자가 보이스팩 작성자인지 확인
        if (voicepack.author.id != userId) {
            logger.warn("보이스팩 공개 여부 변경 권한 없음: userId={}, voicepackId={}, authorId={}", userId, voicepackId, voicepack.author.id)
            throw SecurityException("해당 보이스팩을 수정할 권한이 없습니다.")
        }

        // 영상기반 보이스팩인 경우 공개 불가
        if (voicepack.isVideoBased) {
            logger.warn("영상기반 보이스팩은 공개 불가: voicepackId={}", voicepackId)
            throw IllegalStateException("영상기반 보이스팩은 공개 불가입니다.")
        }

        // 상태 업데이트
        voicepack.isPublic = isPublic
        val updatedVoicepack = voicepackRepository.save(voicepack)
        logger.info("보이스팩 공개 여부 변경 완료: voicepackId={}, isPublic={}", voicepackId, isPublic)

        return convertToDto(updatedVoicepack)
    }

}
