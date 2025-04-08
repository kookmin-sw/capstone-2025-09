package kr.ac.kookmin.cs.capstone.voicepack_platform.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val requestLoggingInterceptor: RequestLoggingInterceptor
) : WebMvcConfigurer{

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**") // 모든 경로에 대해 CORS 설정 (추후 변경)
            .allowedOrigins(
                "https://capstone-2025-09.vercel.app",
                "https://vocalab.kro.kr",
                "http://localhost:3000",
                "https://capstone-2025-09-zeta.vercel.app"
            )
            .allowedMethods("*")
            .allowedHeaders("*")
            .exposedHeaders("Location")
            .allowCredentials(true)
    }
    
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(requestLoggingInterceptor)
            .addPathPatterns("/**")  // 모든 경로에 대해 인터셉터 적용
            .excludePathPatterns("/error")  // 에러 페이지는 제외
    }
}