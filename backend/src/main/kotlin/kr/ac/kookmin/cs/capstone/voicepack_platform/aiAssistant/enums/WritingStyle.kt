package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums

// 문체(말투) 종류를 정의하는 Enum
enum class WritingStyle(val description: String) {
    POLITE("존댓말"),          // 존댓말
    INFORMAL("반말"),        // 반말
    BRIGHT("밝은 톤"),        // 밝은 톤
    CALM("차분한 톤");          // 차분한 톤

    companion object {
        fun fromIndex(index: Int): WritingStyle? {
            return when(index) {
                0 -> POLITE
                1 -> INFORMAL
                2 -> BRIGHT
                3 -> CALM
                else -> null
            }
        }
    }
}