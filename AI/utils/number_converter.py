import re

_digits = ['', '일', '이', '삼', '사', '오', '육', '칠', '팔', '구']
_small  = ['', '십', '백', '천']
_big    = ['', '만', '억', '조', '경']

def _four_digits(n: int) -> str:
    if n == 0:
        return ''
    parts = []
    for pos in range(4):
        n, d = divmod(n, 10)
        if d == 0:
            continue
        if pos == 0:
            parts.append(_digits[d])
        else:
            head = '' if d == 1 else _digits[d]
            parts.append(head + _small[pos])
    return ''.join(reversed(parts))

def _num_to_kor(num: int) -> str:
    if num == 0:
        return '영'
    out, idx = [], 0
    while num:
        num, rem = divmod(num, 10_000)
        if rem:
            chunk = _four_digits(rem)
            if chunk == '일' and idx > 0 and num == 0:
                chunk = ''
            out.insert(0, chunk + _big[idx])
        idx += 1
    return ''.join(out)

_letter = {
    'A':'에이','B':'비','C':'씨','D':'디','E':'이','F':'에프','G':'지','H':'에이치',
    'I':'아이','J':'제이','K':'케이','L':'엘','M':'엠','N':'엔','O':'오','P':'피',
    'Q':'큐','R':'알','S':'에스','T':'티','U':'유','V':'브이','W':'더블유','X':'엑스',
    'Y':'와이','Z':'지',
}
def _abbr(m: re.Match) -> str:
    return ''.join(_letter[c] for c in m.group())

_native = {'1':'한', '2':'두', '3':'세', '4':'네', '5':'다섯',
           '6':'여섯', '7':'일곱', '8':'여덟', '9':'아홉'}
CNT = r'(?:명|개|권|병|마리|대)'

def _native_counter(m: re.Match) -> str:
    return _native[m.group(1)]

def _ordinal(n: int) -> str:
    return '일번' if n == 1 else '이번' if n == 2 else _num_to_kor(n) + '번'

def convert_text(text: str) -> str:
    text = re.sub(r'^(\s*)(\d+)\.',
                  lambda m: f"{m.group(1)}{_ordinal(int(m.group(2)))}.",
                  text, flags=re.M)

    pattern_native = rf'(?<!\d)([1-9])(?=\s*{CNT})'
    text = re.sub(pattern_native, _native_counter, text)

    text = re.sub(r'\b[A-Z]{2,}\b', _abbr, text)

    text = re.sub(r'(?<![A-Za-z])\d[\d,]*',
                  lambda m: _num_to_kor(int(m.group().replace(',', ''))),
                  text)

    return text
