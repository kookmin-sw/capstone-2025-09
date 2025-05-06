package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.message_queue

/**
 * AI 비서 음성 합성 요청을 위해 RabbitMQ로 전송하는 메시지 DTO.
 * S3 키 대신 실제 프롬프트 내용과 필요한 메타데이터를 포함합니다.
 */
data class AiAssistantSynthesisMqRequest(
    // 처리할 Job의 ID
    val jobId: Long,
    // S3에서 읽어온 실제 프롬프트 내용
    val prompt: String,
    // 대상 카테고리 
    val category: String,
    // 적용된 글쓰기 스타일
    val writingStyle: String,
    // 사용된 보이스팩 이름
    val voicepackName: String,
    // 작업 기준 시간 (YYYYMMDDHH)
    val nowTime: String
) 