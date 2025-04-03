import logging
from utils.voice_synthesizer import VoiceSynthesizer
from utils.callback_handler import send_callback

logger = logging.getLogger(__name__)
voice_synthesizer = VoiceSynthesizer()

async def process_voice_registration(
    voicepackId: str,
    file_content: bytes,
    voicepackRequestId: int
):
    """비동기로 화자 등록 처리 및 콜백 전송
    
    Args:
        voicepackId (str): 음성팩 ID
        file_content (bytes): 음성 파일 데이터
        voicepackRequestId (int): 음성팩 요청 ID
    """
    logger.info(f"화자 등록 처리 시작: voicepackId={voicepackId}, voicepackRequestId={voicepackRequestId}")
    
    try:
        # 화자 특징 추출
        logger.info(f"화자 특징 추출 시작: voicepackId={voicepackId}")
        features_result = await voice_synthesizer.extract_speaker_features(
            voicepackId=voicepackId,
            file_content=file_content
        )
        
        if not features_result:
            logger.error(f"화자 특징 추출 실패: voicepackId={voicepackId}")
            raise Exception("화자 특징 추출 실패")
        
        logger.info(f"화자 특징 추출 성공: voicepackId={voicepackId}")
        
        # 성공 콜백
        await send_callback(
            voicepackId=voicepackId,
            voicepackRequestId=voicepackRequestId,
            status="success",
            callback_path="/api/voicepack/callback"
        )

        logger.info(f"화자 등록 성공: voicepackId={voicepackId}, voicepackRequestId={voicepackRequestId}")

    except Exception as e:
        logger.error(f"화자 등록 실패: {str(e)}", exc_info=True)
        # 실패 콜백
        try:
            await send_callback(
                voicepackId=voicepackId,
                voicepackRequestId=voicepackRequestId,
                status="failed",
                callback_path="/api/voicepack/callback"
            )
        except Exception as callback_error:
            logger.error(f"콜백 전송 실패: {str(callback_error)}", exc_info=True) 