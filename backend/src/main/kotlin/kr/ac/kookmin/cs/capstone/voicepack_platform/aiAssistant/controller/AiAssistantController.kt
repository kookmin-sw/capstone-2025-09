package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.AiAssistantSettingRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.response.AiAssistantSettingResponse
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.service.AiAssistantService
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto.VoicepackSynthesisStatusDto
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus

@RestController
@RequestMapping("/api/ai-assistant")
class AiAssistantController(
    private val aiAssistantService: AiAssistantService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    // AI 비서 설정 저장 및 업데이트

    @Operation(
        summary = "AI 비서 세팅",
        description = "AI 비서 이용을 위해 사용자별 초기 설정을 진행합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "세팅 성공",
                content = [Content(schema = Schema(implementation = VoicepackSynthesisStatusDto::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "요청 데이터가 유효하지 않음 (잘못된 문체 혹은 카테고리 없음)"
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없거나 관련 데이터 없음"
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )

    @PostMapping("/settings")
    fun saveOrUpdateAiAssistantSettings(
        @SessionAttribute("userId") userId: Long,
        @RequestBody request: AiAssistantSettingRequest
    ): ResponseEntity<Any> {
        return try {
            // 서비스 메소드 호출하여 설정 저장/업데이트 로직 수행
            val savedSetting = aiAssistantService.saveOrUpdateAiAssistantSettings(userId, request)
            // 성공 시, 저장된 내용을 응답 DTO로 변환 (선택 사항)
            val response = AiAssistantSettingResponse(
                userId = savedSetting.user.id,
                voicepackId = savedSetting.voicepackId,
                writingStyle = savedSetting.writingStyle,
                categories = savedSetting.categories
            )
            logger.info("Successfully updated settings for user ID: $userId")
            ResponseEntity.ok(response)

        } catch (e: NoSuchElementException) {
            logger.warn("Failed to update settings: ${e.message}")
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))

        } catch (e: IllegalArgumentException) {
            logger.warn("Failed to update settings due to invalid argument: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))

        } catch (e: Exception) {
            logger.error("Internal server error during setting update for user ID $userId: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "An internal error occurred."))
        }
    }
}