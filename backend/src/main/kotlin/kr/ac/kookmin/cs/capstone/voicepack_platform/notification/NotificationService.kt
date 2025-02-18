package kr.ac.kookmin.cs.capstone.voicepack_platform.notification

import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.Voicepack
import org.springframework.stereotype.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

@Service
class NotificationService(
    private val supabaseClient: SupabaseClient
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun notifyVoicepackComplete(voicepack: Voicepack) {
        coroutineScope.launch {
            supabaseClient.postgrest["voicepack_notifications"].insert(
                mapOf(
                    "voicepack_id" to voicepack.id,
                    "event_type" to "voicepack_complete"
                )
            )
        }
    }

    fun notifyVoicepackFailed(voicepack: Voicepack) {
        coroutineScope.launch {
            supabaseClient.postgrest["voicepack_notifications"].insert(
                mapOf(
                    "voicepack_id" to voicepack.id,
                    "event_type" to "voicepack_failed"
                )
            )
        }
    }
} 