package kr.ac.kookmin.cs.capstone.voicepack_platform.quote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kr.ac.kookmin.cs.capstone.voicepack_platform.quote.dto.ChatMessage
import kr.ac.kookmin.cs.capstone.voicepack_platform.quote.dto.FunctionParameters
import kr.ac.kookmin.cs.capstone.voicepack_platform.quote.dto.FunctionTool
import kr.ac.kookmin.cs.capstone.voicepack_platform.quote.dto.OpenAIChatRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.quote.dto.OpenAIChatResponse
import kr.ac.kookmin.cs.capstone.voicepack_platform.quote.dto.PropertyDetail
import kr.ac.kookmin.cs.capstone.voicepack_platform.quote.dto.QuoteCategory
import kr.ac.kookmin.cs.capstone.voicepack_platform.quote.dto.GoogleSearchResponse
import kr.ac.kookmin.cs.capstone.voicepack_platform.quote.dto.TodayQuoteRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.quote.dto.Tool
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.VoicepackService
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.dto.VoicepackSynthesisRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto.VoicepackSynthesisSubmitResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class QuoteService(
    private val voicepackService: VoicepackService,
    private val httpClient: HttpClient,
    private val json: Json
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Value("\${openai.api.key}")
    private lateinit var openAiApiKey: String

    @Value("\${google.api.key}")
    private lateinit var googleApiKey: String

    @Value("\${google.cx.id}")
    private lateinit var googleCxId: String

    private val openAiApiUrl = "https://api.openai.com/v1/chat/completions"
    private val googleSearchApiUrl = "https://www.googleapis.com/customsearch/v1"

    private val webSearchTool = Tool(
        type = "function",
        function = FunctionTool(
            name = "web_search_for_quote",
            description = "웹에서 감정과 카테고리에 따른 유명한 명언을 검색하고, 그 명언의 진실성과 말한 사람을 확인합니다.",
            parameters = FunctionParameters(
                type = "object",
                properties = mapOf(
                    "emotion" to PropertyDetail(type = "string", description = "현재 감정 상태, 예: 행복, 슬픔"),
                    "category" to PropertyDetail(type = "string", description = "명언의 지역 분류, 예: 한국, 동양, 서양")
                ),
                required = listOf("emotion", "category")
            )
        )
    )

    suspend fun generateQuoteAndSynthesize(userId: Long, request: TodayQuoteRequest): VoicepackSynthesisSubmitResponse {
        logger.info("오늘의 명언 생성 및 합성 요청 시작: userId={}, request={}", userId, request)

        try {
            val initialPrompt = createOpenAiPrompt(request.emotion, request.category)
            logger.debug("생성된 초기 OpenAI 프롬프트: {}", initialPrompt)

            val initialMessages = mutableListOf(
                ChatMessage("system", "You are a helpful assistant that provides quotes. If a web search is needed to find and verify a quote, you will call the 'web_search_for_quote' function. Provide the quote and its speaker in the format 'Quote - Speaker' in Korean."),
                ChatMessage("user", initialPrompt)
            )

            var openAiRequest = OpenAIChatRequest(
                model = "gpt-4o-2024-08-06",
                messages = initialMessages,
                tools = listOf(webSearchTool),
                tool_choice = "auto"
            )

            var apiResponse: OpenAIChatResponse = httpClient.post(openAiApiUrl) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $openAiApiKey")
                setBody(openAiRequest)
            }.body()

            logger.debug("첫 번째 OpenAI API 응답 수신: {}", apiResponse)

            val firstChoice = apiResponse.choices.firstOrNull()
                ?: throw RuntimeException("OpenAI API로부터 유효한 응답을 받지 못했습니다. (첫 번째 호출)")

            var quoteText: String?

            if (firstChoice.finish_reason == "tool_calls" && firstChoice.message.tool_calls != null) {
                logger.info("OpenAI가 웹 검색 함수 호출을 요청했습니다.")
                val toolCall = firstChoice.message.tool_calls.firstOrNull { it.function.name == "web_search_for_quote" }

                if (toolCall != null) {
                    val searchArguments = json.decodeFromString<Map<String, String>>(toolCall.function.arguments)
                    val emotionArg = searchArguments["emotion"] ?: ""
                    val categoryArg = searchArguments["category"] ?: ""
                    
                    val searchQuery = "\'$categoryArg\' \'$emotionArg\' 관련된 유명한 명언과 말한 사람"
                    logger.info("Google 검색 API 호출. 검색어: {}", searchQuery)
                    val webSearchResultContent = performWebSearch(searchQuery)
                    logger.info("웹 검색 결과: {}", webSearchResultContent)

                    initialMessages.add(ChatMessage(role="assistant", content=null, tool_calls=firstChoice.message.tool_calls))
                    initialMessages.add(
                        ChatMessage(
                            role = "tool",
                            tool_call_id = toolCall.id,
                            content = webSearchResultContent
                        )
                    )

                    openAiRequest = OpenAIChatRequest(
                        model = "gpt-4o-2024-08-06",
                        messages = initialMessages
                    )
                    logger.info("웹 검색 결과를 포함하여 두 번째 OpenAI API 호출")
                    apiResponse = httpClient.post(openAiApiUrl) {
                        contentType(ContentType.Application.Json)
                        header("Authorization", "Bearer $openAiApiKey")
                        setBody(openAiRequest)
                    }.body()
                    logger.debug("두 번째 OpenAI API 응답 수신: {}", apiResponse)

                    quoteText = apiResponse.choices.firstOrNull()?.message?.content?.trim()
                } else {
                    logger.warn("web_search_for_quote 함수 호출이 요청되었으나, 해당 tool_call을 찾을 수 없습니다.")
                    quoteText = firstChoice.message.content?.trim()
                }
            } else {
                logger.info("OpenAI가 웹 검색 없이 바로 응답했습니다.")
                quoteText = firstChoice.message.content?.trim()
            }

            if (quoteText.isNullOrBlank()) {
                throw RuntimeException("OpenAI API로부터 유효한 명언을 받지 못했습니다. (최종)")
            }

            logger.info("추출된 명언: {}", quoteText)

            val synthesisRequest = VoicepackSynthesisRequest(
                voicepackId = request.voicepackId,
                prompt = quoteText
            )

            logger.info("음성 합성 요청 전달: userId={}, voicepackId={}, prompt='{}'", userId, request.voicepackId, quoteText)
            return voicepackService.submitSynthesisRequest(userId, synthesisRequest)

        } catch (e: Exception) {
            logger.error("오늘의 명언 생성 또는 합성 중 오류 발생: userId={}, request={}", userId, request, e)
            return VoicepackSynthesisSubmitResponse(
                id = -1,
                message = "명언 생성 또는 음성 합성 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }

    private fun createOpenAiPrompt(emotion: String, category: QuoteCategory): String {
        val categoryDescription = when (category) {
            QuoteCategory.KOREAN -> "한국"
            QuoteCategory.EASTERN -> "동양"
            QuoteCategory.WESTERN -> "서양"
        }
        return "현재 감정 상태는 '${emotion}'입니다. 이 감정에 어울리는 '${categoryDescription}'의 유명한 명언 한 구절과 그 명언을 말한 사람을 웹 검색을 통해 찾아보고, 검증된 정보를 바탕으로 알려주세요. 응답 형식은 '{말한 사람}은 이렇게 말했습니다. {명언 내용}' 형태로 한글로 답해주세요. 특수문자는 제외합니다. 생성 결과 끝에 말한 사람을 다시 붙이지 마세요. 적절한 결과가 없다면 일반적인 명언을 생성하고, '누군가 이렇게 말했습니다. {명언 내용}' 형식으로 출력합니다." 
    }

    private suspend fun performWebSearch(query: String): String {
        logger.info("Google Custom Search API 호출 시작. 검색어: {}", query)
        try {
            val response: GoogleSearchResponse = httpClient.get(googleSearchApiUrl) {
                parameter("key", googleApiKey)
                parameter("cx", googleCxId)
                parameter("q", query)
                parameter("num", 3)
            }.body()

            if (response.items.isNullOrEmpty()) {
                logger.warn("Google 검색 결과가 없습니다. 검색어: {}", query)
                return json.encodeToString(mapOf("error" to "No search results found."))
            }

            val searchResultsSummary = response.items.mapNotNull { item ->
                mapOf(
                    "title" to item.title,
                    "snippet" to item.snippet,
                    "link" to item.link
                )
            }
            
            if (searchResultsSummary.isEmpty()) {
                logger.warn("유의미한 Google 검색 결과를 찾지 못했습니다. 검색어: {}", query)
                return json.encodeToString(mapOf("message" to "Could not extract meaningful information from search results."))
            }
            
            logger.info("Google 검색 성공. 가공된 결과: {}", searchResultsSummary)
            return json.encodeToString(mapOf("searchResults" to searchResultsSummary))

        } catch (e: Exception) {
            logger.error("Google Custom Search API 호출 중 오류 발생. 검색어: {}, 오류: {}", query, e.message, e)
            return json.encodeToString(mapOf("error" to "Failed to perform web search: ${e.message}"))
        }
    }
} 