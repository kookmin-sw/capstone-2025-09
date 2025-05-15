package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.entity

import jakarta.persistence.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums.WritingStyle
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums.Categories
import kr.ac.kookmin.cs.capstone.voicepack_platform.user.User // 프로젝트의 User 엔티티 임포트

@Entity
@Table(name = "ai_assistant_settings")
data class AiAssistantSetting(
    @Id //
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    // User 엔티티와 1:1 관계 설정 (한 사용자는 하나의 AI 비서 설정을 가짐)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)
    val user: User,

    @Column(nullable = false)
    var voicepackId: Long, // 매핑을 해서 무결성 or id만 사용하니까 복잡성을 줄이기? -> 무결성 체크에 대해 고민해보기

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열로 DB에 저장
    @Column(nullable = false)
    var writingStyle: WritingStyle, // 사용자가 선택한 문체

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "ai_assistant_selected_categories",
        joinColumns = [JoinColumn(name = "setting_id")]
    )
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var categories: MutableSet<Categories> = mutableSetOf() // 중복을 허용하지 않는 Set 사용, 초기값은 빈 Set
)