package kr.ac.kookmin.cs.capstone.voicepack_platform.sale.dto

import io.swagger.v3.oas.annotations.media.Schema

data class SalesSummaryDto(
    @Schema(description = "총 수익 (크레딧)", example = "320")
    val totalRevenue: Int,
    
    @Schema(description = "이번 달 수익 (크레딧)", example = "200")
    val monthlyRevenue: Int,
    
    @Schema(description = "총 판매 건수", example = "5")
    val salesCount: Long
) 