package kr.ac.kookmin.cs.capstone.voicepack_platform.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime


@Configuration
class SupabaseConfig {
    @Value("\${supabase.url}")
    private lateinit var supabaseUrl: String

    @Value("\${supabase.key}")
    private lateinit var supabaseKey: String

    @Bean
    fun supabaseClient(): SupabaseClient {
        return createSupabaseClient(supabaseUrl, supabaseKey) {
            install(Postgrest)
            install(Realtime)
        }
    }
}
