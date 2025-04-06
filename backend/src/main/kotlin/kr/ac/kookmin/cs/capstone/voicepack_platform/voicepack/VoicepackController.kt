package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack

import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto.VoicepackSynthesisSubmitResponse
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto.VoicepackCallbackRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRightDto
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRightBriefDto
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto.VoicepackSynthesisStatusDto
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI

@RestController
@RequestMapping("/api/voicepack")
@Tag(name = "보이스팩", description = "보이스팩 관련 API")
class VoicepackController(
    private val voicepackService: VoicepackService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Operation(
        summary = "보이스팩 변환",
        description = "사용자가 업로드한 음성 파일을 보이스팩으로 변환합니다.",
        responses = [
            ApiResponse(
                responseCode = "202",
                description = "변환 요청 성공",
                content = [Content(schema = Schema(implementation = VoicepackConvertResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청"
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )
    @PostMapping("/convert")
    suspend fun convertVoicepack(
        @Parameter(description = "사용자 ID") @RequestParam userId: Long,
        @Parameter(description = "보이스팩 이름") @RequestParam name: String,
        @Parameter(description = "음성 파일") @RequestPart voiceFile: MultipartFile
    ): ResponseEntity<VoicepackConvertResponse> {
        val request = VoicepackConvertRequest(name, voiceFile)
        val response = voicepackService.convertVoicepack(userId, request)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response)
    }

    @Operation(
        summary = "보이스팩 기반 TTS 생성 요청 (비동기)",
        description = "사용자가 보이스팩을 기반으로 TTS 생성을 비동기적으로 요청합니다.",
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
        @Parameter(description = "사용자 ID") @RequestParam userId: Long,
        @RequestBody request: VoicepackSynthesisRequest
    ): ResponseEntity<Any> {
        try {
            val response = voicepackService.submitSynthesisRequest(userId, request)
            
            // Location 헤더 생성 (상태 조회 엔드포인트 URL)
            val locationUri = ServletUriComponentsBuilder
                .fromCurrentContextPath() // 현재 요청의 기본 URL (e.g., http://localhost:8080)
                .path("/api/voicepack/synthesis/status/{jobId}") // 상태 조회 경로 추가
                .buildAndExpand(response.jobId) // 경로 변수({jobId}) 채우기
                .toUri()
                
            return ResponseEntity.accepted().location(locationUri).body(response)
        } catch (e: SecurityException) {
            logger.error("음성 합성 권한 오류: {}", e.message)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            logger.error("음성 합성 잘못된 요청: {}", e.message)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: RuntimeException) {
            logger.error("음성 합성 요청 제출 오류: {}", e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("음성 합성 중 예상치 못한 오류: {}", e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "음성 합성 요청 중 오류가 발생했습니다."))
        }
    }

    @Operation(
        summary = "보이스팩 목록 조회",
        description = "시스템에 등록된 모든 보이스팩을 조회합니다. 선택적으로 특정 사용자의 보이스팩만 조회할 수 있습니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = List::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )
    @GetMapping("")
    fun getVoicepacks(
        @Parameter(description = "사용자 ID (선택적)") @RequestParam(required = false) userId: Long?
    ): ResponseEntity<List<VoicepackDto>> {
        val voicepacks = voicepackService.getVoicepacks(userId)
        return ResponseEntity.ok(voicepacks)
    }
  
    // 보이스팩 1개만 조회
    @Operation(
        summary = "보이스팩 1개 조회",
        description = "보이스팩 1개를 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",   
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = VoicepackDto::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "보이스팩 존재하지 않음"
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )
    @GetMapping("/{voicepackId}")
    fun getVoicepack(
        @Parameter(description = "보이스팩 ID") @PathVariable voicepackId: Long
    ): ResponseEntity<VoicepackDto> {
        try {
            val response = voicepackService.getVoicepack(voicepackId)
            return ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
    }
    
    // 보이스팩 예시 음성 파일 조회
    @Operation(
        summary = "보이스팩 예시 음성 파일 조회",
        description = "보이스팩 예시 음성 파일을 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = String::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "보이스팩 존재하지 않음"
            ),
            ApiResponse(
                responseCode = "500",   
                description = "서버 오류"
            )
        ]
    )
    @GetMapping("/example/{voicepackId}")
    fun getExampleVoicepack(
        @Parameter(description = "보이스팩 ID") @PathVariable voicepackId: Long
    ): ResponseEntity<String> {
        try {
            val response = voicepackService.getExampleVoice(voicepackId)
            return ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
    }

    @Operation(
        summary = "보이스팩 사용권 획득",
        description = "사용자가 특정 보이스팩의 사용권을 획득합니다 (구매 또는 제작자 자동 획득).",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "사용권 획득 성공",
                content = [Content(schema = Schema(implementation = VoicepackUsageRightDto::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (예: 사용자 또는 보이스팩 없음)"
            ),
            ApiResponse(
                responseCode = "409",
                description = "이미 사용권을 가지고 있는 보이스팩"
            ),
            ApiResponse(
                responseCode = "402",
                description = "크레딧 부족 (크레딧 연동 시)"
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )
    @PostMapping("/usage-right")
    fun grantUsageRight(
        @Parameter(description = "사용자 ID") @RequestParam userId: Long,
        @Parameter(description = "보이스팩 ID") @RequestParam voicepackId: Long
    ): ResponseEntity<Any> {
        try {
            val usageRightDto = voicepackService.grantUsageRight(userId, voicepackId)
            return ResponseEntity.ok(usageRightDto)
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: IllegalStateException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to e.message))
        } catch (e: RuntimeException) {
            if (e.message?.contains("크레딧") == true) {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(mapOf("error" to e.message))
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to (e.message ?: "사용권 획득 처리 중 오류가 발생했습니다.")))
            }
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "사용권 획득 처리 중 예상치 못한 오류가 발생했습니다."))
        }
    }


    // 사용자가 사용권을 보유한 보이스팩 목록 조회
    @Operation(
        summary = "사용자가 사용권을 보유한 보이스팩 목록 조회",
        description = "사용자가 사용권을 보유한 보이스팩 목록을 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = List::class))]
            ),
            ApiResponse(
                responseCode = "500",   
                description = "서버 오류"
            )
        ]
    )
    @GetMapping("/usage-right")
    fun getUserVoicepacks(
        @Parameter(description = "사용자 ID") @RequestParam userId: Long
    ): ResponseEntity<List<VoicepackUsageRightBriefDto>> {
        val voicepacks = voicepackService.getVoicepacksByUserId(userId)
        return ResponseEntity.ok(voicepacks)
    }

    // 음성 합성 시 모델로부터 부를 callback url
    @Operation(
        summary = "음성 합성 콜백",
        description = "음성 합성 처리 완료 후 Cloud Run에서 호출하는 내부 콜백 엔드포인트입니다.",
        deprecated = true
    )
    @PostMapping("/synthesis/callback")
    fun handleSynthesisCallback(
        @Parameter(description = "요청 Job ID") @RequestParam jobId: String,
        @Parameter(description = "처리 성공 여부") @RequestParam success: Boolean,
        @Parameter(description = "성공 시 결과 URL") @RequestParam(required = false) resultUrl: String?,
        @Parameter(description = "실패 시 오류 메시지") @RequestParam(required = false) errorMessage: String?
    ): ResponseEntity<Any> {
        // RequestParam으로 받은 값들을 사용하여 DTO 객체 생성
        val callbackRequest = VoicepackCallbackRequest(
            jobId = jobId,
            success = success,
            resultUrl = resultUrl,
            errorMessage = errorMessage
        )
        
        try {
            voicepackService.handleSynthesisCallback(callbackRequest)
            return ResponseEntity.ok().build() // 성공 시 200 OK만 반환
        } catch (e: Exception) {
            // 콜백 처리 중 오류 발생 시 로깅만 하고 500 에러 반환 (Cloud Run 재시도 방지 목적)
            logger.error("음성 합성 콜백 처리 중 오류 발생: jobId={}, error={}", callbackRequest.jobId, e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "콜백 처리 중 오류 발생"))
        }
    }

    @Operation(
        summary = "음성 합성 상태 조회 (Polling)",
        description = "제출된 음성 합성 요청의 현재 상태와 결과(완료 시)를 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = VoicepackSynthesisStatusDto::class))]
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
    @GetMapping("/synthesis/status/{jobId}")
    fun getSynthesisStatus(
        @Parameter(description = "조회할 요청의 Job ID") @PathVariable jobId: String
    ): ResponseEntity<Any> {
        try {
            val statusDto = voicepackService.getSynthesisStatus(jobId)
            return ResponseEntity.ok(statusDto)
        } catch (e: IllegalArgumentException) {
            logger.warn("음성 합성 상태 조회 실패: {}", e.message)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("음성 합성 상태 조회 중 오류 발생: jobId={}, error={}", jobId, e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "상태 조회 중 오류 발생"))
        }
    }

    // 디버그용 엔드포인트
    @Operation(
        summary = "디버그용 엔드포인트",
        description = "디버그용 엔드포인트입니다.",
        deprecated = true
    )
    @PostMapping("/debug/create-voicepack")
    fun createVoicepackForDebug(
        @Parameter(description = "사용자 ID") @RequestParam userId: Long,
        @Parameter(description = "보이스팩 ID") @RequestParam voicepackId: Long
    ): ResponseEntity<Any> {
        voicepackService.createVoicepackForDebug(userId, voicepackId)
        return ResponseEntity.ok().build()
    }
    
} 