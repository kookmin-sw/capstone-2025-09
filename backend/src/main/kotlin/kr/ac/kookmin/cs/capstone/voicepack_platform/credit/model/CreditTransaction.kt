package kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model

import jakarta.persistence.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.User
import java.time.OffsetDateTime

@Entity
@Table(name = "credit_transactions")
data class CreditTransaction(
    @Id
    @Column(name = "transaction_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(name = "amount", nullable = false)
    val amount: Int,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: TransactionType,
    
    @Column(name = "reference_id", nullable = true)
    val referenceId: Long? = null,
    
    @Column(name = "reference_type", nullable = true)
    @Enumerated(EnumType.STRING)
    val referenceType: ReferenceType? = null,
    
    @Column(name = "description", nullable = true)
    val description: String? = null,
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: TransactionStatus = TransactionStatus.PENDING,
    
    @Column(name = "balance_before", nullable = false)
    val balanceBefore: Int,
    
    @Column(name = "balance_after", nullable = true)
    var balanceAfter: Int? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)

enum class TransactionType {
    CHARGE,      // 크레딧 충전
    PURCHASE,    // 보이스팩 구매 등에 사용
    REFUND,      // 환불
    EXCHANGE,    // 크레딧을 현금으로 환전
    SYSTEM,      // 시스템 조정 (관리자 조정 등)
    SALE_INCOME  // 판매 수익 (충전과 유사)
}

enum class TransactionStatus {
    PENDING,    // 진행 중
    COMPLETED,  // 완료됨
    FAILED      // 실패함
}

enum class ReferenceType {
    PAYMENT,       // 결제 (충전)
    VOICEPACK,     // 보이스팩 구매
    CREDIT_EXCHANGE // 크레딧 환전
    // 필요에 따라 추가 (예: TTS_SYNTHESIS)
} 