---
layout: default
title: "COVOS"
nav_order: 1
description: "캡스톤디자인 2025"
permalink: /
---
## 🎙️ COVOS - AI 보이스팩 거래 및 활용 플랫폼

<br>

<p align="center">
  <a href="https://capstone-2025-09-zeta.vercel.app" target="_blank">
    <img src="assets/logo.svg" alt="COVOS Logo" width="100%"/>
  </a>
  <br/>
  <sub>이미지를 클릭하면 COVOS 플랫폼으로 이동합니다</sub>
</p>

---

## 🔗 목차

<br>

1. [🚀 프로젝트 소개](#project-intro)
2. [💡 핵심 기능](#core-features)  
   a. [🗣️ AI 보이스팩 생성](#feature-voicepack)  
   b. [🛍️ 보이스팩 마켓플레이스](#feature-market)  
   c. [🎮 보이스팩 플레이그라운드](#feature-playground)
3. [🎥 소개 영상](#intro-video)
4. [👥 팀 소개](#team)
5. [📐 시스템 구조도](#architechture)
6. [🧠 기술 스택](#tech-stack)
7. [🛠️ 개발 환경 설정법](#setup)  
8. [📂 폴더 구조](#folder-structure)

---

## 🚀 프로젝트 소개
{: #project-intro }

<br>

<p align="center">
  <img src="assets/intro.png" width="100%" alt="COVOS intro">
</p>

### 이제 목소리는 **표현**을 넘어 **자산**이 됩니다.  

- COVOS는 개인이 자신의 목소리를 AI로 학습시켜 보이스팩을 만들고 이를 사고팔 수 있는 플랫폼입니다.  
- 최신 제로샷 음성합성 기술을 활용해 단 몇 초의 음성만으로도 고품질 AI 목소리를 생성할 수 있습니다.  
- 자신의 AI 목소리를 보유하고, 콘텐츠에 활용하며, 나아가 새로운 디지털 자산 시장의 일원이 될 수 있도록 하는 것이 우리의 목표입니다.

---

## 💡 핵심 기능
{: #core-features }

<br>

### 🗣️ AI 보이스팩 생성
{: #feature-voicepack }

- 사용자가 음성을 업로드하면 AI가 해당 음색·억양을 학습하여 보이스팩을 생성합니다.  

### 🛍️ 보이스팩 마켓플레이스
{: #feature-market }

- 생성한 보이스팩을 크레딧 기반으로 자유롭게 판매할 수 있고, 또한 다른 사람들의 보이스팩을 구매할 수 있습니다.  

### 🎮 보이스팩 플레이그라운드
{: #feature-playground }

- 구매한 보이스팩은 플레이그라운드에서 다양한 방식으로 활용할 수 있습니다.   
  - **베이직 보이스**: 텍스트를 보이스팩 목소리로 읽어주는 TTS 기능입니다.
  - **AI 리포터**: 여러 카테고리의 뉴스를 통합하여, 설정한 보이스와 말투로 음성 리포트를 제공하는 기능입니다.
  - **오늘의 명언**: 감정과 지역을 입력하면 AI가 명언을 생성하고 보이스팩 목소리로 읽어주는 기능입니다.
  - **리멤버 보이스**: 업로드한 영상에서 목소리를 추출하여 나만의 AI 보이스팩을 체험할 수 있습니다.

---

## 🎥 소개 영상
{: #intro-video }

<br>

<p align="center">
  <iframe width="560" height="315" src="https://www.youtube.com/embed/HD5T_Gf53l4?si=ruSDNCb43hbPLMfl" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>
</p>

(예정)

---

## 👥 팀 소개
{: #team }

<br>

<p align="center">
  <img src="assets/people.png" width="100%" alt="COVOS people">
</p>

---

## 📐 시스템 구조도
{: #architechture }

<br>

<p align="center">
  <img src="assets/architecture.png" width="100%" alt="COVOS Architecture">
</p>

---

## 🧠 기술 스택
{: #tech-stack }

<br>

| 영역              | 기술 및 도구                             |
|------------------|------------------------------------------|
| AI/ML            | Zonos: Zero-shot Voice Cloning, PyTorch, HuggingFace |
| Backend          | Spring Boot, Kotlin, MySQL                |
| Frontend         | React, JavaScript, Tailwind                         |
| 인프라           | AWS (EC2, RDS, S3, Lambda, Amazon MQ),  GCP Cloud Run           |
| DevOps & 협업도구 | GitHub, Notion, Slack            |

---

## 🛠️ 개발 환경 설정법
{: #setup }

<br>

### 1. 프로젝트 클론

```bash
git clone https://github.com/kookmin-sw/capstone-2025-09.git
cd capstone-2025-09
```

<br>

### 2. 백엔드 개발 환경 설정 (Spring Boot, Kotlin)

1. **필수 소프트웨어**
   - JDK 17 이상
   - Gradle 7.x 이상 (권장: Wrapper 사용)
   - MySQL (로컬 개발 시)
   - Docker (선택, DB 등 컨테이너 실행용)

2. **설정 파일 준비**
   - `backend/src/main/resources/application.yaml-example` 파일을 복사해 `application.yaml`로 이름 변경 후, DB 및 AWS 등 환경 변수 입력

   ```bash
   cp backend/src/main/resources/application.yaml-example backend/src/main/resources/application.yaml
   # application.yaml 파일을 열어 DB, AWS, OPENAI 등 키를 입력
   ```

3. **서버 실행**
   ```bash
   cd backend
   ./gradlew bootRun
   ```

4. **테스트 실행**
   ```bash
   ./gradlew test
   ```

<br>

### 3. 프론트엔드 개발 환경 설정 (React)

1. **필수 소프트웨어**
   - Node.js 18.x 이상
   - npm

2. **패키지 설치**
   ```bash
   cd ../frontend
   npm install
   ```

3. **환경 변수 파일(.env) 작성**
   - `.env.example` 파일을 참고해 `.env` 파일을 생성하고, API 서버 주소 등 환경 변수 입력

   ```bash
   cp .env.example .env
   # .env 파일을 열어 필요한 값 입력
   ```

4. **개발 서버 실행**
   ```bash
   npm start
   ```

<br>

### 4. AI 모델 및 추론 서버 개발 환경 설정 (Python, FastAPI, Docker)

1. **필수 소프트웨어**
   - Python 3.10 이상
   - Docker
   - (로컬 GPU 사용 시) NVIDIA 드라이버, CUDA Toolkit, cuDNN (버전은 모델 호환성에 따라 다름)

2. **환경 변수 파일(.env) 작성**
   - 프로젝트 루트에 `.env.example` 파일을 참고하여 `.env` 파일을 생성하고, 필요한 API 키 및 AWS 설정값 입력
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
   - `AI/.env` 파일도 동일하게 생성 및 설정합니다. (Cloud Run 배포 시에는 Secret Manager 사용)


3. **Docker 이미지 빌드 및 실행**
   ```bash
   cd ../AI
   # Docker 이미지 빌드
   docker build -t zonos-tts .

   # Docker 컨테이너 실행 (환경 변수 파일 사용)
   # 로컬 GPU를 사용하려면 Docker 실행 명령어에 --gpus all 옵션 등을 추가해야 할 수 있습니다.
   # (예: docker run --gpus all -p 8080:8080 --env-file .env zonos-tts)
   docker run -p 8080:8080 --env-file .env zonos-tts
   ```
   - 서버 실행 후 `http://localhost:8080/docs`에서 API 문서를 확인할 수 있습니다.
   - **참고**: AI 모델은 GPU 환경(예: NVIDIA RTX 3070 이상)에서의 실행을 권장합니다. GPU 없이 실행 시 성능 문제가 발생할 수 있습니다.

---

## 📂 폴더 구조
{: #folder-structure }

<br>

```bash
capstone-2025-09/
├── AI/                       
│   ├── config/
│   │   ├── sample_texts.json              # 음성 합성 테스트용 샘플 텍스트
│   │   └── settings.py                    # 애플리케이션 설정 및 환경 변수 관리
│   ├── utils/                             
│   │   ├── sqs_handler.py                 # AWS SQS 메시지 큐 처리
│   │   ├── storage_manager.py             # AWS S3 스토리지 관리
│   │   ├── synthesis_handler.py           # 음성 합성 및 AI 리포터 요청 처리
│   │   ├── text_converter.py              # 텍스트 변환 및 전처리
│   │   ├── voice_registration_handler.py  # 화자 등록 및 보이스팩 생성
│   │   └── voice_synthesizer.py           # 음성 합성 핵심 로직
│   ├── zonos/                             # 제로샷 음성 복제 모델 
│   ├── .dockerignore        
│   ├── .python-version
│   ├── Dockerfile          
│   ├── main.py                            # FastAPI 기반 메인 서버 애플리케이션
│   ├── pyproject.toml      
│   └── uv.lock             
├── backend/
│   ├── build.gradle.kts
│   ├── Dockerfile
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/
│   │   │   │   └── kr/ac/kookmin/cs/capstone/voicepack_platform/
│   │   │   │       ├── aiAssistant/         # AI 리포터 관련 백엔드 로직
│   │   │   │       ├── common/              # 공통 유틸리티 및 상수
│   │   │   │       ├── config/              # 환경설정 및 설정 클래스
│   │   │   │       ├── credit/              # 크레딧(포인트) 관리
│   │   │   │       ├── notification/        # 알림 기능
│   │   │   │       ├── quote/               # 견적/명언 등 부가 기능
│   │   │   │       ├── sale/                # 보이스팩 판매/구매 관리
│   │   │   │       ├── user/                # 사용자 관리 및 인증
│   │   │   │       ├── video2voicepack/     # 영상 기반 보이스팩 생성
│   │   │   │       ├── voicepack/           # 보이스팩 생성/관리/합성
│   │   │   │       └── VoicepackPlatformApplication.kt
│   │   │   └── resources/
│   │   │       ├── application.yaml
│   │   │       └── application.yaml-example
│   │   └── test/
│   └── gradle/
├── frontend/
│   ├── node_modules/                      # 프로젝트 의존성 모듈
│   ├── public/                            # 정적 파일 (index.html, favicon 등)
│   │   └── index.html
│   ├── src/
│   │   ├── api/                           # API 요청 함수
│   │   │   ├── getVoicepacks.js
│   │   │   └── user.js
│   │   ├── assets/                        # 이미지, 폰트 등 정적 에셋
│   │   │   ├── blur-gray.svg
│   │   │   ├── blur100.svg
│   │   │   ├── blur50.svg
│   │   │   ├── cosmic-fusion.jpeg
│   │   │   ├── deep-ocean.jpeg
│   │   │   ├── hollogram.jpeg
│   │   │   ├── imaginarium.jpeg
│   │   │   ├── iridescent.jpeg
│   │   │   ├── landing-basicVoice.png
│   │   │   ├── landing-quote.png
│   │   │   ├── landing-rememberVoice.png
│   │   │   ├── landing-reporter.png
│   │   │   ├── landing-store.png
│   │   │   ├── logo-new.svg
│   │   │   ├── logo-white.svg
│   │   │   ├── logo.png
│   │   │   ├── logo.svg
│   │   │   └── lp.svg
│   │   │   └── sirens.jpeg
│   │   ├── components/                    # UI 컴포넌트
│   │   │   ├── common/                    # 공통으로 사용되는 컴포넌트
│   │   │   │   ├── AudioListPlayer.js
│   │   │   │   ├── AudioPlayer.js
│   │   │   │   ├── GradientButton.js
│   │   │   │   ├── LandingpageVoicepack.js
│   │   │   │   ├── PageContainer.js
│   │   │   │   ├── SelectBox.js
│   │   │   │   ├── VoicePack.js
│   │   │   │   └── VoicePackModal.js
│   │   │   ├── layout/                    # 페이지 레이아웃 컴포넌트
│   │   │   │   ├── Header.js
│   │   │   │   ├── Layout.js
│   │   │   │   └── Sidebar.js
│   │   │   ├── mypage/                    # 마이페이지 관련 컴포넌트
│   │   │   │   ├── CreditTransactionTabs.js
│   │   │   │   └── Section.js
│   │   │   └── visual/                    # 시각적 효과 관련 컴포넌트
│   │   │       ├── BlurBackground.js
│   │   │       ├── WaveAninmation.js
│   │   │       └── WaveSphere.js
│   │   ├── hooks/                         # 커스텀 React Hooks
│   │   │   ├── useAiAssistant.js
│   │   │   ├── useAssistantSetup.js
│   │   │   ├── useBuyVoicepack.js
│   │   │   ├── useSignin.js
│   │   │   ├── useUserInfo.js
│   │   │   ├── useVideoToVoicepack.js
│   │   │   ├── useVoicepackConvert.js
│   │   │   ├── useVoicepackDelete.js
│   │   │   ├── useVoicepackDetail.js
│   │   │   ├── useVoicepackQuote.js
│   │   │   ├── useVoicepackSynthesis.js
│   │   │   └── useVoicepackUsage.js
│   │   ├── pages/                         # 라우팅될 페이지 컴포넌트
│   │   │   ├── ai-assistant/              # AI 리포터 관련 페이지
│   │   │   │   ├── AssistantReadyScreen.js
│   │   │   │   ├── AssistantSetup.js
│   │   │   │   ├── ScriptPlayer.js
│   │   │   │   └── index.js
│   │   │   ├── mypage/                    # 마이페이지 관련 페이지
│   │   │   │   ├── MyDashboard.js
│   │   │   │   ├── MyPayments.js
│   │   │   │   ├── MyRevenue.js
│   │   │   │   ├── MyVoicepacks.js
│   │   │   │   └── index.js
│   │   │   ├── BasicVoice.js              # 기본 보이스팩 페이지
│   │   │   ├── JoinAgreement.js           # 회원가입 동의 페이지
│   │   │   ├── Landing.js                 # 랜딩 페이지
│   │   │   ├── Quote.js                   # 오늘의 명언 페이지
│   │   │   ├── RememberVoice.js           # 리멤버 보이스 페이지
│   │   │   ├── SignIn.js                  # 로그인 페이지
│   │   │   ├── SignUp.js                  # 회원가입 페이지
│   │   │   ├── VoiceCreate.js             # 보이스팩 생성 페이지
│   │   │   └── VoiceStore.js              # 보이스팩 스토어 페이지
│   │   ├── utils/                         # 유틸리티 함수
│   │   │   ├── axiosInstance.js
│   │   │   ├── extractAudioFromVideo.js
│   │   │   ├── s3Uploader.js
│   │   │   └── userStore.js
│   │   ├── App.css                        # App 컴포넌트 스타일
│   │   ├── App.js                         # 메인 애플리케이션 컴포넌트
│   │   ├── index.css                      # 전역 스타일
│   │   └── index.js                       # 애플리케이션 진입점
│   ├── .DS_Store
│   ├── .gitignore
│   ├── .prettierrc
│   ├── README.md
│   ├── package-lock.json
│   ├── package.json
│   ├── postcss.config.js
│   ├── tailwind.config.js
│   └── vercel.json
├── lambda/
│   ├── ai_assistant_lambda.py             # AI 리포터 기능 AWS Lambda 함수
│   ├── packages_layer.zip                 # Lambda Layer 패키지
│   ├── register_speaker_lambda.py         # 화자 등록 AWS Lambda 함수
│   ├── synthesize_lambda.py               # 음성 합성 AWS Lambda 함수
│   ├── bbcnews/                           # BBC 뉴스 크롤링 Lambda
│   │   └── lambda_function.py
│   ├── economy/                           # 경제 뉴스 크롤링 Lambda
│   │   └── lambda_function.py
│   ├── googlenews/                        # 구글 뉴스 크롤링 Lambda
│   │   └── lambda_function.py
│   ├── itnews/                            # IT 뉴스 크롤링 Lambda
│   │   └── lambda_function.py
│   └── sports/                            # 스포츠 뉴스 크롤링 Lambda
│       └── lambda_function.py
├── .gitignore
└── .env
```
