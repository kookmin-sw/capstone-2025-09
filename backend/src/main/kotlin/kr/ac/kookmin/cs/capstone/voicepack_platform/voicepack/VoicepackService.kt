package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack

import kr.ac.kookmin.cs.capstone.voicepack_platform.user.UserRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.notification.NotificationService
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.beans.factory.annotation.Value
import kotlinx.coroutines.Dispatchers
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.call.body

@Service
class VoicepackService(
    private val voicepackRepository: VoicepackRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService,
    @Value("\${ai.model.service.url}") private val aiModelServiceUrl: String
) {
    private val httpClient = HttpClient(Java) {
        install(ContentNegotiation) {
            json()
        }
    }

    @Transactional
    suspend fun convertVoicepack(userId: Long, request: VoicepackConvertRequest): VoicepackConvertResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }
            
        val voicepack = Voicepack(
            packName = request.packName,
            author = user,
            s3Path = "",
            status = VoicepackStatus.PROCESSING
        ).also { voicepackRepository.save(it) }
        
        try {
            val response = httpClient.post("$aiModelServiceUrl/process") {
                contentType(ContentType.Application.Json)
                setBody(AIModelRequest(
                    voicepackId = voicepack.id,
                    audioFiles = request.audioFiles,
                    options = request.options
                ))
            }
            val aiModelResponse: AIModelResponse = response.body()

            voicepack.apply { 
                status = VoicepackStatus.COMPLETED
                s3Path = aiModelResponse.outputPath
            }.also { voicepackRepository.save(it) }
            
            notificationService.notifyVoicepackComplete(voicepack)
            
        } catch (e: Exception) {
            voicepack.apply { 
                status = VoicepackStatus.FAILED 
            }.also { voicepackRepository.save(it) }
            notificationService.notifyVoicepackFailed(voicepack)
        }
        
        return VoicepackConvertResponse(voicepack.id, voicepack.status.name)
    }
} 