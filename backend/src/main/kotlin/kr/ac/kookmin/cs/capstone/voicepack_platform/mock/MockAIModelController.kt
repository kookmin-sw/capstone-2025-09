package kr.ac.kookmin.cs.capstone.voicepack_platform.mock

import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.AIModelRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.AIModelResponse
import org.springframework.web.bind.annotation.*
import kotlin.random.Random

@RestController
@RequestMapping("/mock/ai-model")
class MockAIModelController {
    
    @PostMapping("/process")
    suspend fun process(@RequestBody request: AIModelRequest): AIModelResponse {
        // 랜덤하게 실패 발생
        if (Random.nextInt(100) < 30) { // 30% 확률로 실패
            throw RuntimeException("AI 모델 처리 실패 (테스트용)")
        }
        
        // 3초 대기 (처리 시간 시뮬레이션)
        Thread.sleep(3000)
        
        return AIModelResponse(
            outputPath = "s3://mock-bucket/voicepacks/${request.voicepackId}/output.zip"
        )
    }
} 