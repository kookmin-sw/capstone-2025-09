package kr.ac.kookmin.cs.capstone.voicepack_platform.quote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.quote.dto.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.VoicepackService
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.VoicepackSynthesisRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto.VoicepackSynthesisSubmitResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class QuoteService(
    private val voicepackService: VoicepackService,
    private val httpClient: HttpClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    // !!! 실제 구현 시에는 환경 변수나 설정 파일에서 안전하게 관리해야 합니다 !!!
    @Value("\${openai.api.key}") // application.properties 등에서 설정
    private lateinit var openAiApiKey: String

    private val openAiApiUrl = "https://api.openai.com/v1/chat/completions"

    suspend fun generateQuoteAndSynthesize(userId: Long, request: TodayQuoteRequest): VoicepackSynthesisSubmitResponse {
        logger.info("오늘의 명언 생성 및 합성 요청 시작: userId={}, request={}", userId, request)

        try {
            // 1. OpenAI 프롬프트 생성
            val prompt = createOpenAiPrompt(request.emotion, request.category)
            logger.debug("생성된 OpenAI 프롬프트: {}", prompt)

            // 2. OpenAI API 호출
            val openAiRequest = OpenAIChatRequest(
                model = "gpt-4.1-mini-2025-04-14", // 또는 다른 원하는 모델
                messages = listOf(
                    ChatMessage("system", "You are a helpful assistant that provides quotes."),
                    ChatMessage("user", prompt)
                )
            )

            val response: OpenAIChatResponse = httpClient.post(openAiApiUrl) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $openAiApiKey")
                setBody(openAiRequest)
            }.body()

            logger.debug("OpenAI API 응답 수신: {}", response)

            // 3. 응답 파싱 (첫 번째 choice의 message content 사용)
            val quoteText = response.choices.firstOrNull()?.message?.content?.trim()
                ?: throw RuntimeException("OpenAI API로부터 유효한 명언을 받지 못했습니다.")

            logger.info("추출된 명언: {}", quoteText)

            // 4. VoicepackService를 통해 음성 합성 요청
            val synthesisRequest = VoicepackSynthesisRequest(
                voicepackId = request.voicepackId,
                prompt = quoteText // OpenAI로부터 받은 명언을 합성할 텍스트로 사용
            )

            logger.info("음성 합성 요청 전달: userId={}, voicepackId={}, prompt='{}'", userId, request.voicepackId, quoteText)
            return voicepackService.submitSynthesisRequest(userId, synthesisRequest)

        } catch (e: Exception) {
            logger.error("오늘의 명언 생성 또는 합성 중 오류 발생: userId={}, request={}", userId, request, e)

            return VoicepackSynthesisSubmitResponse(
                id = -1, // 실패 시 ID는 -1 또는 다른 식별 가능한 값으로 설정
                message = "명언 생성 또는 음성 합성 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }

    private fun createOpenAiPrompt(emotion: String, category: QuoteCategory): String {
        val categoryDescription = when (category) {
            QuoteCategory.KOREAN -> "한국의"
            QuoteCategory.EASTERN -> "동양의"
            QuoteCategory.WESTERN -> "서양의"
        }
        // 명언과 말한 사람만 요청하도록 프롬프트 구체화
        return "현재 감정 상태는 '${emotion}'입니다. 이 감정 상태에 어울리는 ${categoryDescription} 유명한 명언 한 구절과 그 명언을 말한 사람을 알려주세요. 응답 형식은 '명언 구절 - 말한 사람' 형태로 한글로 답해주세요. 특수문자는 제외합니다."
    }
} 