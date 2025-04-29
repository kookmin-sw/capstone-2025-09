package kr.ac.kookmin.cs.capstone.voicepack_platform.credit.dto

import io.swagger.v3.oas.annotations.media.Schema

data class CreditExchangeResponseDto(
    @Schema(description = "처리 결과 메시지", example = "환전 신청이 완료되었습니다.")
    val message: String,
    @Schema(description = "환전될 예상 원화 금액", example = "32000")
    val won: Int
) 