import logging
from .voice_synthesizer import VoiceSynthesizer
from .storage_manager import StorageManager
from .sqs_handler import SQSHandler

logger = logging.getLogger(__name__)
voice_synthesizer = VoiceSynthesizer()
storage_manager = StorageManager()
sqs_handler = SQSHandler()

async def process_synthesis_request(
    prompt: str,
    voicepackName: str,
    userId: int,
    jobId: int,
    speed: float,
    emotionIndex: int
):
    """음성 합성 및 SQS 메시지 전송
    
    Args:
        prompt (str): 합성 프롬프트
        voicepackName (str): 음성팩 이름
        userId (int): 사용자 ID
        jobId (int): 작업 ID
        speed (float): 음성 속도 (1.0 기본, 선택사항),
        emotionIndex (int): 감정 인덱스 (0 기본, 선택사항)
    """
    try:
        logger.info(f"starting speech synthesis: voicepackName={voicepackName}, prompt={prompt}, userId={userId}, jobId={jobId}, speed={speed}")
        
        # 음성 합성 시작
        audio_url, duration = await voice_synthesizer.synthesize_speech(
            prompt=prompt,
            voicepackName=voicepackName,
            userId=userId,
            speed=speed,
            emotionIndex=emotionIndex
        )
        
        if audio_url is None:
             raise ValueError(f"failed to synthesize speech, audio_url is None")

        logger.info(f"speech synthesized: duration={duration} seconds")
        
        await sqs_handler.send_synthesize_message(
            jobId=jobId,
            success=True,
            additional_params={
                "resultUrl": audio_url
            }
        )
        
    except Exception as e:
        logger.error(f"Error during speech synthesis: {str(e)}", exc_info=True)
        error_message = str(e)
        
        try:
            await sqs_handler.send_synthesize_message(
                jobId=jobId,
                success=False, 
                additional_params={
                    "errorMessage": error_message
                }
            )
        except Exception as sqs_error:
            logger.error(f"failed to send SQS message: {str(sqs_error)}", exc_info=True)


async def process_assistant_request(
    prompt: str,
    voicepackName: str,
    jobId: int,
    category: str,
    writingStyle: str,
    nowTime: str,
    speed: float
):
    """AI 비서용 음성 합성 및 SQS 메시지 전송
    
    Args:
        prompt (str): 합성 프롬프트
        voicepackName (str): 음성팩 이름
        jobId (int): 작업 ID
        category (str): 카테고리
        writingStyle (str): 글쓰기 스타일
        nowTime (str): 현재 시간 (YYYYMMDDHH)
        speed (float): 음성 속도 (1.0 기본, 선택사항)
    """
    try:
        logger.info(f"starting assistant synthesis: voicepackName={voicepackName}, prompt={prompt}, jobId={jobId}, category={category}, writingStyle={writingStyle}, nowTime={nowTime}, speed={speed}")

        audio_url, duration = await voice_synthesizer.synthesize_assistant(
            prompt=prompt,
            voicepackName=voicepackName,
            speed=speed,
            category=category,
            writingStyle=writingStyle,
            nowTime=nowTime
        )     
        
        if audio_url is None:
             raise ValueError(f"failed to synthesize assistant speech, audio_url is None")
        
        logger.info(f"assistant synthesized: duration={duration} seconds")
        
        await sqs_handler.send_assistant_message(
            jobId=jobId,
            success=True,
            additional_params={
                "resultS3Key": audio_url
            }
        )
        
    except Exception as e:
        logger.error(f"Error during assistant synthesis: {str(e)}", exc_info=True)
        error_message = str(e)
            
        try:
            await sqs_handler.send_assistant_message(
                jobId=jobId,
                success=False,
                additional_params={
                    "errorMessage": error_message
                }
            )
            
        except Exception as sqs_error:
            logger.error(f"failed to send SQS message: {str(sqs_error)}", exc_info=True)