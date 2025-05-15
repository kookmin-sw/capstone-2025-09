import os
import logging
import requests
import json
import base64
import time

logger = logging.getLogger()
logger.setLevel(logging.INFO)

# 로거 핸들러 및 포맷터 설정 (중복 방지)
if not logger.handlers:
    handler = logging.StreamHandler()
    formatter = logging.Formatter("[%(levelname)s] %(asctime)s [%(module)s.%(funcName)s:%(lineno)d] %(message)s")
    handler.setFormatter(formatter)
    logger.addHandler(handler)
    logger.propagate = False # 루트 로거로 이벤트 전파 방지

def process_voicepack_and_swallow_exceptions(payload, context):
    """
    Cloud Run을 호출하고, 발생하는 모든 예외 (requests.exceptions.Timeout 포함)를
    내부적으로 처리하여 항상 무언가를 반환하고, Lambda 실행 자체를 중단시키지 않음.
    True: 요청 시도 및 (가정상) Cloud Run이 처리를 시작했을 것으로 예상됨.
    False: 요청 시도 전 페이로드 문제 또는 환경 문제.
    """
    function_name = "process_voicepack_and_swallow_exceptions"
    job_id = payload.get("jobId", "UnknownJobId")
    logger.info(f"JobId: {job_id}. Starting processing. Payload: {json.dumps(payload)}") # json.dumps로 보기 좋게

    try:
        voicepack_name = payload["voicepackName"]
        prompt = payload["prompt"]
        category = payload["category"]
        writing_style = payload["writingStyle"]
        now_time = payload["nowTime"]
    except KeyError as e:
        logger.error(f"JobId: {job_id}. Missing required field in payload: {e}. Cannot proceed.")
        return False # 페이로드 문제 시 False 반환

    data = {
        "jobId": job_id,
        "voicepackName": voicepack_name,
        "prompt": prompt,
        "category": category,
        "writingStyle": writing_style,
        "nowTime": now_time
    }

    cloud_run_endpoint = os.getenv("ASSISTANT_ENDPOINT")
    if not cloud_run_endpoint:
        logger.error(f"JobId: {job_id}. Environment variable ASSISTANT_ENDPOINT is not set. Cannot proceed.")
        return False # 환경 변수 문제 시 False

    logger.info(f"JobId: {job_id}. Attempting to send request to Cloud Run. Endpoint: {cloud_run_endpoint}")

    headers = {
        "Content-Type": "application/x-www-form-urlencoded" # 기존 방식 유지
    }

    # Cloud Run 호출 시 타임아웃 설정 (Lambda 함수 전체 타임아웃보다는 짧아야 함)
    # 예: Lambda가 3분(180초)이면, Cloud Run 요청 타임아웃은 10초 또는 그 이하.
    # 이 timeout은 requests 라이브러리가 응답을 기다리는 시간.
    # Cloud Run 콜드 스타트가 이를 넘기면 Timeout 예외 발생.
    request_timeout_seconds = 10

    try:
        start_time = time.time()
        # 이 호출은 Cloud Run이 응답할 때까지 대기 (최대 request_timeout_seconds)
        response = requests.post(cloud_run_endpoint, data=data, headers=headers, timeout=request_timeout_seconds)
        end_time = time.time()
        duration_ms = (end_time - start_time) * 1000

        logger.info(f"JobId: {job_id}. Cloud Run call attempt completed. Duration: {duration_ms:.2f}ms. Status: {response.status_code}. Response (first 200 chars): {response.text[:200]}")

        # Cloud Run이 요청을 성공적으로 받았거나(200) 수락했으면(202)
        # Lambda 입장에서는 "할 일 다 했다" (Cloud Run이 나중에 처리할 것)
        if response.status_code in (200, 202):
            logger.info(f"JobId: {job_id}. Cloud Run request successfully sent/accepted.")
            return True
        else:
            # Cloud Run이 명시적인 에러를 반환한 경우
            logger.error(f"JobId: {job_id}. Cloud Run returned a non-success status: {response.status_code}. This might indicate an issue with the request or Cloud Run service.")
            return True # 요청은 보냈으므로 True. 콜백에서 실패 처리 기대. 또는 False로 하고 별도 처리. 여기선 True.

    except requests.exceptions.Timeout:
        # Lambda 내에서 Cloud Run 응답 대기 중 설정한 timeout(10초) 발생
        # 이 경우에도 Cloud Run은 백그라운드에서 요청을 받았을 수 있고, 콜드 스타트 후 처리할 것임.
        # 따라서 Lambda 입장에서는 "요청은 보냈다고 가정, 나중에 콜백 오겠지"
        logger.warning(f"JobId: {job_id}. Cloud Run request timed out in Lambda (after {request_timeout_seconds}s). Assuming Cloud Run received the request and will process it eventually. Awaiting callback.")
        return True # 요청은 보냈다고 가정

    except requests.exceptions.RequestException as e:
        # 네트워크 오류 등 requests 라이브러리 자체의 예외
        logger.error(f"JobId: {job_id}. Cloud Run request failed due to RequestException: {str(e)}", exc_info=True)
        # 이 경우는 요청 자체가 전달되지 않았을 가능성이 높음.
        # 하지만, 여기서는 "일단 한번 시도"라는 목표에 따라, 복잡한 재시도 로직 없이 True를 반환하여 MQ에서 제거.
        # 실제로는 이런 경우 실패로 간주하고 DLQ로 보내거나, Lambda 내부 재시도 고려.
        # 여기서는 단순화를 위해 True 반환. (또는 False 반환 후 lambda_handler에서 다른 처리)
        return True # 일단 요청 시도는 했으므로 True

    except Exception as e:
        # 기타 예기치 않은 오류
        logger.error(f"JobId: {job_id}. An unexpected error occurred during Cloud Run request: {str(e)}", exc_info=True)
        return False # 예기치 못한 오류는 False

