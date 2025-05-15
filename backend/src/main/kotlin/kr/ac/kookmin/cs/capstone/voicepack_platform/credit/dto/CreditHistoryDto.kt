package kr.ac.kookmin.cs.capstone.voicepack_platform.credit.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

// 충전 내역 DTO
data class ChargeDto(
    @Schema(description = "날짜")
    val date: OffsetDateTime,
    @Schema(description = "원화 금액", example = "5000")
    val amountWon: Int?, // 원화 금액은 Transaction description 파싱 필요? 또는 별도 저장?
    @Schema(description = "충전된 크레딧", example = "50")
    val amountCredit: Int,
    @Schema(description = "결제 수단", example = "카카오페이")
    val method: String?
)

// 사용 내역 DTO
data class UsageDto(
    @Schema(description = "날짜")
    val date: OffsetDateTime,
    @Schema(description = "사용처", example = "감성 보이스 구매")
    val usage: String?,
    @Schema(description = "사용한 크레딧", example = "100")
    val amountCredit: Int
)

// 크레딧 내역 통합 DTO
data class CreditHistoryDto(
    @Schema(description = "충전 내역 리스트")
    val charges: List<ChargeDto>,
    @Schema(description = "사용 내역 리스트")
    val usages: List<UsageDto>
) 