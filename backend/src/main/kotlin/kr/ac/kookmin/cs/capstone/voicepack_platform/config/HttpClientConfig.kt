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
    fun httpClient(): HttpClient {
        return HttpClient(Java) {
            install(ContentNegotiation) { 
                json(Json { 
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true // 알 수 없는 키 무시
                })
            }
            
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL // 필요에 따라 로그 레벨 조절 (INFO, HEADERS, BODY 등)
                sanitizeHeader { header -> header == HttpHeaders.Authorization } // 민감한 헤더 로깅 제외
            }
            
            // 필요하다면 추가적인 엔진 설정 (타임아웃 등) 가능
            // engine {
            //     connectTimeout = 10_000
            //     socketTimeout = 10_000
            // }
        }
    }
} 