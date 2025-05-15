package kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model

import jakarta.persistence.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.User
import java.time.OffsetDateTime

@Entity
@Table(name = "credit_exchange_requests")
data class CreditExchangeRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "credit_amount", nullable = false)
    val creditAmount: Int, // 신청한 크레딧 양

    @Column(name = "won_amount", nullable = false)
    val wonAmount: Int, // 환전될 원화 금액 (신청 시점 기준)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ExchangeStatus = ExchangeStatus.PENDING, // 환전 상태

    // TODO: 환전 신청 시 필요한 계좌 정보 등 추가 필드 고려
    // 예: bankName, accountNumber, accountHolder
    @Column(name = "bank_name", nullable = false)
    val bankName: String,
    
    @Column(name = "account_number", nullable = false)
    val accountNumber: String,
    
    @Column(name = "account_holder", nullable = false)
    val accountHolder: String,

    @Column(name = "request_date", nullable = false)
    val requestDate: OffsetDateTime = OffsetDateTime.now(), // 신청 시점

    @Column(name = "completion_date")
    var completionDate: OffsetDateTime? = null // 처리 완료 시점
)

enum class ExchangeStatus {
    PENDING,    // 처리 대기 중
    PROCESSING, // 처리 중
    COMPLETED,  // 처리 완료 (입금 완료)
    REJECTED    // 처리 거절
} 