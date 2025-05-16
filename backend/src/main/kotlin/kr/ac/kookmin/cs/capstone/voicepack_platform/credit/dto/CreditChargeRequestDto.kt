package kr.ac.kookmin.cs.capstone.voicepack_platform.credit.dto

import io.swagger.v3.oas.annotations.media.Schema

data class CreditChargeRequestDto(
    @Schema(description = "충전할 원화 금액", example = "10000")
    val amount: Int, // 원화 기준
    @Schema(description = "결제 수단", example = "카카오페이")
    val method: String
) 