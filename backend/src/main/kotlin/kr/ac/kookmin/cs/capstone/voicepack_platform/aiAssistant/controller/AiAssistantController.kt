package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.callback.AiAssistantCallbackRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.setting.AiAssistantSettingRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.callback.AiAssistantStatusDto
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.synthesis.AiAssistantSynthesisSubmitRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.response.setting.AiAssistantSettingResponse
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.service.AiAssistantService
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto.VoicepackSynthesisStatusDto
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto.VoicepackSynthesisSubmitResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/api/ai-assistant")
class AiAssistantController(
    private val aiAssistantService: AiAssistantService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * AI 비서 설정 저장 및 업데이트
     */

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

    /**
     * AI 비서 음성 합성
     */

    @Operation(
        summary = "AI 비서 이용하기 요청 (비동기)",
        description = "사용자가 보이스팩을 기반으로 AI 비서 스크립트 TTS 생성을 비동기적으로 요청합니다.",
        responses = [
            ApiResponse(
                responseCode = "202",
                description = "생성 요청 성공 (처리 시작됨)",
                content = [Content(schema = Schema(implementation = VoicepackSynthesisSubmitResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청"
            ),
            ApiResponse(
                responseCode = "403",
                description = "사용 권한 없음"
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류 (Lambda 호출 실패 등)"
            )
        ]
    )
    @PostMapping("/synthesis")
    suspend fun submitSynthesisRequest(
        @SessionAttribute("userId") userId: Long,
        @RequestBody request: AiAssistantSynthesisSubmitRequest
    ): ResponseEntity<Any> {
        try {
            val response = aiAssistantService.submitSynthesisRequest(userId, request)

            // Location 헤더 생성 (상태 조회 엔드포인트 URL)
            val locationUri = ServletUriComponentsBuilder
                .fromCurrentContextPath() // 현재 요청의 기본 URL (e.g., http://localhost:8080)
                .path("/api/ai-assistant/synthesis/status/{id}") // 상태 조회 경로 추가
                .buildAndExpand(response.id) // 경로 변수({id}) 채우기
                .toUri()

            return ResponseEntity.accepted().location(locationUri).body(response)
        } catch (e: SecurityException) {
            logger.error("AI 비서 음성 합성 권한 오류: {}", e.message)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            logger.error("AI 비서 음성 합성 잘못된 요청: {}", e.message)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: RuntimeException) {
            logger.error("AI 비서 음성 합성 요청 제출 오류: {}", e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("AI 비서 음성 합성 중 예상치 못한 오류: {}", e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "음성 합성 요청 중 오류가 발생했습니다."))
        }
    }

    // AI 비서 음성 합성 시 모델로부터 부를 callback url
    @Operation(
        summary = "AI 비서 음성 합성 콜백",
        description = "AI 비서 음성 합성 처리 완료 후 Cloud Run에서 호출하는 내부 콜백 엔드포인트입니다.",
        deprecated = true
    )
    @PostMapping("/synthesis/callback")
    fun handleSynthesisCallback(
        @Parameter(description = "요청 ID") @RequestParam id: Long,
        @Parameter(description = "처리 성공 여부") @RequestParam success: Boolean,
        @Parameter(description = "성공 시 결과 URL") @RequestParam(required = false) resultUrl: String?,
        @Parameter(description = "실패 시 오류 메시지") @RequestParam(required = false) errorMessage: String?
    ): ResponseEntity<Any> {
        // RequestParam으로 받은 값들을 사용하여 DTO 객체 생성
        val callbackRequest = AiAssistantCallbackRequest(
            id = id,
            success = success,
            resultUrl = resultUrl,
            errorMessage = errorMessage
        )

        try {
            aiAssistantService.handleAiAssistantCallback(callbackRequest)
            return ResponseEntity.ok().build() // 성공 시 200 OK만 반환
        } catch (e: Exception) {
            // 콜백 처리 중 오류 발생 시 로깅만 하고 500 에러 반환 (Cloud Run 재시도 방지 목적)
            logger.error("AI 비서 음성 합성 콜백 처리 중 오류 발생: id={}, error={}", callbackRequest.id, e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "콜백 처리 중 오류 발생"))
        }
    }

    @Operation(
        summary = "AI 비서 음성 합성 상태 조회 (Polling)",
        description = "제출된 AI 비서 음성 합성 요청의 현재 상태와 결과(완료 시)를 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = AiAssistantStatusDto::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "해당 Job ID의 요청을 찾을 수 없음"
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )
    @GetMapping("/synthesis/status/{id}")
    fun getAiAssistantStatus(
        @Parameter(description = "조회할 요청의 ID") @PathVariable id: Long
    ): ResponseEntity<Any> {
        try {
            val statusDto = aiAssistantService.getAiAssistantStatus(id)
            return ResponseEntity.ok(statusDto)
        } catch (e: IllegalArgumentException) {
            logger.warn("AI 비서 음성 합성 상태 조회 실패: {}", e.message)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("AI 비서 음성 합성 상태 조회 중 오류 발생: id={}, error={}", id, e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "상태 조회 중 오류 발생"))
        }
    }

}