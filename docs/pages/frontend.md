# 🎙️ COVOS (코보스) 프론트엔드

**COVOS**는 사용자의 목소리를 AI로 학습해 **보이스팩을 생성하고 거래 및 활용**할 수 있는 플랫폼입니다.  
본 리포지토리는 해당 플랫폼의 **프론트엔드 애플리케이션** 소스코드를 포함합니다.

---

## 📚 목차

- [🧩 주요 기능](#-주요-기능)
- [🛠 기술 스택](#-기술-스택)
- [⚙️ 사전 준비 사항](#-사전-준비-사항)
- [🚀 시작하기](#-시작하기)
- [🗂️ 프로젝트 구조](#️-프로젝트-구조)
- [🔧 프로덕션 빌드](#-프로덕션-빌드)
- [☁️ 배포](#-배포)
- [🔍 린팅 및 포맷팅](#-린팅-및-포맷팅)
- [📖 더 알아보기](#-더-알아보기)

---

## 🧩 주요 기능

### 🔊 보이스팩 생성
- 사용자의 음성을 녹음하고, AI 학습을 통해 고품질 보이스팩 생성
- Zero-shot voice cloning 기술 기반

### 🛒 보이스팩 거래
- 보이스팩 등록 및 마켓플레이스 거래
- 크레딧 기반의 구매 및 환전 시스템 연계

### 🗣️ 보이스팩 활용 기능
- **텍스트 변환(TTS)**: 스크립트를 선택한 보이스로 음성 출력
- **맞춤형 AI 비서**: 뉴스, 명언, 일정 등 카테고리 음성을 자동 제공
- **AI 리포터**: 뉴스 카테고리 선택 → AI 프롬프트 생성 → 선택 보이스로 뉴스 리포트
- **오늘의 명언**: 감정 기반 + 지역(동양/서양/한국) 선택 → 명언 생성 및 낭독
- **리멤버 보이스**: 영상 기반 고인의 음성 복원 및 보이스팩화

### 👤 마이페이지
- 생성한 보이스팩 관리(수정/삭제)
- 구매한 보이스팩 확인 및 재생
- 수입 통계 및 크레딧 충전

### 💬 인터랙션 및 UI/UX
- 반응형 디자인 (Tailwind CSS 기반)
- Sonner 기반 알림 시스템
- Framer Motion으로 부드러운 인터랙션 효과
- Three.js 적용 랜딩페이지 (3D 시각 효과)

---

## 🛠 기술 스택

| 카테고리       | 스택 |
|----------------|------|
| **프레임워크** | React (SPA) |
| **언어**       | JavaScript |
| **상태 관리**  | Zustand |
| **스타일링**   | Tailwind CSS |
| **API 통신**   | Axios (인터셉터 포함) |
| **3D 시각화**  | Three.js |
| **애니메이션** | Framer Motion |
| **배포**       | Vercel |

---

## ⚙️ 사전 준비 사항

- Node.js v18 이상
- npm 또는 yarn
- `.env.local` 환경 변수 파일 설정

---

## 🚀 시작하기

### 1. 저장소 클론

```bash
git clone https://github.com/kookmin-sw/capstone-2025-09.git
cd frontend
```

### 2. 의존성 설치
```bash
npm install
```

### 3. 환경 변수 설정
```env
REACT_APP_BASE_URL=https://vocalab.kro.kr/api/
```

### 4. 프론트 로컬 실행
```bash
npm start
```

- 기본 포트: http://localhost:3000
---

## 🗂️ 프로젝트 구조

```
.
├── public/                    # 정적 파일 및 ffmpeg WASM 리소스
│   └── ffmpeg/                # 브라우저 기반 음성 추출용 wasm 파일
│
├── src/
│   ├── App.js / App.css       # 애플리케이션 루트 및 전역 스타일
│   ├── index.js / index.css   # React 진입점 및 Tailwind 초기화
│
│   ├── api/                   # 사용자 및 보이스팩 관련 API 모듈
│
│   ├── assets/                # 이미지, 배경, 로고 등 리소스
│
│   ├── components/            # 공통 UI 컴포넌트 모음
│   │   ├── common/            # 버튼, 모달 등 범용 UI
│   │   ├── layout/            # Header, Sidebar 등 레이아웃
│   │   ├── visual/            # 배경 효과
│   │   └── mypage/            # 마이페이지 전용 섹션 UI
│
│   ├── hooks/                 # 보이스팩, 로그인, AI 리포터 등 기능별 커스텀 훅
│
│   ├── pages/                 # 각 기능별 라우팅 페이지
│   │   ├── 사용자 인증: SignIn, SignUp, JoinAgreement
│   │   ├── 서비스 기능: VoiceCreate, VoiceStore, BasicVoice, ai-assistant/*, Quote, RememberVoice, mypage/*

│
│   └── utils/                 # axios 설정, S3 업로드, Zustand 스토어 등 유틸
│
├── tailwind.config.js         # Tailwind CSS 구성
├── vercel.json                # Vercel 배포 설정
└── package.json               # 종속성과 스크립트 정의
```

---

## 🔧 프로덕션 빌드

```bash
npm run build
```
---

## ☁️ 배포
Vercel 자동 배포
이 프로젝트는 Vercel 플랫폼을 사용하여 자동 배포됩니다.

기본적으로 main 또는 release/* 브랜치에 푸시되면 자동으로 배포가 진행됩니다.

vercel.json 파일을 통해 redirect, clean URL, rewrite 등의 설정을 정의합니다.

예시: vercel.json
```
{
  "rewrites": [
    { "source": "/(.*)", "destination": "/" }
  ]
}
```

---

## 🔍 린팅 및 포맷팅

사용 도구
- ESLint: React, Tailwind CSS 관련 규칙 적용
- Prettier: 코드 스타일 자동 정리

```bash
npm run lint      # 코드 린트 검사
npm run format    # 코드 자동 포맷팅 (Prettier)
```

---

## 📖 더 알아보기

- [React](https://reactjs.org/)
- [Zustand (상태 관리)](https://github.com/pmndrs/zustand)
- [Tailwind CSS](https://tailwindcss.com/docs)
- [Framer Motion](https://www.framer.com/motion/)
- [Three.js](https://threejs.org/)
- [ffmpeg.wasm](https://github.com/ffmpegwasm/ffmpeg.wasm)
- [Vercel](https://vercel.com/docs)
