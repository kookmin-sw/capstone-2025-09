package kr.ac.kookmin.cs.capstone.voicepack_platform.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer{

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**") // 모든 경로에 대해 CORS 설정 (추후 변경)
            .allowedOrigins(
                "https://capstone-2025-09.vercel.app",
                "https://vocalab.kro.kr"
            )
            .allowedMethods("*")
            .allowedHeaders("*")
            .exposedHeaders("Location")
            .allowCredentials(true)
    }
}