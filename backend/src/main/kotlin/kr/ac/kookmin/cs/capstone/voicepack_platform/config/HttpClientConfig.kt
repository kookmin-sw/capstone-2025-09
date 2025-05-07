package kr.ac.kookmin.cs.capstone.voicepack_platform.config

import io.ktor.client.* 
import io.ktor.client.engine.java.* 
import io.ktor.client.plugins.contentnegotiation.* 
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.* 
import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HttpClientConfig {

    @Bean
    fun ktorJson(): Json {
        return Json { 
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
    }

    @Bean
    fun httpClient(json: Json): HttpClient {
        return HttpClient(Java) {
            install(ContentNegotiation) { 
                json(json)
            }
            
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }
            
            // 필요하다면 추가적인 엔진 설정 (타임아웃 등) 가능
            // engine {
            //     connectTimeout = 10_000
            //     socketTimeout = 10_000
            // }
        }
    }
} 