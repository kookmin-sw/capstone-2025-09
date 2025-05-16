import os
import feedparser
import openai
import boto3
import datetime
import json
import logging

# 로깅 설정
logger = logging.getLogger()
logger.setLevel(logging.INFO)

# --- 상수 설정 ---
BBC_RSS_URL = 'https://www.asiae.co.kr/rss/economy.htm'
HEADLINES_COUNT = 3
OPENAI_MODEL = "gpt-4o-mini"

# --- 프롬프트 지시사항 템플릿 및 대상 톤 설정 ---
# 기본 프롬프트 템플릿. {tone} 부분에 어조가 삽입됩니다.
BASE_PROMPT_INSTRUCTION_TEMPLATE = """
당신은 유능한 비서입니다. 이 헤드라인들을 바탕으로, 현재 주요 뉴스를 간략하게 브리핑하는 텍스트를 한국어로 작성해주세요.
{tone} 어조를 사용하고, 각 뉴스의 핵심 내용을 포함시켜 주세요. 출력에 마크다운을 사용하지 않습니다.
"""
# 생성할 목표 톤 목록
TARGET_TONES = ['존댓말', '반말', '밝은 톤', '차분한 톤']

# --- 환경 변수 로드 ---
try:
    # 필수 환경 변수
    openai_api_key = os.environ['OPENAI_API_KEY']
    s3_bucket_name = os.environ['S3_BUCKET_NAME']

    # 로컬 테스트 환경을 위한 AWS 자격 증명 (선택 사항)
    aws_access_key_id = os.environ.get('AWS_ACCESS_KEY_ID')
    aws_secret_access_key = os.environ.get('AWS_SECRET_ACCESS_KEY')
    aws_region = os.environ.get('AWS_REGION')
    aws_session_token = os.environ.get('AWS_SESSION_TOKEN')

except KeyError as e:
    # openai_api_key 또는 s3_bucket_name 누락 시
    logger.error(f"필수 환경 변수 누락: {e}")
    raise ValueError(f"필수 환경 변수 '{e}'가 설정되지 않았습니다.") from e

# --- OpenAI 클라이언트 초기화 ---
openai.api_key = openai_api_key

# --- AWS 클라이언트 초기화 (환경에 따라 분기) ---

IS_LAMBDA_ENVIRONMENT = os.environ.get('AWS_LAMBDA_FUNCTION_NAME') is not None

if IS_LAMBDA_ENVIRONMENT:
    logger.info("Lambda 환경에서 실행 중. IAM 역할을 사용하여 S3 클라이언트 초기화.")
    s3_client = boto3.client('s3') # Lambda 환경에서는 역할 기반 자동 인증 사용
else:
    logger.info("로컬 환경에서 실행 중. 환경 변수에서 AWS 자격 증명을 사용하여 S3 클라이언트 초기화.")
    # 로컬 환경에서 AWS 자격 증명 변수 확인
    if not aws_access_key_id or not aws_secret_access_key or not aws_region:
        logger.warning("로컬 테스트를 위한 AWS 자격 증명 환경 변수(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_REGION)가 "
                       "모두 설정되지 않았습니다. AWS configure 설정 등 다른 방법을 시도합니다.")
        # 환경 변수가 없더라도 일단 기본 초기화 시도 (예: ~/.aws/credentials 사용)
        s3_client = boto3.client(
             's3',
             region_name=aws_region # 리전은 지정하는 것이 좋음
         )
    else:
        # 환경 변수가 모두 있을 경우 명시적으로 사용
        s3_client = boto3.client(
            's3',
            aws_access_key_id=aws_access_key_id,
            aws_secret_access_key=aws_secret_access_key,
            region_name=aws_region,
            aws_session_token=aws_session_token
        )

def fetch_bbc_headlines(url: str, count: int) -> list[str]:
    """지정된 URL의 RSS 피드에서 최신 뉴스 헤드라인을 가져옵니다."""
    logger.info(f"RSS 피드 가져오는 중: {url}")
    feed = feedparser.parse(url)

    # feedparser가 피드 파싱 오류를 감지했는지 확인
    if feed.bozo:
        logger.warning(f"RSS 피드 파싱 오류 감지: {feed.bozo_exception}")
        # 또는 feed.entries가 비어있을 수 있으므로 확인
        if not feed.entries:
            logger.error("RSS 피드에서 항목을 찾을 수 없습니다.")
            return []

    headlines = [entry.title for entry in feed.entries[:count]]
    logger.info(f"헤드라인 {len(headlines)}개 가져옴.")
    return headlines

