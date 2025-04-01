package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack

import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.*
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

@RestController
@RequestMapping("/api/voicepack")
@Tag(name = "보이스팩", description = "보이스팩 관련 API")
class VoicepackController(
    private val voicepackService: VoicepackService
) {
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
        summary = "보이스팩 기반 TTS 생성",
        description = "사용자가 보이스팩을 기반으로 TTS 생성을 요청합니다.",
        responses = [
            ApiResponse(
                responseCode = "202",
                description = "생성 요청 성공",
                content = [Content(schema = Schema(implementation = VoicepackSynthesisResponse::class))]
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
    @PostMapping("/synthesis")
    suspend fun synthesisVoicepack(
        @Parameter(description = "사용자 ID") @RequestParam userId: Long,
        @RequestBody request: VoicepackSynthesisRequest
    ): ResponseEntity<VoicepackSynthesisResponse> {
        val response = voicepackService.synthesisVoicepack(userId, request)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response)
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

} 