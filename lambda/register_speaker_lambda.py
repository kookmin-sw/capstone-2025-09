import os
import logging
import requests
import json
import base64
import time

logger = logging.getLogger()
logger.setLevel(logging.INFO)
for handler in logger.handlers:
    handler.setFormatter(logging.Formatter("[%(levelname)s] %(message)s"))

def process_voicepack_and_swallow(payload):
    """Cloud Run에 단 1회 요청하고, 예외 발생 시에도 swallow"""
    voicepack_id = payload.get("voicepackId", "UnknownId")
    voicepack_request_id = payload.get("voicepackRequestId", "UnknownRequestId")

    logger.info(f"[Start] Cloud Run 호출 시작 - voicepackId: {voicepack_id}, voicepackRequestId: {voicepack_request_id}")

    cloud_run_endpoint = os.getenv("REGISTER_SPEAKER_ENDPOINT")
    if not cloud_run_endpoint:
        logger.error("[Error] 환경변수 REGISTER_SPEAKER_ENDPOINT 누락")
        return

    try:
        voice_file_data = base64.b64decode(payload["voiceFile"])
    except (KeyError, base64.binascii.Error) as e:
        logger.error(f"[Error] voiceFile decode 실패: {e}")
        return

    # multipart/form-data 구성
    files = {
        'voiceFile': ('voice.wav', voice_file_data, 'application/octet-stream')
    }
    data = {
        'voicepackId': voicepack_id,
        'voicepackRequestId': str(voicepack_request_id)
    }

    try:
        timeout_sec = 10
        start = time.time()
        response = requests.post(cloud_run_endpoint, data=data, files=files, timeout=timeout_sec)
        duration = (time.time() - start) * 1000

        logger.info(f"[Response] Cloud Run 응답 코드: {response.status_code} ({duration:.1f}ms)")
        if response.status_code in (200, 202):
            logger.info(f"[Success] Cloud Run 정상 처리 완료")
        else:
            logger.warning(f"[Warning] 비정상 응답: {response.status_code}")
    except requests.exceptions.Timeout:
        logger.warning(f"[Timeout] Cloud Run 타임아웃 발생 ({timeout_sec}s). 콜드스타트 예상.")
    except requests.exceptions.RequestException as e:
        logger.error(f"[Error] Cloud Run 요청 실패: {str(e)}")
    except Exception as e:
        logger.error(f"[Exception] 처리 중 알 수 없는 에러: {str(e)}")

    logger.info(f"[End] Cloud Run 호출 종료 - voicepackId: {voicepack_id}")

def lambda_handler(event, context):
    logger.info("Lambda 시작 - Amazon MQ 트리거")

    rmq_messages = event.get("rmqMessagesByQueue", {})
    messages = rmq_messages.get("convert::/", [])

    if not messages:
        logger.info("처리할 메시지가 없습니다.")
        return {"statusCode": 200, "body": "No messages"}

    for i, msg in enumerate(messages):
        logger.info(f"[{i+1}/{len(messages)}] 메시지 처리 시작")

        try:
            base64_data = msg["data"]
            decoded_bytes = base64.b64decode(base64_data)
            decoded_json = json.loads(decoded_bytes.decode("utf-8"))

            process_voicepack_and_swallow(decoded_json)
        except Exception as e:
            logger.error(f"[Error] 메시지 처리 실패: {str(e)}")

    return {
        "statusCode": 200,
        "body": "Done"
    }
