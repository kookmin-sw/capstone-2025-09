package kr.ac.kookmin.cs.capstone.voicepack_platform.credit.model

import jakarta.persistence.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.User
import java.time.OffsetDateTime

@Entity
@Table(name = "user_credits")
data class Credit(
    @Id
    @Column(name = "credit_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User,
    
    @Column(name = "balance", nullable = false)
    var balance: Int = 0,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
) 