package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.response.synthesis

/**
 * AI 비서 다중 카테고리 음성 합성 요청 시 응답 DTO.
 * 생성된 요청의 ID를 반환합니다.
 */
data class AiAssistantMultiSynthesisResponse(
    val requestId: Long // 생성된 AiAssistantSynthesisRequest의 ID
) 