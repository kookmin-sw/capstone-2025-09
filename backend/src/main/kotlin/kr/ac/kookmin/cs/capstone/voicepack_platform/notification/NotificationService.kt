package kr.ac.kookmin.cs.capstone.voicepack_platform.notification

import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.Voicepack
import org.springframework.stereotype.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable

@Serializable
private data class NotificationData(
    val voicepack_name: String,
    val created_by: Long,
    val event_type: String
)

@Service
class NotificationService(
    private val supabaseClient: SupabaseClient
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun notifyVoicepackComplete(voicepack: Voicepack) {
        coroutineScope.launch {
            supabaseClient.postgrest["voicepack_notifications"].insert(
                NotificationData(
                    voicepack_name = voicepack.packName,
                    created_by = voicepack.author.id,
                    event_type = "voicepack_complete"
                )
            )
        }
    }

    fun notifyVoicepackFailed(voicepack: Voicepack) {
        coroutineScope.launch {
            supabaseClient.postgrest["voicepack_notifications"].insert(
                NotificationData(
                    voicepack_name = voicepack.packName,
                    created_by = voicepack.author.id,
                    event_type = "voicepack_failed"
                )
            )
        }
    }
} 