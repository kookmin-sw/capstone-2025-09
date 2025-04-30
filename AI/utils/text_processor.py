import re

def sentences_split(text: str) -> list[str]:
    """문장 분리 함수"""
    pattern = r'(?<=[.!?])\s+'
    sentences = re.split(pattern, text)
    sentences = [sentence.strip() for sentence in sentences if sentence.strip()]
    return sentences

# 1) 숫자 → 한글 발음 ----------------------------------------------------------
_digits = ['', '일', '이', '삼', '사', '오', '육', '칠', '팔', '구']
_small = ['', '십', '백', '천']
_big = ['', '만', '억', '조', '경']          # 10⁴ 단위

def _four_digits_to_kor(n: int) -> str:
    """0 ≤ n < 10000 범위 숫자를 한글(만-단위 없이)로 변환"""
    parts = []
    for pos in range(4):                 # 1-자리 → 천-자리
        d, n = n % 10, n // 10
        if d == 0:
            continue
        if pos == 0:                     # 1의 자리
            parts.append(_digits[d])
        else:                            # 십·백·천
            parts.append((_small[pos] if d == 1 else _digits[d] + _small[pos]))
    return ''.join(reversed(parts))

def _number_to_kor(num: int) -> str:
    if num == 0:
        return '영'
    groups, i, out = [], 0, []
    while num:
        groups.append(num % 10_000)      # 만-단위로 끊기
        num //= 10_000
    for i, g in enumerate(groups):
        if g == 0:
            continue
        word = _four_digits_to_kor(g)
        # 맨 앞이 '1만', '1억' … 일 때 '일' 생략
        if word == '일' and _big[i] and i == len(groups) - 1:
            word = ''
        out.insert(0, word + _big[i])
    return ''.join(out)

# 2) 영문 대문자 약어 → 한글 발음 ---------------------------------------------
_letter = {
    'A':'에이','B':'비','C':'씨','D':'디','E':'이','F':'에프','G':'지','H':'에이치',
    'I':'아이','J':'제이','K':'케이','L':'엘','M':'엠','N':'엔','O':'오','P':'피',
    'Q':'큐','R':'알','S':'에스','T':'티','U':'유','V':'브이','W':'더블유','X':'엑스',
    'Y':'와이','Z':'지',
}

def _abbr_to_kor(m: re.Match) -> str:
    return ''.join(_letter[c] for c in m.group())

# 3) 서수(목차 번호) 변환 -------------------------------------------------------
def _ordinal(num: int) -> str:
    return ('일번' if num == 1 else '이번' if num == 2 else _number_to_kor(num) + '번')

def convert_text(text: str) -> str:
    # (1) 줄머리 번호: "1." → "일번."
    text = re.sub(r'^(\s*)(\d+)\.', lambda m: f"{m.group(1)}{_ordinal(int(m.group(2)))}.", text,
                  flags=re.MULTILINE)

    # (2) 약어: 2글자 이상 연속 대문자 → 한글
    text = re.sub(r'\b[A-Z]{2,}\b', _abbr_to_kor, text)

    # (3) 숫자: 글자와 붙어 있지 않은 정수(쉼표 허용) → 한글
    def _num_repl(m):
        return _number_to_kor(int(m.group().replace(',', '')))
    text = re.sub(r'(?<![A-Za-z])\d[\d,]*', _num_repl, text)

    return text
