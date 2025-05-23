package kr.ac.kookmin.cs.capstone.voicepack_platform.video2voicepack

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.VoicepackConvertResponse
import kr.ac.kookmin.cs.capstone.voicepack_platform.video2voicepack.presignedurl.PresignedUrlService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/video2voicepack")
@Tag(name = "영상기반 보이스팩", description = "영상 기반 보이스팩 생성 API")
class Video2VoicepackController(
    private val video2VoicepackService: Video2VoicepackService,
    private val presignedUrlService: PresignedUrlService
) {
    @Operation(
        summary = "Presigned URL 발급",
        description = "FE에서 영상 업로드를 위한 PUT URL을 발급합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Presigned URL 발급 성공",
                content = [Content(schema = Schema(implementation = String::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )
    @GetMapping("/presigned-url")
    fun getPresignedUrl(): ResponseEntity<String> {
        val putUrl = presignedUrlService.generateAndSavePresignedUrls()
        return ResponseEntity.ok(putUrl)
    }

    @Operation(
        summary = "영상 기반 보이스팩 생성",
        description = "업로드에 사용한 PUT URL을 그대로 사용해 보이스팩을 생성합니다.",
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
    suspend fun convertVideo2Voicepack(
        @Parameter(description = "사용자 ID") @RequestParam userId: Long,
        @Parameter(description = "보이스팩 이름") @RequestParam name: String,
        @Parameter(description = "업로드에 사용한 PUT Presigned URL") @RequestParam putUrl: String
    ): ResponseEntity<VoicepackConvertResponse> {
        val response = video2VoicepackService.convertVideo2Voicepack(userId, name, putUrl)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response)
    }
}