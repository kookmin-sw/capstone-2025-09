package kr.ac.kookmin.cs.capstone.voicepack_platform.sale

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface SaleRepository : JpaRepository<Sale, Long> {

    // 판매자 ID로 판매 내역 조회 (페이징 및 최신순 정렬)
    fun findBySellerIdOrderByTransactionDateDesc(sellerId: Long, pageable: Pageable): Page<Sale>

    // 판매자 ID로 총 판매 금액 합계 조회
    @Query("SELECT SUM(s.amount) FROM Sale s WHERE s.seller.id = :sellerId")
    fun sumAmountBySellerId(sellerId: Long): Int? // 결과가 없을 수 있으므로 Nullable

    // 판매자 ID와 특정 기간으로 판매 금액 합계 조회
    @Query("SELECT SUM(s.amount) FROM Sale s WHERE s.seller.id = :sellerId AND s.transactionDate BETWEEN :startDate AND :endDate")
    fun sumAmountBySellerIdAndTransactionDateBetween(sellerId: Long, startDate: OffsetDateTime, endDate: OffsetDateTime): Int?

    // 판매자 ID로 총 판매 건수 조회
    fun countBySellerId(sellerId: Long): Long
} 