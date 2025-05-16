import os
import logging
import requests
import json
import base64

# 로그 설정
logger = logging.getLogger()
logger.setLevel(logging.INFO)
for handler in logger.handlers:
    handler.setFormatter(logging.Formatter("[%(levelname)s] %(message)s"))

def process_voicepack_and_swallow(payload):
    """Cloud Run 호출을 1회만 수행하고, 예외는 모두 swallow"""
    job_id = payload.get("jobId", "UnknownJobId")
    logger.info(f"[Start] Cloud Run 호출 시작 - jobId: {job_id}")

    try:
        voicepack_name = payload["voicepackName"]
        prompt = payload["prompt"]
        user_id = payload["userId"]
        emotion_index = payload["emotionIndex"]
    except KeyError as e:
        logger.error(f"[Error] 누락된 필드: {e}")
        return

    data = {
        "jobId": job_id,
        "userId": user_id,
        "voicepackName": voicepack_name,
        "prompt": prompt,
        "emotionIndex": emotion_index,
    }

    cloud_run_endpoint = os.getenv("SYNTHESIZE_ENDPOINT")
    if not cloud_run_endpoint:
        logger.error("[Error] 환경변수 SYNTHESIZE_ENDPOINT 누락")
        return

    try:
        headers = {"Content-Type": "application/x-www-form-urlencoded"}
        response = requests.post(cloud_run_endpoint, data=data, headers=headers, timeout=10)

        logger.info(f"[Response] Cloud Run 응답 코드: {response.status_code}")
        if response.status_code == 202:
            logger.info(f"[Success] Cloud Run 처리 완료 - jobId: {job_id}")
        else:
            logger.warning(f"[Warning] 비정상 응답: {response.status_code}")
    except requests.exceptions.Timeout:
        logger.warning("[Timeout] Cloud Run 요청 타임아웃 발생 (10초). 콜드스타트 가능성.")
    except requests.exceptions.RequestException as e:
        logger.error(f"[Error] Cloud Run 요청 실패: {str(e)}")
    except Exception as e:
        logger.error(f"[Exception] 처리 중 알 수 없는 에러: {str(e)}")

    logger.info(f"[End] Cloud Run 호출 종료 - jobId: {job_id}")

def lambda_handler(event, context):
    """Amazon MQ 트리거용 Lambda 진입점"""

    logger.info("Lambda 시작 - Amazon MQ 트리거")
    rmq_messages = event.get("rmqMessagesByQueue", {})
    messages = rmq_messages.get("synthesis::/", [])

    if not messages:
        logger.info("처리할 메시지가 없습니다.")
        return {"statusCode": 200, "body": "No messages"}

    for i, msg in enumerate(messages):
        logger.info(f"[{i+1}/{len(messages)}] 메시지 처리 시작")
        try:
            base64_data = msg["data"]
            decoded_bytes = base64.b64decode(base64_data)
            decoded_json = json.loads(decoded_bytes.decode("utf-8"))

            payload = {
                "userId": decoded_json.get("userId"),
                "jobId": decoded_json.get("id"),
                "voicepackName": decoded_json.get("voicepackName"),
                "prompt": decoded_json.get("prompt"),
                "emotionIndex": decoded_json.get("emotionIndex"),
            }

            process_voicepack_and_swallow(payload)

        except Exception as e:
            logger.error(f"[Error] 메시지 처리 실패: {str(e)}")

    return {
        "statusCode": 200,
        "body": "Done"
    }
