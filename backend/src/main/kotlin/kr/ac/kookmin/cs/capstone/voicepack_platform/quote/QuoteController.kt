package kr.ac.kookmin.cs.capstone.voicepack_platform.quote

import kr.ac.kookmin.cs.capstone.voicepack_platform.quote.dto.TodayQuoteRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto.VoicepackSynthesisSubmitResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/api/quote")
class QuoteController(
    private val quoteService: QuoteService
) {

    @PostMapping("")
    suspend fun generateTodayQuote(
        @Parameter(description = "사용자 ID") @RequestParam userId: Long,
        @RequestBody request: TodayQuoteRequest
    ): ResponseEntity<VoicepackSynthesisSubmitResponse> {
        // 서비스 호출 전에 현재 컨텍스트 경로 미리 추출
        val baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString()
        
        val response = quoteService.generateQuoteAndSynthesize(userId, request)
        // 서비스에서 오류 발생 시 id가 -1로 반환될 수 있으므로, 이에 따라 상태 코드 분기 가능
        return if (response.id != -1L) {
            // 미리 추출한 baseUrl 사용하여 URI 생성
            val locationUri = ServletUriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/api/voicepack/synthesis/status/{id}")
                .buildAndExpand(response.id)
                .toUri()
                
            return ResponseEntity.accepted().location(locationUri).body(response)
        } else {
            // 실패 응답 처리 (예: 500 Internal Server Error 또는 400 Bad Request 등)
            ResponseEntity.internalServerError().body(response)
        }
    }
} 