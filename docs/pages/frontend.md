---
title: "프론트엔드 개발 가이드"
layout: default
nav_order: 3
parent: 가이드
has_toc: false
---
# **프론트엔드 개발 가이드**

{: .no_toc }

## 목차

{: .no_toc }

- TOC
{:toc}

---

## 주요 기능

### 보이스팩 생성
- 사용자의 음성을 녹음하고, AI 학습을 통해 고품질 보이스팩 생성
- Zero-shot voice cloning 기술 기반

### 보이스팩 거래
- 보이스팩 등록 및 마켓플레이스 거래
- 크레딧 기반의 구매 및 환전 시스템 연계

###  보이스팩 활용 기능
- **텍스트 변환(TTS)**: 스크립트를 선택한 보이스로 음성 출력
- **맞춤형 AI 비서**: 뉴스, 명언, 일정 등 카테고리 음성을 자동 제공
- **AI 리포터**: 뉴스 카테고리 선택 → AI 프롬프트 생성 → 선택 보이스로 뉴스 리포트
- **오늘의 명언**: 감정 기반 + 지역(동양/서양/한국) 선택 → 명언 생성 및 낭독
- **리멤버 보이스**: 영상 기반 고인의 음성 복원 및 보이스팩화

### 마이페이지
- 생성한 보이스팩 관리(수정/삭제)
- 구매한 보이스팩 확인 및 재생
- 수입 통계 및 크레딧 충전

### 인터랙션 및 UI/UX
- 반응형 디자인 (Tailwind CSS 기반)
- Sonner 기반 알림 시스템
- Framer Motion으로 부드러운 인터랙션 효과
- Three.js 적용 랜딩페이지 (3D 시각 효과)

---

## 기술 스택

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

## 사전 준비 사항

- Node.js v18 이상
- npm 또는 yarn
- `.env.local` 환경 변수 파일 설정

---

## 시작하기

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
```
REACT_APP_BASE_URL=https://vocalab.kro.kr/api/
```

### 4. 프론트 로컬 실행
```bash
npm start
```

- 기본 포트: http://localhost:3000


---

## 프로젝트 구조

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

## 프로덕션 빌드

```bash
npm run build
```

---

## 배포 
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

## 테스트 및 배포 자동화 (CI/CD)

- Vercel Preview Deploy
  PR 생성 시마다 Vercel에서 미리보기 배포 URL을 자동 생성하여 기능 구현 결과를 실시간으로 확인할 수 있도록 설정하였습니다.

- 브랜치 전략
    - `main`: 안정적인 운영을 위한 릴리즈 브랜치
    - `develop`: 기능 개발 브랜치를 통합한 메인 개발 브랜치
    - `feature/*`: 각 기능 단위로 분기한 개발 브랜치
    - Git Flow 기반 브랜치 전략을 통해 기능별 협업 및 병합 흐름을 명확히 관리하였습니다.

- GitHub Actions
    - 커밋/PR 발생 시 자동으로 린트 및 포맷 검사(`npm run lint`, `npm run format`)를 수행하여 코드 품질 유지합니다.
    - `main`, `develop` 병합 시 동작하도록 설정합니다.

- 배포 구조
    - 실제 Vercel 프로덕션 배포는 `suwith` 개인 레포지토리에서 진행했습니다.
    - 개발은 조직 레포(`capstone-2025-09`)의 `develop` 브랜치 기준으로 진행하고, 주요 변경사항을 개인 배포 레포로 옮겨 릴리즈했습니다.
    - 팀 레포는 무료 요금제 제약으로 프로덕션 도메인 연결 및 고정 빌드 환경 유지가 어려웠기 때문에
      프로덕션 배포는 Vercel의 개인 요금제를 사용 중인 `suwith` 계정으로 운영했습니다.
    - 이 구조를 통해 비용 효율성과 배포 안정성을 동시에 확보하면서도, 협업과 개발은 팀 레포에서 분리하여 관리했습니다.


---

## 성능 최적화

- 이미지, 비디오 파일에 대해 지연 로딩(Lazy Loading) 및 브라우저 캐싱 설정
- `React.memo`, `useCallback`, `useMemo` 등을 활용하여 불필요한 렌더링 방지
- Three.js 기반 3D 요소는 Canvas 분리 및 FPS 최적화 처리 적용

---

## 린팅 및 포맷팅

사용 도구
- ESLint: React, Tailwind CSS 관련 규칙 적용
- Prettier: 코드 스타일 자동 정리

```bash
npm run lint      # 코드 린트 검사
npm run format    # 코드 자동 포맷팅 (Prettier)
```

---

## 사용 기술 관련 자료

- [React](https://reactjs.org/)
- [Zustand (상태 관리)](https://github.com/pmndrs/zustand)
- [Tailwind CSS](https://tailwindcss.com/docs)
- [Framer Motion](https://www.framer.com/motion/)
- [Three.js](https://threejs.org/)
- [ffmpeg.wasm](https://github.com/ffmpegwasm/ffmpeg.wasm)
- [Vercel](https://vercel.com/docs)