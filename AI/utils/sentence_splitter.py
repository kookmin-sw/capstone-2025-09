import re

def sentences_split(text: str) -> list[str]:
    """문장 분리 함수"""
    pattern = r'(?<=[.!?])\s+'
    sentences = re.split(pattern, text)
    sentences = [sentence.strip() for sentence in sentences if sentence.strip()]
    return sentences