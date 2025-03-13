package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.LogLevel
import kr.ac.kookmin.cs.capstone.voicepack_platform.notification.NotificationService
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.UserRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.request.VoicepackRequestStatus
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.request.VoicepackRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.request.VoicepackRequestRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class VoicepackService(
        private val voicepackRepository: VoicepackRepository,
        private val voicepackRequestRepository: VoicepackRequestRepository,
        private val userRepository: UserRepository,
        private val notificationService: NotificationService,
        @Value("\${ai.model.service.url}") private val aiModelServiceUrl: String
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

    // 1. 보이스팩 변환 요청
    @Transactional
    suspend fun convertVoicepack(
            userId: Long,
            request: VoicepackConvertRequest
    ): VoicepackConvertResponse {
        logger.info("보이스팩 변환 요청: userId={}, request={}", userId, request)
        val user = findUser(userId)
        validatePackName(request.name, user.id)

        // 2. 보이스팩 요청 엔티티 생성
        val voicepackRequest = VoicepackRequest(
            name = request.name,
            author = user,
            status = VoicepackRequestStatus.PROCESSING
        )
        voicepackRequestRepository.save(voicepackRequest)

        return try {
            processVoicepackConversion(voicepackRequest, request)
            notificationService.notifyVoicepackComplete(voicepackRequest)
            VoicepackConvertResponse(voicepackRequest.id, VoicepackRequestStatus.COMPLETED.name)
        } catch (e: Exception) {
            voicepackRequest.status = VoicepackRequestStatus.FAILED
            voicepackRequestRepository.save(voicepackRequest)
            notificationService.notifyVoicepackFailed(voicepackRequest)
            VoicepackConvertResponse(voicepackRequest.id, VoicepackRequestStatus.FAILED.name)
        }
    }

    // 보이스팩 변환 처리
    @Transactional
    private suspend fun processVoicepackConversion(
            voicepackRequest: VoicepackRequest,
            request: VoicepackConvertRequest
    ): VoicepackConvertResponse {
        try {
            val aiModelRequest = AIModelRequest(
                    voicepackId = voicepackRequest.id,
                    voiceFile = request.voiceFile
            )
            
            logger.info("AI 모델 요청: requestId={}, request={}", voicepackRequest.id, aiModelRequest)
            
            val response = httpClient.post("$aiModelServiceUrl/process") {
                contentType(ContentType.Application.Json)
                setBody(aiModelRequest)
            }

            val aiModelResponse: AIModelResponse = response.body()
            
            logger.info("AI 모델 응답: requestId={}, response={}", voicepackRequest.id, aiModelResponse)

            // 변환 성공 시 Voicepack 엔티티 생성
            voicepackRequest.status = VoicepackRequestStatus.COMPLETED
            voicepackRequest.s3Path = aiModelResponse.outputPath
            voicepackRequest.completedAt = OffsetDateTime.now()
            voicepackRequestRepository.save(voicepackRequest)

            // 완성된 보이스팩 생성
            val voicepack = Voicepack(
                name = voicepackRequest.name,
                author = voicepackRequest.author,
                s3Path = aiModelResponse.outputPath
            )
            voicepackRepository.save(voicepack)

        } catch (e: Exception) {
            logger.error("AI 모델 처리 실패: requestId={}, error={}", voicepackRequest.id, e.message)
            throw e
        }

        return VoicepackConvertResponse(voicepackRequest.id, voicepackRequest.status.name)
    }

    // 보이스팩 요청 목록 조회
    fun getVoicepackRequests(userId: Long): List<VoicepackRequestDto> {
        val requests = voicepackRequestRepository.findByAuthorId(userId)
        return requests.map { VoicepackRequestDto.fromEntity(it) }
    }

    // 보이스팩 요청 상태 조회
    fun getVoicepackRequestStatus(userId: Long, requestId: Long): VoicepackRequestDto {
        val request = voicepackRequestRepository.findByIdAndAuthorId(requestId, userId)
            ?: throw IllegalArgumentException("요청을 찾을 수 없습니다")
        return VoicepackRequestDto.fromEntity(request)
    }

    // 완성된 보이스팩 목록 조회
    fun getVoicepacks(userId: Long): List<VoicepackDto> {
        val voicepacks = voicepackRepository.findByAuthorId(userId)
        return voicepacks.map { VoicepackDto.fromEntity(it) }
    }

    // 완성된 보이스팩 조회
    fun getVoicepack(userId: Long, voicepackId: Long): VoicepackDto {
        val voicepack = voicepackRepository.findByIdAndAuthorId(voicepackId, userId)
            ?: throw IllegalArgumentException("보이스팩을 찾을 수 없습니다")
        return VoicepackDto.fromEntity(voicepack)
    }

    private fun findUser(userId: Long) =
            userRepository.findById(userId).orElseThrow {
                IllegalArgumentException("User not found")
            }

    private fun validatePackName(packName: String, userId: Long) {
        if (voicepackRepository.existsByNameAndAuthorId(packName, userId)) {
            logger.error("이미 같은 이름의 보이스팩이 존재합니다: packName={}, userId={}", packName, userId)
            throw IllegalArgumentException("이미 같은 이름의 보이스팩이 존재합니다")
        }
    }
}
