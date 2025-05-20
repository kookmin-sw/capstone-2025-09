import json
from typing import List, Tuple

from config.settings import OPENAI_CONFIG
from openai import OpenAI

_API_KEY = OPENAI_CONFIG['api_key']
if not _API_KEY:
    raise EnvironmentError("환경 변수 OPENAI_API_KEY 가 설정되지 않았습니다.")
client = OpenAI(api_key=_API_KEY)

SYSTEM_PROMPT = """
당신은 대한민국 뉴스 원고를 음성 합성용 대본으로 교정하고, 동시에 해당 텍스트의 안전성을 검사하는 전문가입니다.

규칙
1. 모든 아라비아 숫자를 한국어로 완전하게 풀어쓰십시오.
   - 예) 2025년 5월 19일 → 이천이십오년 오월 십구일
         8명 → 여덟명
         50% → 오십퍼센트
2. 숫자 뒤 단위(년·명·% 등)는 그대로 유지하되 숫자만 변환하십시오.
3. 맞춤법·고유명사·어순은 절대로 변경하지 마십시오.
4. 한 문장이 40음절을 넘으면 의미가 자연스러운 쉼표(,)·접속어(그리고·하지만 등) 지점에서
   추가로 문장을 나누어 주십시오.
5. 제공된 텍스트에 불법적이거나 사회적으로 용납되지 않는 내용 (예: 폭력, 욕설, 증오 발언, 불법 약물, 성인 콘텐츠 및 보이스피싱 등)이 포함되어 있는지 판단하십시오.
6. 부가 설명이나 요약 없이 **결과 문장과 안전성 판단 결과만** 제공하십시오.
7. 마크다운은 사용하지 않습니다.
8. **최종 출력은 다음 JSON 형식의 문자열로만 출력하십시오:**
   {
     "safe": [True/False],
     "Text": ["문장1", "문장2", ...]
   }
   "safe" 키의 값은 텍스트가 안전하면 True, 그렇지 않으면 False입니다.
   "Text" 키의 값은 규칙에 따라 변환되고 분할된 문장들의 리스트입니다. 만약 텍스트가 안전하지 않아 "safe"가 False라면, "Text"는 빈 리스트([])여야 합니다.
"""

def convert_text(
    text: str,
    model: str = "gpt-4o-mini",
    *,
    temperature: float = 0.0,
) -> Tuple[bool, List[str]]:
    """
    Parameters
    ----------
    article : str
        원본 기사(또는 일반 텍스트).
    model : str, default "gpt-4o-mini"
        사용할 OpenAI Chat 모델 이름.
    temperature : float, default 0.0
        창의성 제어용 파라미터. 0이면 가장 일관된 결과.

    Returns
    ------
    Tuple[bool, List[str]]
        (안전성 여부, 숫자 변환 및 문장 분할이 완료된 문장 리스트).
        안전하지 않은 경우 (False, [])를 반환.
    """
    completion = client.chat.completions.create(
        model=model,
        messages=[
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": text},
        ],
        temperature=temperature,
        response_format={ "type": "json_object" }
    )

    raw_content = completion.choices[0].message.content.strip()
    try:
        parsed_response = json.loads(raw_content)
        
        if not isinstance(parsed_response, dict) or \
           "safe" not in parsed_response or not isinstance(parsed_response["safe"], bool) or \
           "Text" not in parsed_response or not isinstance(parsed_response["Text"], list) or \
           not all(isinstance(s, str) for s in parsed_response["Text"]):
            raise ValueError("응답 형식이 올바르지 않습니다.")

        is_safe = parsed_response["safe"]
        sentences = parsed_response["Text"]

        if not is_safe and sentences:
             sentences = []
        
    except json.JSONDecodeError as exc:
        raise ValueError(
            "모델 응답을 JSON으로 파싱하지 못했습니다. "
            f"원본 응답: {raw_content!r}"
        ) from exc
    except ValueError as exc:
        raise ValueError(
            f"모델 응답 형식이 예상과 다릅니다. {str(exc)} "
            f"원본 응답: {raw_content!r}"
        ) from exc
    except Exception as exc:
        raise ValueError(
            "모델 응답 처리 중 알 수 없는 오류가 발생했습니다. "
            f"원본 응답: {raw_content!r}"
        ) from exc

    return is_safe, sentences