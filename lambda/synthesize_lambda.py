import os
import logging
import requests
import json
import base64

logger = logging.getLogger()
logger.setLevel(logging.INFO)

for handler in logger.handlers:
    handler.setFormatter(logging.Formatter("[%(levelname)s] %(message)s"))


def process_voicepack(payload):
    """Cloud Run 호출 및 재시도 로직"""
    try:
        job_id = payload["jobId"]
        voicepack_name = payload["voicepackName"]
        prompt = payload["prompt"]
        user_id = payload["userId"]
    except KeyError as e:
        logger.error(f"[Error] Missing required field: {e}")
        return

    data = {
        "jobId": job_id,
        "userId": user_id,
        "voicepackName": voicepack_name,
        "prompt": prompt,
    }

    cloud_run_endpoint = os.getenv("SYNTHESIZE_ENDPOINT")

    logger.info(f"[Start] Cloud Run 요청 시작: jobId: {job_id}")

    try:
        headers = {"Content-Type": "application/x-www-form-urlencoded"}

        response = requests.post(
            cloud_run_endpoint, data=data, headers=headers, timeout=100
        )
        logger.info(f"Cloud Run 응답 코드: {response.status_code}")

        if response.status_code == 202:
            logger.info(f"[Success] Cloud Run 처리 완료: jobId: {job_id}")
            return

    except requests.exceptions.RequestException as e:
        logger.error(f"[Error] Cloud Run request failed: {str(e)}")

    logger.error(f"[Fail] Cloud Run 요청 실패")


def lambda_handler(event, context):
    """Amazon MQ 트리거용 Lambda 진입점"""

    logger.info(f"Lambda triggered by Amazon MQ")
    # print("this is event", event)
    rmq_messages = event.get("rmqMessagesByQueue", {})
    print("this is rmq", rmq_messages)
    messages = rmq_messages.get("synthesis::/", [])
    print("this is messages", messages)
    for msg in messages:
        try:
            base64_data = msg["data"]
            decoded_bytes = base64.b64decode(base64_data)
            decoded_json = json.loads(decoded_bytes.decode("utf-8"))
            payload = {
                "userId": decoded_json["userId"],
                "jobId": decoded_json["id"],
                "voicepackName": decoded_json["voicepackName"],
                "prompt": decoded_json["prompt"],
            }
            process_voicepack(payload)
        except Exception as e:
            logger.error(f"[Error] Failed to process message: {str(e)}")

    return {"statusCode": 202, "body": "Done"}
