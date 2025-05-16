---
title: "AI 모델 및 추론 서버 개발 가이드"
layout: default
nav_order: 5
parent: 매뉴얼
has_toc: false
---
# AI 모델 및 추론 서버 개발 가이드

{: .no_toc }

---

## 목차

{: .no_toc }

- TOC
{:toc}

---

## 소개
ZONOS는 Zyphra에서 개발한 오픈소스 텍스트-음성 변환(TTS) 솔루션입니다. 사용자 음성 등록부터 감정을 포함한 음성 합성까지 다양한 기능을 제공합니다.

ZONOS GitHub 레포지토리: [https://github.com/Zyphra/Zonos](https://github.com/Zyphra/Zonos)

---

## 주요 기능
- **화자 등록**: 사용자 음성을 등록하여 개인화된 음성팩 생성
- **음성 합성**: 텍스트를 사용자 음성으로 변환
- **AI 비서**: 카테고리와 작성 스타일에 맞는 응답 생성 및 음성 합성
- **감정 표현**: 음성에 다양한 감정 부여 가능

---

## 기술 스택
- **언어**: Python 3.10
- **프레임워크**: FastAPI
- **AI/ML**: PyTorch, Transformers
- **인프라**: Docker, AWS(S3, SQS)
- **음성 처리**: torchaudio, SudachiPy, Phonemizer

---

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

---

## API 엔드포인트

**화자 등록 (/register_speaker)**
- 음성 파일 업로드 및 화자 ID 등록
- 음성팩 생성 및 저장
- 샘플 보이스 합성

**음성 합성 (/synthesize)**
- 텍스트를 사용자 음성으로 변환
- 감정 조절 가능
- S3에 결과 저장 및 SQS로 알림

**AI 비서 (/assistant)**
- 프롬프트, 카테고리, 작성 스타일 기반 응답 생성
- 생성된 응답을 음성으로 합성
- S3에 결과 저장 및 SQS로 알림

**상태 확인 (/health)**
- 서비스 상태 모니터링

---

## 환경 변수 설정
프로젝트 루트에 `.env` 파일을 다음과 같이 생성합니다:

```
# OpenAI API 키
OPENAI_API_KEY=sk-your-api-key-here

# AWS S3 설정
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
AWS_DEFAULT_REGION=ap-northeast-2
AWS_BUCKET_NAME=your-bucket-name

# AWS SQS 설정
AWS_SQS_REGISTER_QUEUE_URL=https://sqs.ap-northeast-2.amazonaws.com/your-account-id/your-queue-name
AWS_SQS_SYNTHESIZE_QUEUE_URL=https://sqs.ap-northeast-2.amazonaws.com/your-account-id/your-queue-name
AWS_SQS_ASSISTANT_QUEUE_URL=https://sqs.ap-northeast-2.amazonaws.com/your-account-id/your-queue-name
```

---

## 설치 및 실행

```bash
# 도커 이미지 빌드
docker build -t zonos-tts .

# 도커 컨테이너 실행 (환경 변수 포함)
docker run -p 8080:8080 --env-file .env zonos-tts
```

---

## Cloud Run 배포
ZONOS 서비스는 Google Cloud Run을 통해 배포됩니다. 모델의 성능을 위해 GPU를 사용하는 배포 과정은 다음과 같습니다:

### GPU 사용을 위한 사전 준비

```bash
# GPU 할당량 확인
gcloud compute quotas list --project=[프로젝트-ID] | grep -i gpu
```

해당 링크에서 GPU 할당량 증가 요청을 승인받아야 합니다:
[https://console.cloud.google.com/iam-admin/quotas](https://console.cloud.google.com/iam-admin/quotas)

### 컨테이너 이미지 빌드 및 푸시

```bash
# 이미지 빌드
docker build -t gcr.io/[프로젝트-ID]/zonos-tts-gpu:latest .

# Container Registry에 이미지 푸시
docker push gcr.io/[프로젝트-ID]/zonos-tts-gpu:latest
```

### Secret Manager에 환경 변수 등록

Cloud Run은 .env 파일을 직접 지원하지 않기 때문에, Google Secret Manager를 사용하여 민감한 환경 변수를 저장합니다. 다음 명령어로 필요한 비밀들을 등록합니다:

```bash
# OpenAI API 키 등록
gcloud secrets create openai-api-key --data-file=- <<< "sk-your-api-key-here"

# AWS 자격 증명 등록
gcloud secrets create aws-access-key --data-file=- <<< "your-access-key"
gcloud secrets create aws-secret-key --data-file=- <<< "your-secret-key"

# S3 버킷 및 SQS URL 등록
gcloud secrets create s3-bucket-name --data-file=- <<< "your-bucket-name"
gcloud secrets create sqs-url --data-file=- <<< "https://sqs.ap-northeast-2.amazonaws.com/your-account-id/your-queue-name"
```

### Cloud Run 서비스 배포

등록한 Secret Manager의 비밀들을 환경 변수로 사용하여 GPU가 지원되는 Cloud Run 서비스를 배포합니다:

```bash
gcloud run deploy zonos-tts \
  --image gcr.io/[프로젝트-ID]/zonos-tts-gpu:latest \
  --platform managed \
  --region us-central1 \
  --memory 4Gi \
  --cpu 4 \
  --gpu 1 \
  --gpu-type=nvidia-tesla-t4 \
  --allow-unauthenticated \
  --set-secrets="OPENAI_API_KEY=openai-api-key:latest,AWS_ACCESS_KEY_ID=aws-access-key:latest,AWS_SECRET_ACCESS_KEY=aws-secret-key:latest,S3_BUCKET_NAME=s3-bucket-name:latest,SQS_URL=sqs-url:latest"
```

### GPU 활용 확인 및 모니터링
- Nvidia-SMI 명령어를 통한 GPU 사용량 확인
- Cloud Monitoring을 통한 GPU 사용량 모니터링
- Cloud Logging을 통한 로그 확인

### 비용 최적화
- 최소 인스턴스 설정 (--min-instances)
- 자동 스케일링 설정 (--max-instances)
- GPU 사용량에 따른 비용 모니터링

---

## FAQ

### Q: API 문서는 어떻게 확인할 수 있나요?
A: 서버 실행 후 `http://localhost:8080/docs`에서 API 문서를 확인할 수 있습니다.


### Q: 샘플 보이스로 만들어지는 텍스트를 변경할 수 있나요?
A: `config/sample_texts.json` 파일에 리스트 형식으로 텍스트를 추가해주세요.


### Q: 로컬에서 GPU를 사용하지 않고 실행할 수 있나요?
A: 현재는 RTX 3070에서 실행됨을 확인했습니다. GPU가 없는 환경에서는 실행을 하지 않는 것이 추천됩니다.


### Q: 사용할 수 있는 감정을 추가할 수 있나요? 감정을 추가하기 위해서는 어떻게 하면 되나요?
A: `AI/utils/voice_synthesizer.py`의 EMOTION_PROFILE 리스트를 수정하는 식으로 진행할 수 있습니다. 


총 8개의 세부 감정 파라미터를 조정할 수 있으며 각 파라미터는 순서대로 Happiness, Sadness, Disgust, Fear, Surprise, Anger, Other 및 Neutral입니다.


### Q: Cloud Run 배포를 GUI로 진행하는 방법이 알고싶어요!
A: [노션 페이지](https://sprout-angle-4de.notion.site/Cloud-run-with-Docker-1a0599a329d58049890dda8ff53f016c?pvs=4) 를 참고하시면 됩니다.


### Q: Cloud Run을 통해 배포를 진행했는데 서버가 켜지는 데에 시간이 오래 걸려요. 자동으로 서버가 닫히기도 하는데 정상인가요?
A: 정상입니다. 

Cloud Run은 Auto Scaling을 통해 일정 시간 동안 트래픽이 없으면 자동으로 컨테이너를 종료하고, 다시 요청이 들어오면 종료된 컨테이너를 재실행하는 데에 초기화하는 과정이 필요합니다.


이 초기화하는 과정 및 자동 종료가 불편하다면, 최소 인스턴스 수를 1로 설정하여 서버가 계속 켜져있게 하는 방법도 존재합니다.