package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.setting.AiAssistantSettingRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.response.setting.AiAssistantSettingResponse
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.service.AiAssistantService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.response.synthesis.AiAssistantMultiSynthesisResponse
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.response.synthesis.AiAssistantMultiSynthesisStatusResponse
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.callback.AiAssistantJobCallbackRequest

@RestController
@RequestMapping("/api/ai-assistant")
@Tag(name = "AI Assistant", description = "AI 비서 관련 API")
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
                content = [Content(schema = Schema(implementation = AiAssistantSettingResponse::class))]
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
     * AI 비서 음성 합성 (다중 카테고리 통합)
     */
    @Operation(
        summary = "AI 비서 이용하기 요청 (다중 카테고리)",
        description = "로그인된 사용자의 AI 비서 설정에 따라 선택된 모든 카테고리에 대한 음성 합성을 비동기적으로 요청합니다. 중복 요청은 자동으로 처리됩니다.",
        responses = [
            ApiResponse(
                responseCode = "202",
                description = "요청 수락됨. Location 헤더에 상태 확인 URL 포함",
                content = [Content(schema = Schema(implementation = AiAssistantMultiSynthesisResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "잘못된 요청 또는 설정 (선택된 카테고리 없음 등 - IllegalArgumentException)"),
            ApiResponse(responseCode = "401", description = "인증 실패 (세션 등)"),
            ApiResponse(responseCode = "403", description = "해당 보이스팩 사용 권한 없음 (SecurityException)"),
            ApiResponse(responseCode = "404", description = "사용자, 보이스팩, 또는 AI 비서 설정을 찾을 수 없음 (IllegalArgumentException/NoSuchElementException 등)"),
            ApiResponse(responseCode = "500", description = "서버 내부 오류 (날짜 포맷 오류, MQ/S3 오류 등)")
        ]
    )
    @PostMapping("/synthesis")
    fun submitSynthesisRequest(
        @SessionAttribute("userId") userId: Long
    ): ResponseEntity<AiAssistantMultiSynthesisResponse> {
        try {
            val responseDto = aiAssistantService.submitSynthesisRequest(userId)

            val locationUri = ServletUriComponentsBuilder
                .fromCurrentContextPath() // 현재 요청의 기본 URL (e.g., http://localhost:8080)
                .path("/api/ai-assistant/synthesis//status/{requestId}")
                .buildAndExpand(responseDto.requestId) // 경로 변수({requestId}) 채우기
                .toUri()

            return ResponseEntity.accepted().location(locationUri).body(responseDto)
        } catch (e: SecurityException) {
            logger.warn("AI 비서 음성 합성 권한 오류: User ID {}, Message: {}", userId, e.message)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null)
        } catch (e: IllegalArgumentException) {
            logger.warn("AI 비서 음성 합성 잘못된 요청: User ID {}, Message: {}", userId, e.message)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
        } catch (e: IllegalStateException) {
            logger.error("AI 비서 음성 합성 내부 상태 오류: User ID {}, Message: {}", userId, e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        } catch (e: Exception) {
            logger.error("AI 비서 음성 합성 중 예상치 못한 오류: User ID: {}, Message: {}", userId, e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @GetMapping("/synthesis/status/{requestId}")
    @Operation(
        summary = "AI 비서 음성 합성 상태 확인 (다중 카테고리)",
        description = "지정된 요청 ID의 전체 음성 합성 진행 상태를 확인합니다.",
        parameters = [
            Parameter(name = "requestId", description = "확인할 요청의 ID", required = true)
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "상태 조회 성공. PROCESSING, SUCCESS, FAILURE 중 하나.", content = [Content(schema = Schema(implementation = AiAssistantMultiSynthesisStatusResponse::class))]),
            ApiResponse(responseCode = "404", description = "요청 ID에 해당하는 요청을 찾을 수 없음 (IllegalArgumentException 발생)")
        ]
    )
    fun getSynthesisRequestStatus(
        @PathVariable requestId: Long
    ): ResponseEntity<AiAssistantMultiSynthesisStatusResponse> {
        try {
            val statusDto = aiAssistantService.getSynthesisRequestStatus(requestId)
            return ResponseEntity.ok(statusDto)
        } catch (e: IllegalArgumentException) {
            logger.warn("AI 비서 상태 조회 실패 (찾을 수 없음): Request ID $requestId, Error: ${e.message}")
            return ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("AI 비서 상태 조회 중 오류 발생: Request ID $requestId", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping("/callback")
    @Operation(
        summary = "AI 비서 음성 합성 콜백 (Job 단위)",
        description = "Cloud Run 에서 개별 음성 합성 작업(Job) 완료/실패 시 호출하는 엔드포인트",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Job 처리 결과 정보",
            required = true,
            content = [Content(schema = Schema(implementation = AiAssistantJobCallbackRequest::class))]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "콜백 처리 성공"),
            ApiResponse(responseCode = "404", description = "Job ID에 해당하는 작업을 찾을 수 없음 (IllegalArgumentException 발생)")
        ]
    )
    fun handleSynthesisCallback(
        @RequestBody callbackDto: AiAssistantJobCallbackRequest
    ): ResponseEntity<String> {
        try {
            aiAssistantService.handleSynthesisCallback(callbackDto)
            return ResponseEntity.ok("Callback processed successfully for Job ID: ${callbackDto.jobId}")
        } catch (e: IllegalArgumentException) {
            logger.warn("Callback 처리 실패 (찾을 수 없음): Job ID ${callbackDto.jobId}, Error: ${e.message}")
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
        } catch (e: Exception) {
            logger.error("Callback 처리 중 오류 발생: Job ID ${callbackDto.jobId}", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error")
        }
    }

}