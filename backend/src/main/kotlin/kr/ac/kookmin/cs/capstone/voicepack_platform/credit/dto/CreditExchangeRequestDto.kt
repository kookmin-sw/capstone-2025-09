package kr.ac.kookmin.cs.capstone.voicepack_platform.credit.dto

import io.swagger.v3.oas.annotations.media.Schema
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.CreditExchangeRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.ExchangeStatus
import java.time.OffsetDateTime

data class CreditExchangeRequestDto(
    @Schema(description = "환전 신청 ID", example = "1")
    val id: Long,
    @Schema(description = "신청 날짜")
    val date: OffsetDateTime,
    @Schema(description = "신청 금액 (원화)", example = "50000")
    val amountWon: Int,
    @Schema(description = "처리 상태", example = "PENDING")
    val status: ExchangeStatus
) {
    companion object {
        fun fromEntity(entity: CreditExchangeRequest): CreditExchangeRequestDto {
            return CreditExchangeRequestDto(
                id = entity.id,
                date = entity.requestDate,
                amountWon = entity.wonAmount,
                status = entity.status
            )
        }
    }
} 