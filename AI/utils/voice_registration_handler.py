import logging
from utils.voice_synthesizer import VoiceSynthesizer
from utils.sqs_handler import SQSHandler

logger = logging.getLogger(__name__)
voice_synthesizer = VoiceSynthesizer()
sqs_handler = SQSHandler()

async def process_voice_registration(
    voicepackId: str,
    file_content: bytes,
    voicepackRequestId: int
):
    """비동기로 화자 등록 처리 및 SQS 메시지 전송
    
    Args:
        voicepackId (str): 음성팩 ID
        file_content (bytes): 음성 파일 데이터
        voicepackRequestId (int): 음성팩 요청 ID
    """
    logger.info(f"starting speaker registration: voicepackId={voicepackId}, voicepackRequestId={voicepackRequestId}")
    
    try:
        # 화자 특징 추출
        logger.info(f"extracting speaker features: voicepackId={voicepackId}")
        features_result = await voice_synthesizer.extract_speaker_features(
            voicepackId=voicepackId,
            file_content=file_content
        )
        
        if not features_result:
            logger.error(f"failed to extract speaker features: voicepackId={voicepackId}")
            raise Exception("failed to extract speaker features")
        
        logger.info(f"speaker features extracted: voicepackId={voicepackId}")
        
        # 성공 메시지 전송
        await sqs_handler.send_register_message(
            voicepackRequestId=voicepackRequestId,
            status="success"
        )

        logger.info(f"speaker registered: voicepackId={voicepackId}, voicepackRequestId={voicepackRequestId}")

    except Exception as e:
        logger.error(f"failed to register speaker: {str(e)}", exc_info=True)
        # 실패 메시지 전송
        try:
            await sqs_handler.send_register_message(
                voicepackRequestId=voicepackRequestId,
                status="failed",
                additional_params={"error": str(e)}
            )
        except Exception as sqs_error:
            logger.error(f"failed to send SQS message: {str(sqs_error)}", exc_info=True) 
