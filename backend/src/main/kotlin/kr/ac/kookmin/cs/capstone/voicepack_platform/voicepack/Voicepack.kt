package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack

import jakarta.persistence.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.User
import java.time.OffsetDateTime

@Entity
@Table(name = "voicepack")
data class Voicepack(
    @Id
    @Column(name = "voicepack_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(name = "name", nullable = false)
    val name: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    val author: User,
    
    @Column(name = "s3_path", nullable = false)
    val s3Path: String,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
) 