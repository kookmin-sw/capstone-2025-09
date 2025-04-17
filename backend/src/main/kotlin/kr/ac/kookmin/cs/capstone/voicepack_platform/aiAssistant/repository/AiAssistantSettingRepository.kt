package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.repository

import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.entity.AiAssistantSetting
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface AiAssistantSettingRepository : JpaRepository<AiAssistantSetting, Long> {
    fun findByUserId(userId: Long): Optional<AiAssistantSetting>
}