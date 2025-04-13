package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.service

import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.AiAssistantSettingRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.entity.AiAssistantSetting
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums.Categories
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums.WritingStyle
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.repository.AiAssistantSettingRepository
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AiAssistantService(
    private val userRepository: UserRepository,
    private val aiAssistantSettingRepository: AiAssistantSettingRepository
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // AI 비서 설정 저장 및 업데이트

    fun saveOrUpdateAiAssistantSettings(userId: Long, request: AiAssistantSettingRequest): AiAssistantSetting {

        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }

        val writingStyle = WritingStyle.fromIndex(request.writingStyle)
            ?: throw IllegalArgumentException("Invalid writing style provided: ${request.writingStyle}")

        val categories = request.categories.mapNotNull { index ->
            Categories.fromIndex(index) ?: run { // fromIndex(index)가 null일 경우 유효하지 않은 인덱스는 로그 남기고 무시 -> 그냥 에러를 던지는게 좋을까?
                logger.warn("Invalid category index received: $index")
                null
            }
        }.toMutableSet() // 중복 제거 및 변경 가능한 Set으로 변환

        if (categories.isEmpty()) {
            throw IllegalArgumentException("At least one valid category must be selected.")
        }

        // 기존 설정이 있으면 업데이트, 없으면 새로 생성

        val existingSetting = aiAssistantSettingRepository.findByUserId(userId)

        val setting = existingSetting.orElse(
            AiAssistantSetting(user = user, voicepackId = request.voicepackId, writingStyle = writingStyle, categories = categories)
        )

        // 기존 설정이 있는 경우 필드 값 업데이트
        if (existingSetting.isPresent) {
            setting.voicepackId = request.voicepackId
            setting.writingStyle = writingStyle
            setting.categories = categories
        }

        logger.info("Saving/Updating AI Assistant settings for user ID: $userId")

        return aiAssistantSettingRepository.save(setting)
    }
}