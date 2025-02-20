package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack

import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/voicepack")
class VoicepackController(
    private val voicepackService: VoicepackService
) {
    @PostMapping("/convert")
    suspend fun convertVoicepack(
        @RequestParam userId: Long,
        @RequestBody request: VoicepackConvertRequest
    ): VoicepackConvertResponse {
        return voicepackService.convertVoicepack(userId, request)
    }
} 