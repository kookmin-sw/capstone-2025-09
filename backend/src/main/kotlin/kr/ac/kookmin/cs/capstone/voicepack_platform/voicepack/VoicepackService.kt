package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack

import kr.ac.kookmin.cs.capstone.voicepack_platform.user.UserRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.notification.NotificationService
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value

@Service
class VoicepackService(
    private val voicepackRepository: VoicepackRepository,
    private val userRepository: UserRepository,
    private val restTemplate: RestTemplate,
    private val notificationService: NotificationService,
    @Value("\${ai.model.service.url}") private val aiModelServiceUrl: String
) {
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
        
        withContext(Dispatchers.IO) {
            try {
                val aiModelResponse = restTemplate.postForObject(
                    "$aiModelServiceUrl/process",
                    AIModelRequest(
                        voicepackId = voicepack.id,
                        audioFiles = request.audioFiles,
                        options = request.options
                    ),
                    AIModelResponse::class.java
                ) ?: throw RuntimeException("AI Model returned null response")
                
                // TODO S3 저장은 모델 쪽에서 하거나, 이 자리에서 추가

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
        }
        
        return VoicepackConvertResponse(voicepack.id, voicepack.status.name)
    }
} 