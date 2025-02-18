package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack

import jakarta.persistence.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.User

@Entity
@Table(name = "voicepacks")
data class Voicepack(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false, unique = true)
    val packName: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    val author: User,
    
    @Column(nullable = false)
    var s3Path: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: VoicepackStatus = VoicepackStatus.PROCESSING
)

enum class VoicepackStatus {
    PROCESSING,
    COMPLETED,
    FAILED
} 