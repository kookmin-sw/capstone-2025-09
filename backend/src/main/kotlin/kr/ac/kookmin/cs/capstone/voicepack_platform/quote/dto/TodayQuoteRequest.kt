package kr.ac.kookmin.cs.capstone.voicepack_platform.quote.dto

data class TodayQuoteRequest(
    val emotion: String, // 현재 감정 상태
    val category: QuoteCategory, // 원하는 명언 분야
    val voicepackId: Long // 음성 합성에 사용할 보이스팩 ID
) 