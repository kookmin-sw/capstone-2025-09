---
title: "AI 메뉴얼입니다."
layout: default
nav_order: 5
parent: 메뉴얼
---
# AI 모델 및 추론 서버 개발 가이드

## 소개
ZONOS는 Zyphra에서 개발한 오픈소스 텍스트-음성 변환(TTS) 솔루션입니다. 사용자 음성 등록부터 감정을 포함한 음성 합성까지 다양한 기능을 제공합니다.

## 주요 기능
- **화자 등록**: 사용자 음성을 등록하여 개인화된 음성팩 생성
- **음성 합성**: 텍스트를 사용자 음성으로 변환
- **AI 비서**: 카테고리와 작성 스타일에 맞는 응답 생성 및 음성 합성
- **감정 표현**: 음성에 다양한 감정 부여 가능

## 기술 스택
- **언어**: Python 3.10
- **프레임워크**: FastAPI
- **AI/ML**: PyTorch, Transformers
- **인프라**: Docker, AWS(S3, SQS)
- **음성 처리**: torchaudio, SudachiPy, Phonemizer

## 디렉토리 구조
```
AI/
├── zonos/                     # 핵심 모델 및 알고리즘 
│   ├── backbone/              # 백본 모델 구성요소
│   └── ...                    # 기타 모델 관련 파일
├── utils/                     # 유틸리티 함수
│   ├── voice_synthesizer.py   # 음성 합성 핵심 로직
│   ├── storage_manager.py     # S3 스토리지 관리 
│   ├── sqs_handler.py         # AWS SQS 메시지 처리
│   ├── synthesis_handler.py   # 음성 합성 및 AI 비서 요청 처리
│   ├── text_converter.py      # 텍스트 변환 및 전처리
│   └── voice_registration_handler.py # 화자 등록 및 음성팩 생성
├── config/                    # 설정 파일 디렉토리
├── main.py                    # API 서버 진입점
├── Dockerfile                 # 도커 이미지 정의
└── pyproject.toml             # 프로젝트 의존성 정의
```

## API 엔드포인트
1. **화자 등록 (/register_speaker)**
   - 음성 파일 업로드 및 화자 ID 등록
   - 음성팩 생성 및 저장

2. **음성 합성 (/synthesize)**
   - 텍스트를 사용자 음성으로 변환
   - 감정 조절 가능
   - S3에 결과 저장 및 SQS로 알림

3. **AI 비서 (/assistant)**
   - 프롬프트, 카테고리, 작성 스타일 기반 응답 생성
   - 생성된 응답을 음성으로 합성
   - S3에 결과 저장 및 SQS로 알림

4. **상태 확인 (/health)**
   - 서비스 상태 모니터링

## 설치 및 실행
```bash
# 도커 이미지 빌드
docker build -t zonos-tts .

# 도커 컨테이너 실행
docker run -p 8080:8080 zonos-tts
```

## 개발자 환경 설정
```bash
# Python 버전 설정
python -m venv venv
source venv/bin/activate  # 윈도우: venv\Scripts\activate

# 의존성 설치
pip install uv
uv pip install -e .

# 서버 실행
uvicorn main:app --host 0.0.0.0 --port 8080
```