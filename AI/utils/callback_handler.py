import logging
import httpx
from config.settings import BACKEND_CONFIG

logger = logging.getLogger(__name__)

async def send_callback(
    voicepackId: str,
    voicepackRequestId: int,
    status: str
):
    """콜백 전송 함수
    
    Args:
        voicepackId (str): 음성팩 ID
        voicepackRequestId (int): 음성팩 요청 ID
        status (str): 상태 ('success' 또는 'failed')
    """
    callback_url = f"{BACKEND_CONFIG['callback_url']}/api/voicepack/callback"
    
    async with httpx.AsyncClient() as client:
        try:
            logger.info(f"{status} 콜백 전송 시작: voicepackId={voicepackId}")
            
            callback_url_with_params = f'{callback_url}?voicepackRequestId={voicepackRequestId}&status={status}'
            logger.info(f"callback_url: {callback_url_with_params}")
            
            await client.get(callback_url_with_params)
            
            logger.info(f"{status} 콜백 전송 완료: voicepackId={voicepackId}")
            
        except Exception as e:
            logger.error(f"콜백 전송 실패: {str(e)}", exc_info=True)
            raise 