import ast
from typing import List

from config.settings import OPENAI_CONFIG
from openai import OpenAI

_API_KEY = OPENAI_CONFIG['api_key']
if not _API_KEY:
    raise EnvironmentError("환경 변수 OPENAI_API_KEY 가 설정되지 않았습니다.")
client = OpenAI(api_key=_API_KEY)

SYSTEM_PROMPT = """
당신은 대한민국 뉴스 원고를 음성 합성용 대본으로 교정하는 전문가입니다.

규칙
1. 모든 아라비아 숫자를 한국어로 완전하게 풀어쓰십시오.
   - 예) 2025년 → 이천이십오년
         8명   → 여덟명
         50%   → 오십퍼센트
2. 숫자 뒤 단위(년·명·% 등)는 그대로 유지하되 숫자만 변환하십시오.
3. 맞춤법·고유명사·어순은 절대로 변경하지 마십시오.
4. 한 문장이 40음절을 넘으면 의미가 자연스러운 쉼표(,)·접속어(그리고·하지만 등) 지점에서
   추가로 문장을 나누어 주십시오.
5. 부가 설명이나 요약 없이 **결과 문장만** 제공하십시오.
6. 마크다운은 사용하지 않습니다.
7. **최종 출력은 ["문장1", "문장2", ...] 형태의 엄격한 파이썬 리스트 리터럴로만 출력**하십시오.
"""

def convert_text(
    text: str,
    model: str = "gpt-4o-mini",
    *,
    temperature: float = 0.0,
) -> List[str]:
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
    -------
    List[str]
        숫자 변환 및 문장 분할이 완료된 문장 리스트.
    """
    completion = client.chat.completions.create(
        model=model,
        messages=[
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": text},
        ],
        temperature=temperature,
    )

    raw_content = completion.choices[0].message.content.strip()
    try:
        # 모델이 반환한 리스트 리터럴을 안전하게 파싱
        sentences: List[str] = ast.literal_eval(raw_content)
        if not isinstance(sentences, list) or not all(isinstance(s, str) for s in sentences):
            raise ValueError
        
    except Exception as exc:  # 파싱 실패
        raise ValueError(
            "모델 응답을 리스트로 파싱하지 못했습니다. "
            f"원본 응답: {raw_content!r}"
        ) from exc

    return sentences