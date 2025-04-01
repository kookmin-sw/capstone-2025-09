package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright

import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRight
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface VoicepackUsageRightRepository : JpaRepository<VoicepackUsageRight, Long> {
    // 특정 사용자가 특정 보이스팩 사용권을 가지고 있는지 확인
    fun existsByUserIdAndVoicepackId(userId: Long, voicepackId: Long): Boolean

    @Query("""
        SELECT new kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRightBriefDto(
            v.voicepack.id,
            v.voicepack.name
        )
        FROM VoicepackUsageRight v
        WHERE v.user.id = :userId
    """)
    fun findVoicepackDtosByUserId(userId: Long): List<VoicepackUsageRightBriefDto>

    // 특정 사용자가 보유한 모든 사용권 정보 조회 (페이지네이션 가능)
    // fun findByUserId(userId: Long, pageable: Pageable): Page<VoicepackUsageRight>
} 