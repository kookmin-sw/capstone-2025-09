package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright

import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.Voicepack
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.usageright.VoicepackUsageRight
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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
    fun findVoicepackDtosByUserId(@Param("userId") userId: Long): List<VoicepackUsageRightBriefDto>

    // 사용자가 구매한 (즉, 자신이 생성하지 않은) 보이스팩 엔티티 목록 조회
    @Query("SELECT DISTINCT vur.voicepack FROM VoicepackUsageRight vur WHERE vur.user.id = :userId AND vur.voicepack.author.id <> :userId")
    fun findDistinctPurchasedVoicepacksByUserId(@Param("userId") userId: Long): List<Voicepack>

    // 사용자가 구매한 (자신이 생성하지 않은) 보이스팩 개수 조회
    @Query("SELECT COUNT(DISTINCT vur.voicepack.id) FROM VoicepackUsageRight vur WHERE vur.user.id = :userId AND vur.voicepack.author.id <> :userId")
    fun countPurchasedVoicepacksByUserId(@Param("userId") userId: Long): Int

    // 특정 사용자가 사용 가능한 모든 보이스팩 엔티티 목록 조회
    @Query("SELECT vur.voicepack FROM VoicepackUsageRight vur WHERE vur.user.id = :userId")
    fun findByUserId(@Param("userId") userId: Long): List<Voicepack>
} 