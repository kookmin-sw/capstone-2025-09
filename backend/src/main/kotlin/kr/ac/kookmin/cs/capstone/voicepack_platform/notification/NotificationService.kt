package kr.ac.kookmin.cs.capstone.voicepack_platform.notification

import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.Voicepack
import org.springframework.stereotype.Service
import org.springframework.jdbc.core.JdbcTemplate

@Service
class NotificationService(
    private val jdbcTemplate: JdbcTemplate
) {
    // TODO 실제로 동작하는지 모름, Supabase는 Dependency 다를 수 있음.
    fun notifyVoicepackComplete(voicepack: Voicepack) {
        jdbcTemplate.execute(
            "NOTIFY voicepack_complete, '${voicepack.id}'"
        )
    }
    
    fun notifyVoicepackFailed(voicepack: Voicepack) {
        jdbcTemplate.execute(
            "NOTIFY voicepack_failed, '${voicepack.id}'"
        )
    }
} 