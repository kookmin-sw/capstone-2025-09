package kr.ac.kookmin.cs.capstone.voicepack_platform.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer{

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**") // 모든 경로에 대해 CORS 설정 (추후 변경)
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("*")
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}