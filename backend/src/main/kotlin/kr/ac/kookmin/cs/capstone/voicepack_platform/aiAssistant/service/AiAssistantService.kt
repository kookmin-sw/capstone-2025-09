package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.transaction.annotation.Transactional
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.callback.AiAssistantCallbackRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.setting.AiAssistantSettingRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.callback.AiAssistantStatusDto
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.synthesis.AiAssistantSynthesisSubmitRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.response.synthesis.AiAssistantSynthesisSubmitResponse
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.entity.AiAssistantSetting
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.entity.AiAssistantStatus
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.entity.AiAssistantSynthesisRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums.Categories
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums.WritingStyle
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.repository.AiAssistantSettingRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.repository.AiAssistantSynthesisRequestRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.UserRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.VoicepackRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.SynthesisStatus
import org.springframework.beans.factory.annotation.Value
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRightRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.*
import kotlin.NoSuchElementException
import java.time.format.DateTimeFormatter

@Service
class AiAssistantService(
    private val userRepository: UserRepository,
    private val voicepackRepository: VoicepackRepository,
    private val aiAssistantSettingRepository: AiAssistantSettingRepository,
    private val voicepackUsageRightRepository: VoicepackUsageRightRepository,
    private val aiAssistantSynthesisRequestRepository: AiAssistantSynthesisRequestRepository,
    @Value("\${aws.s3.bucket-name}") private val bucketName: String,
    private val objectMapper: ObjectMapper,
    private val rabbitTemplate: RabbitTemplate,
    private val s3Client: S3Client,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // AI 비서 설정 저장 및 업데이트

    @Transactional
    fun saveOrUpdateAiAssistantSettings(userId: Long, request: AiAssistantSettingRequest): AiAssistantSetting {

        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }

        val writingStyle = WritingStyle.fromIndex(request.writingStyle)
            ?: throw IllegalArgumentException("Invalid writing style provided: ${request.writingStyle}")

        val categories = request.categories.mapNotNull { index ->
            Categories.fromIndex(index) ?: run { // fromIndex(index)가 null일 경우 유효하지 않은 인덱스는 로그 남기고 무시 -> 그냥 에러를 던지는게 좋을까?
                logger.warn("Invalid category index received: $index")
                null
            }
        }.toMutableSet() // 중복 제거 및 변경 가능한 Set으로 변환

        if (categories.isEmpty()) {
            throw IllegalArgumentException("At least one valid category must be selected.")
        }

        // 기존 설정이 있으면 업데이트, 없으면 새로 생성

        val existingSetting = aiAssistantSettingRepository.findByUserId(userId)

        val setting = existingSetting.orElse(
            AiAssistantSetting(user = user, voicepackId = request.voicepackId, writingStyle = writingStyle, categories = categories)
        )

        // 기존 설정이 있는 경우 필드 값 업데이트
        if (existingSetting.isPresent) {
            setting.voicepackId = request.voicepackId
            setting.writingStyle = writingStyle
            setting.categories = categories
        }

        logger.info("Saving/Updating AI Assistant settings for user ID: $userId")

        return aiAssistantSettingRepository.save(setting)
    }

    @Transactional
    suspend fun submitSynthesisRequest(userId: Long, request: AiAssistantSynthesisSubmitRequest): AiAssistantSynthesisSubmitResponse {
        logger.info("AI 비서 음성 합성 비동기 요청 시작: userId={}, request={}", userId, request)

        val user = userRepository.findById(userId).orElseThrow{
            IllegalArgumentException("User not found")
        }

        val voicepack =  voicepackRepository.findById(request.voicepackId).orElseThrow {
            IllegalArgumentException("Voicepack not found")
        }

        logger.info("요청 정보 확인 완료: userId={}, voicepackId={}, voicepackName={}", userId, voicepack.id, voicepack.name)

        // TODO: 사용권 확인 로직 강화 (사용자가 이 보이스팩에 대한 사용권을 가지고 있는지 확인)
        if (!voicepackUsageRightRepository.existsByUserIdAndVoicepackId(userId, request.voicepackId)) {
            logger.warn("사용 권한 없는 보이스팩 합성 시도: userId={}, voicepackId={}", userId, request.voicepackId)
            throw SecurityException("해당 보이스팩에 대한 사용 권한이 없습니다.")
        }

        /**
         * s3에서 prompt 가져와서 저장하기
         */

        // 현재는 카테고리의 첫 번째 인덱스를 사용하는걸로 되어있는데 렌덤으로 바꾸던지 하면 될듯 (.random() 사용)

        val existingSetting = aiAssistantSettingRepository.findByUserId(userId)
            .orElseThrow {
                logger.error("AI Assistant 설정을 찾을 수 없습니다. userId: {}", userId)
                IllegalStateException("AI Assistant 설정이 존재하지 않습니다.")
            }

        val categories = existingSetting.categories.firstOrNull()
            ?: throw IllegalStateException("카테고리 설정이 비어 있습니다. userId: $userId").also {
                logger.error("카테고리 설정이 비어 있습니다. userId: {}", userId)
            }

        val writingStyle = existingSetting.writingStyle

        // 테스트 Time을 쓰지 않는 시점에 val로 변경
        var formattedDateTime = try {
            OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"))
        } catch (e: Exception) {
            logger.error("날짜 포맷 중 오류 발생: ${e.message}", e)
            throw IllegalStateException("날짜 포맷 에러", e)
        }

        logger.info("현재 시각을 확인했습니다. nowTime : {}", formattedDateTime)
        formattedDateTime = "2025041211" //테스트용 시간대 설정

        val existingVoiceS3Key = "ai-assistant/${voicepack.name}/$formattedDateTime/${categories.description}/${writingStyle.description}.wav"  // AI 비서 세팅에 맞춘 음성 합성 결과 경로
        val promptS3Key = "prompt/$formattedDateTime/${categories.description}/${writingStyle.description}.txt" // AI 비서 세팅에 맞춘 프롬프트 경로

        logger.info("S3 키 생성 완료: voiceKey=$existingVoiceS3Key, promptKey=$promptS3Key")

        /**
        * s3에 이미 저장된 (사용자의 세팅과 동일한) 음성이 있나 확인
        */

        if (doesS3ObjectExist(existingVoiceS3Key)) {
            logger.info("기존 음성 파일 존재 - 즉시 반환: userId={}, voicepackId={}, resultUrl={}", userId, voicepack.id, existingVoiceS3Key)

            return AiAssistantSynthesisSubmitResponse(
                id = null,
                message = "기존 음성이 존재하여 즉시 반환합니다.",
                resultUrl = existingVoiceS3Key
            )
        }

        // 추후 Redis 같은 캐시 시스템 사용하면 좋을듯
        val prompt: String = try {
            readPromptFromS3(promptS3Key)
        } catch (e: Exception) {
            logger.error("프롬프트 읽기 실패: key={}, error={}", promptS3Key, e.message)
            throw IllegalStateException("프롬프트를 불러오지 못했습니다.")
        }

        // AI 비서 이용하기 요청 엔티티 생성 및 저장
        val synthesisRequest = AiAssistantSynthesisRequest(
            user = user,
            voicepack = voicepack,
            prompt = prompt,
            categories = categories,
            writingStyle = writingStyle
        )

        val savedRequest = aiAssistantSynthesisRequestRepository.save(synthesisRequest) // 저장된 엔티티를 받음
        logger.info("AI 비서 음성 합성 요청 엔티티 생성 및 저장 완료: id={}, userId={}, voicepackId={}, status={}",
            savedRequest.id, userId, request.voicepackId, savedRequest.status)

        // RabbitMQ로 메시지 전송
        logger.info("RabbitMQ 메시지 전송 시도: id={}", savedRequest.id)
        val sendSuccess = sendSynthesisMessageToRabbitMQ(savedRequest, formattedDateTime)

        if (sendSuccess) {
            // 성공 시 처리
            logger.info("RabbitMQ 메시지 전송 성공: id={}", savedRequest.id)
            savedRequest.status = AiAssistantStatus.PROCESSING
            savedRequest.updatedAt = OffsetDateTime.now()
            aiAssistantSynthesisRequestRepository.save(savedRequest)
            logger.info("AI 비서 음성 합성 요청 상태 업데이트: id={}, status={}", savedRequest.id, SynthesisStatus.PROCESSING)

            val response = AiAssistantSynthesisSubmitResponse(
                id = savedRequest.id,
                message = "AI 비서 음성 합성 요청이 성공적으로 제출되었습니다. 완료 시 알림이 전송됩니다."
            )
            logger.info("AI 비서 음성 합성 요청 성공 응답 반환: id={}", savedRequest.id)
            return response
        } else {
            // 실패 시 처리
            logger.error("RabbitMQ 메시지 전송 실패: id={}", savedRequest.id)
            savedRequest.status = AiAssistantStatus.FAILED
            savedRequest.errorMessage = "MQ 메시지 전송 실패"
            savedRequest.updatedAt = OffsetDateTime.now()
            aiAssistantSynthesisRequestRepository.save(savedRequest)
            logger.info("AI 비서 음성 합성 요청 상태 업데이트: id={}, status={}, errorMessage={}", savedRequest.id, SynthesisStatus.FAILED, savedRequest.errorMessage)

            val response = AiAssistantSynthesisSubmitResponse(
                id = savedRequest.id,
                message = "AI 비서 음성 합성 요청 처리 중 오류가 발생했습니다. 나중에 다시 시도해주세요."
            )
            logger.warn("AI 비서 음성 합성 요청 실패 응답 반환: id={}", savedRequest.id)
            return response
        }
    }

    // --- S3에 AI 비서 음성 합성 결과물 존재 여부 확인 함수 ---
    private fun doesS3ObjectExist(s3Key: String): Boolean {
        return try {
            // HeadObject는 객체 메타데이터만 가져옴 (파일 다운로드 X, 효율적)
            val headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build()
            s3Client.headObject(headObjectRequest)
            true // 예외 없으면 객체 존재
        } catch (e: NoSuchKeyException) {
            false // NoSuchKeyException은 객체가 없다는 의미
        } catch (e: Exception) {
            // 기타 예외 발생 시 로깅 후 false 반환
            logger.error("이미 만들어진 AI 비서 음성 합성 결과물 존재 여부 확인중 오류 발생 : key={}, error={}", s3Key, e.message)
            false
        }
    }

    // S3에서 프롬프트 읽는 함수
    private fun readPromptFromS3(s3Key: String): String {
        // S3에서 객체(파일)를 가져오기 위한 요청 객체 생성
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName) // 설정된 버킷 이름 사용
            .key(s3Key)         // 파라미터로 받은 파일 경로 사용
            .build()

        // s3Client를 사용하여 객체를 InputStream 형태로 가져옴
        // .use 블록: 작업 완료 후 자동으로 리소스(InputStream 등)를 닫아줌
        return s3Client.getObject(getObjectRequest, ResponseTransformer.toInputStream()).use { inputStream ->
            // InputStream을 UTF-8 인코딩으로 텍스트를 읽을 수 있는 BufferedReader로 변환
            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                // 파일 전체 내용을 문자열로 읽어서 반환
                reader.readText()
            }
        }
    }

    private fun sendSynthesisMessageToRabbitMQ(synthesisRequest: AiAssistantSynthesisRequest, formettedDateTime: String): Boolean {
        try {
            // 메시지 생성
            val messageJson = objectMapper.writeValueAsString(
                mapOf(
                    "id" to synthesisRequest.id,
                    "userId" to synthesisRequest.user.id,
                    "voicepackName" to synthesisRequest.voicepack.name,
                    "prompt" to synthesisRequest.prompt,
                    "categories" to synthesisRequest.categories.description,
                    "writingStyle" to synthesisRequest.writingStyle.description,
                    "nowTime" to formettedDateTime,
                    "callbackUrl" to "/api/ai-assistant/synthesis/callback"
                )
            )

            // MQ로 메시지 전송
            logger.info("RabbitMQ 전송 시작: id={}", synthesisRequest.id)
            rabbitTemplate.convertAndSend("ai-assistant", messageJson)
            logger.info("MQ 메시지 전송 완료: id={}, queue={}", synthesisRequest.id, "ai-assistant")

            return true
        } catch (e: Exception) {
            logger.error("MQ 메시지 전송 중 오류 발생: ${e.message}", e)
            return false
        }
    }

    /**
     * AI 비서 음성 합성 콜백 처리
     */

    @Transactional
    fun handleAiAssistantCallback(callbackRequest: AiAssistantCallbackRequest) {
        logger.info("AI 비서 음성 합성 콜백 수신: id={}", callbackRequest.id)

        val synthesisRequest = aiAssistantSynthesisRequestRepository.findByIdOrNull(callbackRequest.id)
            ?: run {
                logger.error("콜백 처리 실패: 해당 id의 요청을 찾을 수 없음 - id={}", callbackRequest.id)
                return // 혹은 예외 던지기
            }

        // 이미 처리된 콜백인지 확인 (멱등성)
        if (synthesisRequest.status == AiAssistantStatus.COMPLETED || synthesisRequest.status == AiAssistantStatus.FAILED) {
            logger.warn("이미 처리된 콜백 요청입니다: id={}, status={}", callbackRequest.id, synthesisRequest.status)
            return
        }

        // 콜백 결과에 따라 상태 업데이트
        synthesisRequest.updatedAt = OffsetDateTime.now()
        if (callbackRequest.success) {
            synthesisRequest.status = AiAssistantStatus.COMPLETED
            synthesisRequest.resultUrl = callbackRequest.resultUrl // S3 Presigned URL 또는 직접 URL
            logger.info("음성 합성 성공 처리 완료: id={}, resultUrl={}",
                synthesisRequest.id, synthesisRequest.resultUrl)
            // TODO: 사용자에게 성공 알림 전송 (notificationService 사용)
            // notificationService.notifySynthesisComplete(synthesisRequest)
        } else {
            synthesisRequest.status = AiAssistantStatus.FAILED
            synthesisRequest.errorMessage = callbackRequest.errorMessage ?: "음성 합성 처리 실패 (원인 미상)"
            logger.error("음성 합성 실패 처리 완료: id={}, error={}",
                synthesisRequest.id, synthesisRequest.errorMessage)
            // TODO: 사용자에게 실패 알림 전송
            // notificationService.notifySynthesisFailed(synthesisRequest)
        }

        aiAssistantSynthesisRequestRepository.save(synthesisRequest)
    }

    /**
     * 음성 합성 상태 조회
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    fun getAiAssistantStatus(id: Long): AiAssistantStatusDto {
        logger.debug("AI 비서 음성 합성 상태 조회 요청: id={}", id)

        val synthesisRequest = aiAssistantSynthesisRequestRepository.findById(id)
            .orElseThrow {
                logger.warn("상태 조회 실패: 해당 id의 요청을 찾을 수 없음 - id={}", id)
                IllegalArgumentException("해당 id의 AI 비서 음성 합성 요청을 찾을 수 없습니다.")
            }

        logger.debug("AI 비서 음성 합성 상태 조회 성공: id={}, status={}", id, synthesisRequest.status)
        return AiAssistantStatusDto(
            id = synthesisRequest.id,
            status = synthesisRequest.status.name,
            resultUrl = synthesisRequest.resultUrl,
            errorMessage = synthesisRequest.errorMessage
        )
    }

}