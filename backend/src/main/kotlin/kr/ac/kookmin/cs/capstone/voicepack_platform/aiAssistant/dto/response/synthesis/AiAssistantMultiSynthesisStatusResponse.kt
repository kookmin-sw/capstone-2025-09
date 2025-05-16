package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.response.synthesis

import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.entity.SynthesisStatus

/**
 * AI 비서 다중 카테고리 음성 합성 상태 확인 응답 DTO.
 */
data class AiAssistantMultiSynthesisStatusResponse(
    // 요청의 전체 진행 상태 (PENDING, PROCESSING, SUCCESS, FAILURE)
    val status: SynthesisStatus,
    // 상태가 SUCCESS일 경우, 각 카테고리별 결과 S3 키 맵 ("카테고리 설명": "결과 S3 Key")
    val results: Map<String, String>?
) 