package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack

import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/voicepack")
class VoicepackController(
    private val voicepackService: VoicepackService
) {
    @PostMapping("/convert")
    suspend fun convertVoicepack(
        @RequestParam userId: Long,
        @RequestParam name: String,
        @RequestPart voiceFile: MultipartFile
    ): ResponseEntity<VoicepackConvertResponse> {
        val request = VoicepackConvertRequest(name, voiceFile)
        val response = voicepackService.convertVoicepack(userId, request)
        return ResponseEntity.ok(response)
    }

} 