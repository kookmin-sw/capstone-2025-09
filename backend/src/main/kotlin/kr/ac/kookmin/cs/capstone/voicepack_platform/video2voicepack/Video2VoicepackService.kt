package kr.ac.kookmin.cs.capstone.voicepack_platform.video2voicepack

import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.VoicepackService
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.VoicepackConvertRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.VoicepackConvertResponse
import kr.ac.kookmin.cs.capstone.voicepack_platform.video2voicepack.SimpleMultipartFile
import kr.ac.kookmin.cs.capstone.voicepack_platform.video2voicepack.presignedurl.PresignedUrlService
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.nio.file.Files
import org.slf4j.LoggerFactory

@Service
class Video2VoicepackService(
    private val voicepackService: VoicepackService,
    private val presignedUrlService: PresignedUrlService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    
    suspend fun convertVideo2Voicepack(userId: Long, name: String, putUrl: String): VoicepackConvertResponse {
        // PUT URL로 엔티티 조회 (존재하는지 및 만료되지 않았는지 확인)
        val urlDetail = presignedUrlService.getPresignedUrlDetailByPutUrl(putUrl)
        val getUrl = urlDetail.getUrl
        
        logger.info("조회된 GET URL: {}", getUrl)
        
        // 임시 파일 생성
        val tempFile = File.createTempFile("video_", ".mp4")
        try {
            // URL로부터 파일 다운로드
            URI(getUrl).toURL().openStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            logger.info("파일 다운로드 완료: {}, 크기: {}", tempFile.absolutePath, tempFile.length())

            // MultipartFile로 변환 (SimpleMultipartFile 사용)
            val fileBytes = Files.readAllBytes(tempFile.toPath())
            val multipartFile = SimpleMultipartFile(
                tempFile.name,
                "video/mp4",
                fileBytes
            )

            // 보이스팩 변환 요청
            val convertRequest = VoicepackConvertRequest(
                name = name,
                voiceFile = multipartFile,
                isVideoBased = true,
                tempFilePath = tempFile.absolutePath,
                imageFile = null,
                categories = listOf("영상 기반")
            )
            logger.info("보이스팩 변환 요청: userId={}, name={}, fileSize={}", userId, name, multipartFile.size)

            return voicepackService.convertVoicepack(userId, convertRequest)
        } catch (e: Exception) {
            // 오류 발생 시 VoicepackService가 호출되지 않으므로, 여기서 임시 파일 삭제
            if (tempFile.exists()) {
                try {
                    tempFile.delete()
                    logger.info("오류 발생으로 임시 파일 삭제: {}", tempFile.absolutePath)
                } catch (deleteError: Exception) {
                    logger.warn("임시 파일 삭제 실패: {}", tempFile.absolutePath, deleteError)
                }
            }
            throw e
        }
    }
}