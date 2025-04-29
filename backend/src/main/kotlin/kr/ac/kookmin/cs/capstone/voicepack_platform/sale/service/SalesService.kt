package kr.ac.kookmin.cs.capstone.voicepack_platform.sale.service

import kr.ac.kookmin.cs.capstone.voicepack_platform.sale.SaleRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.sale.dto.SaleDto
import kr.ac.kookmin.cs.capstone.voicepack_platform.sale.dto.SalesSummaryDto
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.temporal.TemporalAdjusters

@Service
class SalesService(
    private val saleRepository: SaleRepository
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional(readOnly = true)
    fun getSalesHistory(sellerId: Long, pageable: Pageable): Page<SaleDto> {
        logger.info("판매 내역 조회: sellerId={}, page={}, size={}", sellerId, pageable.pageNumber, pageable.pageSize)
        val salesPage = saleRepository.findBySellerIdOrderByTransactionDateDesc(sellerId, pageable)
        return salesPage.map { SaleDto.fromEntity(it) }
    }

    @Transactional(readOnly = true)
    fun getSalesSummary(sellerId: Long): SalesSummaryDto {
        logger.info("판매 통계 조회: sellerId={}", sellerId)

        val totalRevenue = saleRepository.sumAmountBySellerId(sellerId) ?: 0

        // 이번 달 수익 계산
        val now = OffsetDateTime.now()
        val firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0)
        val lastDayOfMonth = now.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999)
        val monthlyRevenue = saleRepository.sumAmountBySellerIdAndTransactionDateBetween(sellerId, firstDayOfMonth, lastDayOfMonth) ?: 0

        val salesCount = saleRepository.countBySellerId(sellerId)

        logger.info("판매 통계 조회 완료: sellerId={}, total={}, monthly={}, count={}", sellerId, totalRevenue, monthlyRevenue, salesCount)
        return SalesSummaryDto(
            totalRevenue = totalRevenue,
            monthlyRevenue = monthlyRevenue,
            salesCount = salesCount
        )
    }
} 