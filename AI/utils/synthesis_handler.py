import logging
from utils.voice_synthesizer import VoiceSynthesizer
from utils.storage_manager import StorageManager
from utils.sqs_handler import SQSHandler

logger = logging.getLogger(__name__)
voice_synthesizer = VoiceSynthesizer()
storage_manager = StorageManager()
sqs_handler = SQSHandler()

async def process_synthesis_request(
    prompt: str,
    voicepackName: str,
    userId: int,
    jobId: int,
    speed: float
):
    """비동기로 음성 합성 및 SQS 메시지 전송
    
    Args:
        prompt (str): 합성 프롬프트
        voicepackName (str): 음성팩 이름
        userId (int): 사용자 ID
        jobId (int): 작업 ID
        speed (float): 음성 속도 (1.0 기본, 선택사항)
    """
    try:
        logger.info(f"starting speech synthesis: voicepackName={voicepackName}, prompt={prompt}, userId={userId}, jobId={jobId}, speed={speed}")
        
        # 음성 합성 시작
        audio_url, duration = await voice_synthesizer.synthesize_speech(
            prompt=prompt,
            voicepackName=voicepackName,
            userId=userId,
            speed=speed
        )
        
        if audio_url is None:
            raise ValueError(f"failed to synthesize speech")
        
        logger.info(f"speech synthesized: duration={duration} seconds")
        
        await sqs_handler.send_synthesize_message(
            jobId=jobId,
            success=True,
            additional_params={
                "resultUrl": audio_url
            }
        )
        
    except Exception as e:
        logger.error(f"failed to synthesize speech: {str(e)}", exc_info=True)
        # 실패 메시지 전송
        try:
            await sqs_handler.send_synthesize_message(
                jobId=jobId,
                success=False,
                additional_params={
                    "errorMessage": str(e)
                }
            )
            
        except Exception as sqs_error:
            logger.error(f"failed to send SQS message: {str(sqs_error)}", exc_info=True)