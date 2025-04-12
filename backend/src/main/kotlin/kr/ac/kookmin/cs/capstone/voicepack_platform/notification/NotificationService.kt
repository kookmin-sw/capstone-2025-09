package kr.ac.kookmin.cs.capstone.voicepack_platform.notification

import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.convert.VoicepackRequest
import org.springframework.stereotype.Service
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable

inline fun <reified T> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)

@Serializable
private data class NotificationData(
    val voicepack_name: String,
    val created_by: Long,
    val event_type: String,
    val created_at: String
)


@Service
class NotificationService(

) {
    private val log = logger()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun notifyVoicepackComplete(voicepackRequest: VoicepackRequest) {
        log.info("보이스팩 변환 완료 알림 전송: voicepackRequest={}", voicepackRequest)
    }

    fun notifyVoicepackFailed(voicepackRequest: VoicepackRequest) {
        log.info("보이스팩 변환 실패 알림 전송: voicepackRequest={}", voicepackRequest)
    }
} 