def lambda_handler(event, context):
    """Amazon MQ 트리거용 Lambda 진입점"""
    function_name = "lambda_handler"
    aws_request_id = context.aws_request_id if context else "N/A"
    logger.info(f"Lambda triggered. RequestId: {aws_request_id}")

    # 환경 변수 로깅
    logger.info(f"ASSISTANT_ENDPOINT: {os.getenv('ASSISTANT_ENDPOINT')}")

    if context:
        logger.info(f"Function Name: {context.function_name}, Version: {context.function_version}, Timeout: {context.get_remaining_time_in_millis() / 1000.0}s (approx)")

    rmq_messages = event.get("rmqMessagesByQueue", {})
    messages = rmq_messages.get("ai-assistant::/", [])

    if not messages:
        logger.info("No messages found. Exiting.")
        return {"statusCode": 200, "body": "No messages to process"}

    logger.info(f"Received {len(messages)} messages. BatchSize from trigger should be 1 if processing one by one like this.")

    # 현재 BatchSize가 1이라고 가정하고, messages 리스트에는 하나의 메시지만 들어있을 것임
    # 만약 BatchSize가 1보다 크면, 이 루프는 여러 메시지를 순차 처리함
    for i, msg in enumerate(messages):
        msg_number = i + 1
        job_id_for_log = "UnknownJobIdInLoop"

        logger.info(f"Processing message {msg_number} of {len(messages)}.")
        try:
            base64_data = msg.get("data")
            if not base64_data:
                logger.error(f"Message {msg_number} data is missing.")
                continue # 다음 메시지로 (BatchSize > 1 경우) 또는 루프 종료 (BatchSize = 1 경우)

            decoded_bytes = base64.b64decode(base64_data)
            decoded_json_string = decoded_bytes.decode("utf-8")
            decoded_json = json.loads(decoded_json_string)

            job_id_for_log = decoded_json.get("jobId", "UnknownJobIdInLoop")

            required_fields = ["jobId", "voicepackName", "prompt", "category", "writingStyle", "nowTime"]
            missing_fields = [field for field in required_fields if field not in decoded_json]
            if missing_fields:
                logger.error(f"Message {msg_number} (JobId: {job_id_for_log}). Missing required fields: {missing_fields}.")
                continue

            payload = decoded_json # 전체 decoded_json을 페이로드로 사용 가능

            # process_voicepack_and_swallow_exceptions는 내부에서 예외를 처리하므로,
            # 이 함수 호출 자체가 실패하는 경우는 거의 없음 (예: Python 인터프리터 오류)
            # 반환값은 Cloud Run 요청 시도 결과 (True/False)이지만, Lambda 자체의 성공/실패를 결정짓진 않음
            process_voicepack_and_swallow_exceptions(payload, context)

            # 특별히 여기서 할 일 없음. 위 함수가 True/False를 반환하지만,
            # Lambda 실행은 계속 성공으로 간주하여 MQ에서 메시지가 ACK되도록 함.

        except Exception as e:
            # 메시지 파싱 실패 등 process_voicepack 호출 전의 예외
            logger.error(f"Message {msg_number} (JobId: {job_id_for_log}). Critical error before calling process_voicepack: {str(e)}", exc_info=True)
            # 이 경우, 해당 메시지는 처리 실패.
            # BatchSize가 1이면 이 Lambda 실행은 이 메시지에 대해 실패한 것.
            # 하지만 다음 return에서 statusCode 200을 반환하면 MQ는 성공으로 간주할 수 있음.
            # 진정한 실패 처리를 위해서는 여기서 예외를 다시 던지거나(raise),
            # "Report batch item failures" 메커니즘 사용 필요.
            # 현재 목표는 "MQ에서 안 없어지고 반복 실행"을 막는 것이므로,
            # 일단 파싱 실패 시에도 Lambda는 정상 종료되도록 둠.
            pass # 또는 continue

    # Lambda가 성공적으로 모든 메시지 처리를 시도했음 (개별 요청의 성공/실패와 별개)
    # 이렇게 하면 MQ는 이 배치(BatchSize=1이면 단일 메시지)를 ACK 처리함.
    logger.info("Finished attempting to process all received messages. Lambda will exit successfully.")
    return {
        "statusCode": 200, # 또는 202
        "body": "All received messages have been processed (or attempted)."
    }
