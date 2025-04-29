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
        voicepackId = payload["voicepackId"]
        voiceFile = payload["voiceFile"]
        voicepackRequestId = payload["voicepackRequestId"]
    except KeyError as e:
        logger.error(f"[Error] Missing required field: {e}")
        return

    cloud_run_endpoint = os.getenv("REGISTER_SPEAKER_ENDPOINT")

    logger.info(
        f"[Start] Cloud Run 요청 시작: voicepackId: {voicepackId}, voicepackRequestId: {voicepackRequestId}"
    )

    try:
        # multipart/form-data 형식에 맞게
        files = {
            "voiceFile": (
                "voice.wav",
                base64.b64decode(voiceFile),
                "application/octet-stream",
            )
        }
        data = {
            "voicepackId": voicepackId,
            "voicepackRequestId": str(voicepackRequestId),
        }

        response = requests.post(
            cloud_run_endpoint, data=data, files=files, timeout=150
        )
        logger.info(f"Cloud Run 응답 코드: {response.status_code}")

        if response.status_code == 202:
            logger.info(
                f"[Success] Cloud Run 처리 완료: voicepackId: {voicepackId}, voicepackRequestId: {voicepackRequestId}"
            )
            return

    except requests.exceptions.RequestException as e:
        logger.error(f"[Error] Cloud Run request failed: {str(e)}")

    logger.error(f"[Fail] Cloud Run 요청 실패")


def lambda_handler(event, context):
    """Amazon MQ 트리거용 Lambda 진입점"""

    logger.info(f"Lambda triggered by Amazon MQ")
    rmq_messages = event.get("rmqMessagesByQueue", {})
    messages = rmq_messages.get("convert::/", [])
    # print("messages", messages)

    for msg in messages:
        try:
            base64_data = msg["data"]
            decoded_bytes = base64.b64decode(base64_data)
            decoded_json = json.loads(decoded_bytes.decode("utf-8"))
            payload = {
                "voicepackId": decoded_json["voicepackId"],
                "voiceFile": decoded_json["voiceFile"],
                "voicepackRequestId": decoded_json["voicepackRequestId"],
            }
            process_voicepack(payload)
        except Exception as e:
            logger.error(f"[Error] Failed to process message: {str(e)}")

    return {"statusCode": 202, "body": "Done"}
