import json
from typing import List, Tuple

from config.settings import OPENAI_CONFIG
from openai import OpenAI

_API_KEY = OPENAI_CONFIG['api_key']
if not _API_KEY:
    raise EnvironmentError("환경 변수 OPENAI_API_KEY 가 설정되지 않았습니다.")
client = OpenAI(api_key=_API_KEY)

SYSTEM_PROMPT = """
[prompt]
다음 한국어 뉴스 원고를 음성 합성용 대본으로 변환하고 안전성을 평가하십시오.

작업 절차:
1. 모든 아라비아 숫자를 한국어 완전표기(예: 2025년 5월 19일→이천이십오년 오월 십구일, 50%→오십퍼센트)로 변환하되, 단위(년·명·% 등)는 그대로 유지합니다.
2. 맞춤법·고유명사·어순은 절대 변경하지 마십시오.
3. 한 문장의 음절 수가 40음을 넘으면 자연스러운 쉼표(,)나 접속어(그리고·하지만 등) 지점에서 분할하십시오.
4. 이 텍스트에 욕설이 포함되어 있는지 판단합니다.
   - 욕설 검출은 반드시 ‘어절 단위’로만 수행합니다.  
     (예: “시발”은 필터링하나 “시발점”·“시발역” 등 다른 어절 전체는 필터링하지 않음)
5. **최종 출력은 다음 JSON 형식의 문자열로만 출력하십시오:**
    {
     "safe": [True/False],
     "Text": ["문장1", "문장2", ...]
    }
   "safe" 키의 값은 텍스트가 안전하면 True, 그렇지 않으면 False입니다.
   "Text" 키의 값은 규칙에 따라 변환되고 분할된 문장들의 리스트입니다. 만약 텍스트가 안전하지 않아 "safe"가 False라면, "Text"는 빈 리스트([])여야 합니다.

예시 1. 욕설로 간주될 언어도 없고 숫자가 있는 텍스트  
입력: “오늘 2025년 12월 31일에 100명이 행사에 참여했습니다.”  
출력: {
    "safe":true,
    "text":["오늘 이천이십오년 십이월 삼십일일에","백명이 행사에 참여했습니다."]
    }

예시 2. 욕설 어절이 없지만 어근이 포함된 다른 어절  
입력: “이것은 우리에게 새로운 시발점이 될 것이다.”  
출력: {
    "safe":true,
    "text":["이것은 우리에게 새로운 시발점이 될 것이다."]
    }

예시 3. 욕설 어절이 포함된 경우  
입력: “그가 순간 분노하여 시발을 외쳤다.”  
출력: {
    "safe":false,
    "text":[]
    }

지금부터 입력된 텍스트를 위 규칙에 따라 처리해 주십시오.
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