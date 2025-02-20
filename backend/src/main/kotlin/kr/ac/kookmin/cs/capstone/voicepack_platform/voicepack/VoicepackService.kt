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
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VoicepackService(
        private val voicepackRepository: VoicepackRepository,
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

    suspend fun convertVoicepack(
            userId: Long,
            request: VoicepackConvertRequest
    ): VoicepackConvertResponse {
        val user = findUser(userId)
        validatePackName(request.packName, user.id)

        val voicepack =
                Voicepack(
                        packName = request.packName,
                        author = user,
                        s3Path = "",
                        status = VoicepackStatus.PROCESSING
                )

        return try {
            processVoicepackConversion(voicepack, request)
            notificationService.notifyVoicepackComplete(voicepack)
            VoicepackConvertResponse(voicepack.id, VoicepackStatus.COMPLETED.name)
        } catch (e: Exception) {
            notificationService.notifyVoicepackFailed(voicepack)
            VoicepackConvertResponse(voicepack.id, VoicepackStatus.FAILED.name)
        }
    }

    @Transactional
    private suspend fun processVoicepackConversion(
            voicepack: Voicepack,
            request: VoicepackConvertRequest
    ): VoicepackConvertResponse {
        try {
            voicepackRepository.save(voicepack)

            val aiModelRequest = AIModelRequest(
                    voicepackId = voicepack.id,
                    audioFiles = request.audioFiles,
                    options = request.options
            )
            
            logger.info("AI 모델 요청: voicepackId={}, request={}", voicepack.id, aiModelRequest)
            
            val response = httpClient.post("$aiModelServiceUrl/process") {
                contentType(ContentType.Application.Json)
                setBody(aiModelRequest)
            }
            val aiModelResponse: AIModelResponse = response.body()
            
            logger.info("AI 모델 응답: voicepackId={}, response={}", voicepack.id, aiModelResponse)

            voicepack.status = VoicepackStatus.COMPLETED
            voicepack.s3Path = aiModelResponse.outputPath
            voicepackRepository.save(voicepack)
        } catch (e: Exception) {
            logger.error("AI 모델 처리 실패: voicepackId={}, error={}", voicepack.id, e.message)
            voicepackRepository.delete(voicepack)
            throw e
        }

        return VoicepackConvertResponse(voicepack.id, voicepack.status.name)
    }

    private fun findUser(userId: Long) =
            userRepository.findById(userId).orElseThrow {
                IllegalArgumentException("User not found")
            }

    private fun validatePackName(packName: String, userId: Long) {
        if (voicepackRepository.existsByPackNameAndAuthorId(packName, userId)) {
            throw IllegalArgumentException("이미 같은 이름의 보이스팩이 존재합니다")
        }
    }
}
