package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack

import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.*
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
                responseCode = "200",
                description = "변환 성공",
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
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @Operation(
        summary = "보이스팩 기반 TTS 생성",
        description = "사용자가 보이스팩을 기반으로 TTS 생성을 요청합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "생성 성공",
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
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
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
        return ResponseEntity.ok(voicepacks.map { VoicepackDto.fromEntity(it) })
    }
  
} 