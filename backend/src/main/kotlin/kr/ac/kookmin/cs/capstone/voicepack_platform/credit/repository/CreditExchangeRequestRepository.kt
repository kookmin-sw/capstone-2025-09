package kr.ac.kookmin.cs.capstone.voicepack_platform.credit.repository

import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.CreditExchangeRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CreditExchangeRequestRepository : JpaRepository<CreditExchangeRequest, Long> {

    // 사용자 ID로 환전 신청 내역 조회 (페이징 및 최신순 정렬)
    fun findByUserIdOrderByRequestDateDesc(userId: Long, pageable: Pageable): Page<CreditExchangeRequest>
} 