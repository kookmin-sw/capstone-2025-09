import logging
import httpx
from config.settings import BACKEND_CONFIG
from typing import Optional, Dict, Any

logger = logging.getLogger(__name__)

async def send_callback(
    voicepackId: str,
    voicepackRequestId: int,
    status: str,
    callback_path: str,
    additional_params: Optional[Dict[str, Any]] = None
):
    """콜백 전송 함수
    
    Args:
        voicepackId (str): 음성팩 ID
        voicepackRequestId (int): 음성팩 요청 ID
        status (str): 상태 ('success' 또는 'failed')
        callback_path (str): 콜백 엔드포인트 경로 (예: '/api/voicepack/callback')
        additional_params (Optional[Dict[str, Any]]): 추가 쿼리 파라미터
    """
    base_url = BACKEND_CONFIG['callback_url']
    callback_url = f"{base_url}{callback_path}"
    
    # 기본 파라미터 설정
    params = {
        'voicepackRequestId': voicepackRequestId,
        'status': status
    }
    
    # 추가 파라미터가 있다면 병합
    if additional_params:
        params.update(additional_params)
    
    async with httpx.AsyncClient() as client:
        try:
            logger.info(f"{status} 콜백 전송 시작: voicepackId={voicepackId}")
            
            # URL 파라미터 구성
            query_string = '&'.join([f"{k}={v}" for k, v in params.items()])
            callback_url_with_params = f'{callback_url}?{query_string}'
            
            logger.info(f"callback_url: {callback_url_with_params}")
            
            await client.get(callback_url_with_params)
            
            logger.info(f"{status} 콜백 전송 완료: voicepackId={voicepackId}")
            
        except Exception as e:
            logger.error(f"콜백 전송 실패: {str(e)}", exc_info=True)
            raise 