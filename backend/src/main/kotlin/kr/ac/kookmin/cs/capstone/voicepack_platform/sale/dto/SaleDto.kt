package kr.ac.kookmin.cs.capstone.voicepack_platform.sale.dto

import io.swagger.v3.oas.annotations.media.Schema
import kr.ac.kookmin.cs.capstone.voicepack_platform.sale.Sale
import java.time.OffsetDateTime

data class SaleDto(
    @Schema(description = "판매 기록 ID", example = "1")
    val id: Long,
    
    @Schema(description = "판매 일시")
    val date: OffsetDateTime,
    
    @Schema(description = "판매된 보이스팩 이름", example = "감성 보이스")
    val voicepackName: String,
    
    @Schema(description = "구매자 이메일", example = "buyer@example.com") // 구매자 ID 대신 이메일 사용
    val buyerEmail: String,
    
    @Schema(description = "판매 금액 (크레딧)", example = "100")
    val amount: Int
) {
    companion object {
        fun fromEntity(sale: Sale): SaleDto {
            return SaleDto(
                id = sale.id,
                date = sale.transactionDate,
                voicepackName = sale.voicepack.name,
                buyerEmail = sale.buyer.email, // 구매자 이메일로 변경
                amount = sale.amount
            )
        }
    }
} 