package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.callback

import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.entity.SynthesisStatus

/**
 * Cloud Run 에서 개별 음성 합성 Job의 결과를 콜백으로 전송할 때 사용하는 요청 DTO.
 */
data class AiAssistantJobCallbackRequest(
    // 처리된 Job의 ID
    val jobId: Long,
    // Job 처리 결과 상태 (SUCCESS 또는 FAILURE)
    val status: SynthesisStatus,
    // Job 성공 시 결과 파일 S3 키 (실패 시 null)
    val resultS3Key: String? = null
) 