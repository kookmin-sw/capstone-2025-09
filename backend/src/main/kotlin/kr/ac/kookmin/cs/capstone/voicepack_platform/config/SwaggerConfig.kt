package kr.ac.kookmin.cs.capstone.voicepack_platform.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .addServersItem(Server().url("https://vocalab.kro.kr"))
        .addServersItem(Server().url("http://localhost:8080"))
        .components(Components())
        .info(
            Info()
                .title("보이스팩 플랫폼 API")
                .description("보이스팩 플랫폼 API 문서")
                .version("v1.0.0")
        )
} 