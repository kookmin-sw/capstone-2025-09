package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.transaction.annotation.Transactional
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.setting.AiAssistantSettingRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.synthesis.AiAssistantSynthesisSubmitRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.entity.AiAssistantSetting
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.entity.AiAssistantSynthesisRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums.Categories
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums.WritingStyle
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.repository.AiAssistantSettingRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.repository.AiAssistantSynthesisRequestRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.UserRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.VoicepackRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRightRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.*
import kotlin.NoSuchElementException
import org.springframework.beans.factory.annotation.Value
import java.time.format.DateTimeFormatter
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.response.synthesis.AiAssistantMultiSynthesisResponse
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.response.synthesis.AiAssistantMultiSynthesisStatusResponse
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.callback.AiAssistantJobCallbackRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.message_queue.AiAssistantSynthesisMqRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.entity.AiAssistantSynthesisJob
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.repository.AiAssistantSynthesisJobRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.entity.SynthesisStatus
import java.time.LocalDateTime

@Service
class AiAssistantService(
    private val userRepository: UserRepository,
    private val voicepackRepository: VoicepackRepository,
    private val aiAssistantSettingRepository: AiAssistantSettingRepository,
    private val voicepackUsageRightRepository: VoicepackUsageRightRepository,
    private val aiAssistantSynthesisRequestRepository: AiAssistantSynthesisRequestRepository,
    private val aiAssistantSynthesisJobRepository: AiAssistantSynthesisJobRepository,
    @Value("\${aws.s3.bucket-name}") private val bucketName: String,
    private val objectMapper: ObjectMapper,
    private val rabbitTemplate: RabbitTemplate,
    private val s3Client: S3Client,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * AI 비서 설정 저장 및 업데이트
     */
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

    /**
     * AI 비서 음성 합성 요청 처리 (다중 카테고리)
     */
    @Transactional
    fun submitSynthesisRequest(userId: Long, request: AiAssistantSynthesisSubmitRequest): AiAssistantMultiSynthesisResponse {
        logger.info("AI 비서 음성 합성 요청 시작 - User ID: {}, Request: {}", userId, request)

        val user = userRepository.findById(userId).orElseThrow{
            IllegalArgumentException("User not found with ID: $userId")
        }

        val voicepack =  voicepackRepository.findById(request.voicepackId).orElseThrow {
            IllegalArgumentException("Voicepack not found with ID: ${request.voicepackId}")
        }

        logger.info("요청 정보 확인 완료: userId={}, voicepackId={}, voicepackName={}", userId, voicepack.id, voicepack.name)

        // TODO: 사용권 확인 로직 강화 (사용자가 이 보이스팩에 대한 사용권을 가지고 있는지 확인)
        if (!voicepackUsageRightRepository.existsByUserIdAndVoicepackId(userId, request.voicepackId)) {
            logger.warn("사용 권한 없는 보이스팩 합성 시도: userId={}, voicepackId={}", userId, request.voicepackId)
            throw SecurityException("해당 보이스팩에 대한 사용 권한이 없습니다.")
        }

        val settings = aiAssistantSettingRepository.findByUserId(userId)
            .orElseThrow { IllegalArgumentException("AI Assistant settings not found for user ID: $userId") }

        if (settings.categories.isEmpty()) {
            throw IllegalArgumentException("No categories selected in AI Assistant settings.")
        }

        val synthesisRequest = AiAssistantSynthesisRequest(user = user)
        aiAssistantSynthesisRequestRepository.save(synthesisRequest)
        logger.debug("AiAssistantSynthesisRequest 생성됨 - ID: {}", synthesisRequest.id)

        val writingStyleEnum = settings.writingStyle
        
         // 테스트 Time을 쓰지 않는 시점에 val로 변경
         var formattedDateTime = try {
            OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"))
        } catch (e: Exception) {
            logger.error("날짜 포맷 중 오류 발생: ${e.message}", e)
            throw IllegalStateException("날짜 포맷 에러", e)
        }

        logger.info("현재 시각을 확인했습니다. nowTime : {}", formattedDateTime)
        formattedDateTime = "2025041211" //테스트용 시간대 설정
        
        val currentPromptTimeSlot = formattedDateTime

        settings.categories.forEach<Categories> { categoryEnum ->
            logger.debug("[Request ID: {}] Category {} 처리 시작...", synthesisRequest.id, categoryEnum.description)

            val promptS3Key = "prompt/$currentPromptTimeSlot/${categoryEnum.description}/${writingStyleEnum.description}.txt"
            val resultS3Key = "ai-assistant/${voicepack.name}/$currentPromptTimeSlot/${categoryEnum.description}/${writingStyleEnum.description}.wav"

            val existingJobOpt = aiAssistantSynthesisJobRepository.findTopByVoicePackIdAndCategoryAndWritingStyleAndPromptTimeSlotAndStatusInOrderByCreatedAtDesc(
                voicepack.id,
                categoryEnum,
                writingStyleEnum,
                currentPromptTimeSlot,
                listOf(SynthesisStatus.PENDING, SynthesisStatus.PROCESSING, SynthesisStatus.SUCCESS) // 조회 조건 추가
            )

            val jobToAssociate: AiAssistantSynthesisJob

            if (existingJobOpt.isPresent) {
                jobToAssociate = existingJobOpt.get()
                logger.info("[Request ID: {}] 기존 Job 재사용 - Job ID: {}, Status: {}, Category: {}", synthesisRequest.id, jobToAssociate.id, jobToAssociate.status, categoryEnum.description)
            } else {
                logger.info("[Request ID: {}] 새로운 Job 생성 필요 - Category: {}", synthesisRequest.id, categoryEnum.description)
                var newJob = AiAssistantSynthesisJob(
                    voicePack = voicepack,
                    category = categoryEnum,
                    writingStyle = writingStyleEnum,
                    promptS3Key = promptS3Key,
                    promptTimeSlot = currentPromptTimeSlot,
                    resultS3Key = resultS3Key,
                    status = SynthesisStatus.PENDING
                )
                newJob = aiAssistantSynthesisJobRepository.save(newJob)
                jobToAssociate = newJob
                logger.debug("[Request ID: {}] 새 Job 생성됨 - ID: {}", synthesisRequest.id, jobToAssociate.id)

                var promptContent: String? = null
                var mqSendSuccess = false
                try {
                    promptContent = readPromptFromS3(promptS3Key)
                    logger.debug("[Job ID: {}] S3 프롬프트 읽기 성공", newJob.id)

                    val mqDto = AiAssistantSynthesisMqRequest(
                        jobId = newJob.id!!,
                        prompt = promptContent,
                        category = categoryEnum.description,
                        writingStyle = writingStyleEnum.description,
                        voicepackName = voicepack.name,
                        nowTime = currentPromptTimeSlot
                    )
                    val messageJson = objectMapper.writeValueAsString(mqDto)
                    rabbitTemplate.convertAndSend("ai-assistant", messageJson) // MQ 메시지 발행
                    logger.info("[Job ID: {}] MQ 메시지 발행 성공", newJob.id)
                    mqSendSuccess = true
                } catch (e: NoSuchKeyException) { 
                    logger.warn("[Job ID: {}] S3에서 프롬프트 파일을 찾을 수 없습니다. Key: {}", newJob.id, promptS3Key)
                    // mqSendSuccess는 false로 유지되어 finally에서 FAILURE 처리됨
                } catch (e: Exception) {
                    // 그 외 S3 읽기 또는 MQ 발행 중 예외 발생 시 로깅
                    logger.error("[Job ID: {}] 프롬프트 처리 또는 MQ 발행 중 예상치 못한 오류 발생: {}", newJob.id, e.message, e)
                } finally {
                    newJob.status = if (mqSendSuccess) SynthesisStatus.PROCESSING else SynthesisStatus.FAILURE
                    aiAssistantSynthesisJobRepository.save(newJob)
                    logger.debug("[Job ID: {}] Job 상태 업데이트: {}", newJob.id, newJob.status)
                }
            }

            synthesisRequest.jobs.add(jobToAssociate)
            synthesisRequest.updatedAt = LocalDateTime.now()
        }

        // 모든 카테고리 요청 후 최종 Request 상태 저장
        aiAssistantSynthesisRequestRepository.save(synthesisRequest)

        logger.info("AI 비서 음성 합성 요청 처리 완료 - Request ID: {}", synthesisRequest.id)
        return AiAssistantMultiSynthesisResponse(requestId = synthesisRequest.id!!)
    }

    private fun readPromptFromS3(s3Key: String): String {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(s3Key)
            .build()

        return s3Client.getObject(getObjectRequest, ResponseTransformer.toInputStream()).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                reader.readText()
            }
        }
    }

    @Transactional(readOnly = true)
    fun getSynthesisRequestStatus(requestId: Long): AiAssistantMultiSynthesisStatusResponse {
        logger.debug("AI 비서 음성 합성 상태 조회 - Request ID: {}", requestId)
        val request = aiAssistantSynthesisRequestRepository.findById(requestId)
            .orElseThrow { IllegalArgumentException("Synthesis request not found with ID: $requestId") }

        val overallStatus = request.getOverallStatus()
        var results: Map<String, String>? = null

        if (overallStatus == SynthesisStatus.SUCCESS) {
            results = request.jobs.associate { job ->
                job.category.description to job.resultS3Key
            }
            logger.debug("상태 조회 결과: SUCCESS, 결과 개수: {}", results.size ?: 0)
        } else {
            logger.debug("상태 조회 결과: {}", overallStatus)
        }

        return AiAssistantMultiSynthesisStatusResponse(status = overallStatus, results = results)
    }

    @Transactional
    fun handleSynthesisCallback(callbackDto: AiAssistantJobCallbackRequest) {
        logger.info("AI 비서 음성 합성 콜백 수신 - Job ID: {}, Success: {}", callbackDto.jobId, callbackDto.success)
        val job = aiAssistantSynthesisJobRepository.findById(callbackDto.jobId)
            .orElseThrow {
                logger.warn("콜백 처리 실패: Job ID {} 를 찾을 수 없습니다.", callbackDto.jobId)
                IllegalArgumentException("Synthesis job not found with ID: ${callbackDto.jobId}")
            }

        if (job.status == SynthesisStatus.SUCCESS || job.status == SynthesisStatus.FAILURE) {
            logger.warn("이미 처리된 Job에 대한 콜백 수신됨 - Job ID: {}, 현재 상태: {}, 성공 여부: {}", job.id, job.status, callbackDto.success)
            return
        }

        if (callbackDto.success) {
            job.status = SynthesisStatus.SUCCESS
            if (callbackDto.resultS3Key == null) {
                logger.error("콜백 오류: 성공 상태이지만 resultS3Key가 null 입니다 - Job ID: {}", job.id)
                job.status = SynthesisStatus.FAILURE
            } else {
                logger.info("Job {} 상태 SUCCESS로 업데이트됨. Result Key: {}", job.id, callbackDto.resultS3Key)
            }
        } else {
            logger.warn("Job {} 상태 FAILURE로 업데이트됨.", job.id)
        }

        aiAssistantSynthesisJobRepository.save(job)
        logger.debug("Job {} 성공 여부 업데이트 완료: {}", job.id, job.status)
    }
}