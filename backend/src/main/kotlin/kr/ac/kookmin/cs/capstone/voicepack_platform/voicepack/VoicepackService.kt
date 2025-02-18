package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.notification.NotificationService
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.UserRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.*
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
    private val httpClient = HttpClient(Java) { install(ContentNegotiation) { json() } }

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

            val response =
                    httpClient.post("$aiModelServiceUrl/process") {
                        contentType(ContentType.Application.Json)
                        setBody(
                                AIModelRequest(
                                        voicepackId = voicepack.id,
                                        audioFiles = request.audioFiles,
                                        options = request.options
                                )
                        )
                    }
            val aiModelResponse: AIModelResponse = response.body()

            voicepack.status = VoicepackStatus.COMPLETED
            voicepack.s3Path = aiModelResponse.outputPath
            voicepackRepository.save(voicepack)
        } catch (e: Exception) {
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
