package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright

import jakarta.persistence.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.User
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.Voicepack
import java.time.OffsetDateTime

@Entity
@Table(name = "voicepack_usage_rights", uniqueConstraints = [
    UniqueConstraint(columnNames = ["user_id", "voicepack_id"])
])
data class VoicepackUsageRight(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voicepack_id", nullable = false)
    val voicepack: Voicepack,

    @Column(name = "granted_at", nullable = false)
    val grantedAt: OffsetDateTime = OffsetDateTime.now()
) 