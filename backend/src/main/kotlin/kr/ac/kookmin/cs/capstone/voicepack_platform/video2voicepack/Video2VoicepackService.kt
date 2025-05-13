package kr.ac.kookmin.cs.capstone.voicepack_platform.video2voicepack

import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.VoicepackConvertRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.VoicepackConvertResponse
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.VoicepackService
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import kr.ac.kookmin.cs.capstone.voicepack_platform.video2voicepack.SimpleMultipartFile
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.nio.file.Files

@Service
class Video2VoicepackService(
    private val voicepackService: VoicepackService
) {
    suspend fun convertVideo2Voicepack(userId: Long, name: String, presignedUrl: String): VoicepackConvertResponse {
        // 1. presignedUrl로 파일 다운로드
        val tempFile = File.createTempFile("video2voicepack_", ".wav")
        try {
            URI(presignedUrl).toURL().openStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            // 2. MultipartFile로 변환
            val fileBytes = Files.readAllBytes(tempFile.toPath())
            val multipartFile: MultipartFile = SimpleMultipartFile(
                name,
                "audio/wav",
                fileBytes
            )
            // 3. 기존 보이스팩 생성 로직 호출
            val request = VoicepackConvertRequest(name, multipartFile, isVideoBased = true, tempFilePath = tempFile.absolutePath)
            val response = voicepackService.convertVoicepack(userId, request)
            // 4. 생성된 보이스팩의 isVideoBased=true, isPublic=false로 강제 (DB 반영)
            // (VoicepackService에서 생성 후 후처리 필요)
            // 별도 후처리 메소드 호출 또는 콜백에서 처리 필요
            // 5. 반환
            return response
        } finally {
        }
    }
}