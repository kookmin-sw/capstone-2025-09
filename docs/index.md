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
  <img src="assets/logo.svg" alt="COVOS Logo" width="100%"/>
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

---

## 🎥 소개 영상
{: #intro-video }

<br>

<p align="center">
  <iframe width="560" height="315" src="https://www.youtube.com/embed/HLSFtyEcC9E?si=JR25zfAydfEGVCk-" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>
</p>

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
| Backend          | Kotlin, Spring Boot, MySQL                |
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
   - npm 또는 yarn

2. **패키지 설치**
   ```bash
   cd ../frontend
   npm install
   # 또는
   yarn install
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
   # 또는
   yarn start
   ```


---

## 📂 폴더 구조
{: #folder-structure }

<br>

```bash
capstone-2025-09/
├── backend/
│   ├── build.gradle.kts
│   ├── Dockerfile
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/
│   │   │   │   └── kr/ac/kookmin/cs/capstone/voicepack_platform/
│   │   │   │       ├── aiAssistant/
│   │   │   │       ├── common/
│   │   │   │       ├── config/
│   │   │   │       ├── credit/
│   │   │   │       ├── notification/
│   │   │   │       ├── quote/
│   │   │   │       ├── sale/
│   │   │   │       ├── user/
│   │   │   │       ├── video2voicepack/
│   │   │   │       ├── voicepack/
│   │   │   │       └── VoicepackPlatformApplication.kt
│   │   │   └── resources/
│   │   │       ├── application.yaml
│   │   │       └── application.yaml-example
│   │   └── test/
│   └── gradle/
├── frontend/
│   ├── package.json
│   ├── public/
│   ├── src/
│   │   ├── api/
│   │   ├── assets/
│   │   ├── components/
│   │   │   ├── common/
│   │   │   ├── layout/
│   │   │   └── visual/
│   │   ├── data/
│   │   ├── hooks/
│   │   ├── pages/
│   │   │   ├── ai-assistant/
│   │   │   │   ├── AssistantReadyScreen.js
│   │   │   │   ├── AssistantSetup.js
│   │   │   │   ├── ScriptPlayer.js
│   │   │   │   └── index.js
│   │   │   ├── BasicVoice.js
│   │   │   ├── Landing.js
│   │   │   ├── MyPage.js
│   │   │   ├── SignIn.js
│   │   │   ├── SignUp.js
│   │   │   ├── VoiceCreate.js
│   │   │   └── VoiceStore.js
│   │   ├── utils/
│   │   ├── App.js
│   │   └── index.js
│   └── node_modules/
├── .gitignore
├── .env
```
