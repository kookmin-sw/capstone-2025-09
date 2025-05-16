package kr.ac.kookmin.cs.capstone.voicepack_platform.sale

import jakarta.persistence.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.User
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.Voicepack
import java.time.OffsetDateTime

@Entity
@Table(name = "sales")
data class Sale(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    val seller: User, // 판매자 (보이스팩 제작자)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    val buyer: User, // 구매자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voicepack_id", nullable = false)
    val voicepack: Voicepack, // 판매된 보이스팩

    @Column(name = "amount", nullable = false)
    val amount: Int, // 판매 금액 (크레딧)

    @Column(name = "transaction_date", nullable = false)
    val transactionDate: OffsetDateTime = OffsetDateTime.now() // 판매 시점
) 