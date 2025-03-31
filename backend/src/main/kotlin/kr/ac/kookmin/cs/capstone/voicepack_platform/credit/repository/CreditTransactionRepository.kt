package kr.ac.kookmin.cs.capstone.voicepack_platform.credit.repository

import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.CreditTransaction
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.ReferenceType
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.TransactionStatus
import kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model.TransactionType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface CreditTransactionRepository : JpaRepository<CreditTransaction, Long> {
    fun findByUserId(userId: Long, pageable: Pageable): Page<CreditTransaction>
    
    fun findByUserIdAndCreatedAtBetween(
        userId: Long, 
        startDate: OffsetDateTime, 
        endDate: OffsetDateTime,
        pageable: Pageable
    ): Page<CreditTransaction>
    
    fun findByUserIdAndType(
        userId: Long, 
        type: TransactionType,
        pageable: Pageable
    ): Page<CreditTransaction>
    
    fun findByUserIdAndStatus(
        userId: Long, 
        status: TransactionStatus,
        pageable: Pageable
    ): Page<CreditTransaction>
    
    fun findByReferenceIdAndReferenceType(
        referenceId: Long,
        referenceType: ReferenceType,
        pageable: Pageable
    ): Page<CreditTransaction>
} 