def generate_news_briefing(headlines: list[str], tone: str) -> str:
    """OpenAI API를 사용하여 지정된 톤으로 뉴스 헤드라인 기반 브리핑을 생성합니다."""
    if not headlines:
        logger.info(f"[{tone}] 브리핑을 생성할 헤드라인이 없습니다.")
        # 헤드라인이 없는 경우 특정 톤에 대한 메시지 반환
        return f"{tone}으로 브리핑할 뉴스가 없습니다."

    # 헤드라인 목록을 문자열로 변환
    headline_list = "\n".join([f"- {h}" for h in headlines])

    # 현재 톤을 사용하여 프롬프트 지시사항 포맷팅
    formatted_prompt_instruction = BASE_PROMPT_INSTRUCTION_TEMPLATE.format(tone=tone)

    # OpenAI에 보낼 프롬프트 구성
    prompt = f"""
    다음은 BBC 뉴스 헤드라인 상위 {len(headlines)}개입니다:
    {headline_list}

    {formatted_prompt_instruction}
    """

    logger.info(f"[{tone}] OpenAI로 뉴스 브리핑 생성 중...")
    try:
        response = openai.chat.completions.create(
            model=OPENAI_MODEL,
            messages=[
                {"role": "system", "content": "당신은 뉴스를 한국어로 요약하고 브리핑하는 유능한 비서입니다."},
                {"role": "user", "content": prompt}
            ],
            temperature=0.7, # 약간의 창의성을 허용
            max_tokens=500  # 생성될 텍스트의 최대 길이 설정
        )
        briefing = response.choices[0].message.content.strip()
        logger.info(f"[{tone}] 브리핑 생성 완료.")
        return briefing
    except Exception as e:
        logger.error(f"[{tone}] OpenAI API 호출 중 오류 발생: {e}", exc_info=True)
        raise RuntimeError(f"[{tone}] OpenAI API 호출 실패") from e

def upload_to_s3(bucket: str, content: str, tone: str) -> str:
    """생성된 브리핑 텍스트를 S3에 업로드합니다. 파일명에 톤을 포함합니다."""
    # 시간대 정보 포함된 UTC 현재 시간 사용
    now = datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=9)))
    # S3 키 형식: prompt/YYYYMMDDHH/economy/{tone}.txt
    s3_key = f"prompt/{now.strftime('%Y%m%d%H')}/economy/{tone}.txt"

    logger.info(f"[{tone}] 브리핑을 s3://{bucket}/{s3_key} 경로에 업로드 중...")
    try:
        s3_client.put_object(
            Bucket=bucket,
            Key=s3_key,
            Body=content.encode('utf-8'), # UTF-8 인코딩 명시
            ContentType='text/plain; charset=utf-8' # Content-Type 명시
        )
        logger.info(f"[{tone}] S3 업로드 완료.")
        return s3_key # 생성된 S3 키 반환
    except Exception as e:
        logger.error(f"[{tone}] S3 업로드 중 오류 발생: {e}", exc_info=True)
        raise RuntimeError(f"[{tone}] S3 업로드 실패") from e

def lambda_handler(event, context):
    """Lambda 함수 핸들러. 정의된 모든 톤에 대해 브리핑을 생성하고 S3에 업로드합니다."""
    logger.info("Lambda 함수 실행 시작.")

    results = {}
    errors = {}

    try:
        # 1단계: BBC 뉴스 헤드라인 가져오기 (모든 톤에 공통)
        headlines = fetch_bbc_headlines(BBC_RSS_URL, HEADLINES_COUNT)

        # 헤드라인이 없는 경우 조기 반환
        if not headlines:
            logger.warning("가져온 헤드라인이 없어 처리를 중단합니다.")
            return {
                'statusCode': 200,
                'body': json.dumps({'message': '처리할 뉴스 헤드라인이 없습니다.'})
            }

        # 2단계 & 3단계: 각 톤별로 브리핑 생성 및 S3 업로드
        for tone in TARGET_TONES:
            try:
                logger.info(f"--- {tone} 톤 처리 시작 ---")
                # 브리핑 생성
                briefing_text = generate_news_briefing(headlines, tone)
                # S3 업로드
                s3_path = upload_to_s3(s3_bucket_name, briefing_text, tone)
                results[tone] = f's3://{s3_bucket_name}/{s3_path}'
                logger.info(f"--- {tone} 톤 처리 완료 --- S3 경로: {results[tone]}")

            except Exception as tone_error:
                # 개별 톤 처리 중 발생한 오류 기록
                error_msg = f'{tone} 톤 처리 중 오류 발생: {tone_error}'
                logger.error(error_msg, exc_info=True)
                errors[tone] = str(tone_error)

        # 최종 결과 조합
        if not errors:
            # 모든 톤 성공
            success_message = f"모든 톤({', '.join(TARGET_TONES)})의 뉴스 브리핑이 성공적으로 생성되어 S3에 저장되었습니다."
            logger.info(success_message)
            return {
                'statusCode': 200,
                'body': json.dumps({
                    'message': success_message,
                    's3_paths': results
                })
            }
        else:
            # 일부 또는 전체 톤 실패
            error_message = f"일부 톤 처리 중 오류 발생. 성공: {list(results.keys())}, 실패: {list(errors.keys())}"
            logger.error(error_message)
            # 부분 성공/실패 상태 반환 (상태 코드 207 사용 고려 가능)
            return {
                'statusCode': 200, # 또는 207 Multi-Status
                'body': json.dumps({
                    'message': error_message,
                    'successful_paths': results,
                    'errors': errors
                })
            }

    except Exception as global_error:
        # 헤드라인 가져오기 등 초기 단계 또는 예상 못한 오류 처리
        error_message = f"Lambda 함수 실행 중 예측하지 못한 오류 발생: {global_error}"
        logger.error(error_message, exc_info=True)
        return {
            'statusCode': 500,
            'body': json.dumps({
                'message': 'Lambda 함수 실행 중 심각한 오류 발생.',
                'error': str(global_error)
            })
        } 