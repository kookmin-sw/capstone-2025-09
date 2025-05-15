package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.enums

enum class Categories(val description: String) {
    BBC_NEWS("bbcnews"),      // BBC 뉴스
    GOOGLE_NEWS("googlenews"),// Google 뉴스
    IT_NEWS("itnews"),        // IT 뉴스
    ECONOMY("economy"), // 경제
    SPORTS("sports"); // 스포츠

    companion object {
        // 프론트엔드에서 전달받은 인덱스(0, 1, 2 ...)를 Enum 값으로 변환
        fun fromIndex(index: Int): Categories? {
            return when (index) {
                0 -> BBC_NEWS
                1 -> GOOGLE_NEWS
                2 -> IT_NEWS
                3 -> ECONOMY
                4 -> SPORTS
                else -> null // 유효하지 않은 인덱스 처리
            }
        }
    }
}