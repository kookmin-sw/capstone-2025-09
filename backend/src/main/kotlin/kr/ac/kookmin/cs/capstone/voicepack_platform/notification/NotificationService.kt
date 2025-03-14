package kr.ac.kookmin.cs.capstone.voicepack_platform.notification

import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.request.VoicepackRequest
import org.springframework.stereotype.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Serializable
private data class NotificationData(
    val voicepack_name: String,
    val created_by: Long,
    val event_type: String,
    val created_at: String
)

@Service
class NotificationService(
    private val supabaseClient: SupabaseClient
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun notifyVoicepackComplete(voicepackRequest: VoicepackRequest) {
        coroutineScope.launch {
            supabaseClient.postgrest["voicepack_notifications"].insert(
                NotificationData(
                    voicepack_name = voicepackRequest.name,
                    created_by = voicepackRequest.author.id,
                    event_type = "voicepack_complete",
                    created_at = voicepackRequest.completedAt!!.toInstant().toString()
                )
            )
        }
    }

    fun notifyVoicepackFailed(voicepackRequest: VoicepackRequest) {
        coroutineScope.launch {
            supabaseClient.postgrest["voicepack_notifications"].insert(
                NotificationData(
                    voicepack_name = voicepackRequest.name,
                    created_by = voicepackRequest.author.id,
                    event_type = "voicepack_failed",
                    created_at = voicepackRequest.completedAt!!.toInstant().toString()
                )
            )
        }
    }
} 