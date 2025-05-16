---
title: "백엔드 개발 가이드"
layout: default
nav_order: 4
parent: 메뉴얼
---

# 백엔드 개발 가이드

## 목차
- [소개](#소개)
- [시스템 아키텍처](#시스템-아키텍처)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [API 엔드포인트](#api-엔드포인트)
- [개발 환경 설정](#개발-환경-설정)
- [배포 방법](#배포-방법)
- [FAQ](#faq)

## 소개
본 백엔드 시스템은 VoicePack 플랫폼을 위한 서버 애플리케이션입니다. 사용자가 보이스팩을 생성, 판매, 구매할 수 있는 API를 제공하며, AI 음성 합성 및 영상 기반 보이스팩 생성 등의 기능을 제공합니다.

## 시스템 아키텍처
이 시스템은 Spring Boot 기반의 백엔드 서버로 구현되어 있으며 다음과 같은 구성요소로 이루어져 있습니다:

- **Spring Boot 애플리케이션** - 핵심 비즈니스 로직 처리
- **MySQL 데이터베이스** - 사용자, 보이스팩, 거래 정보 등 데이터 저장
- **AWS S3** - 음성 파일, 이미지 등 미디어 저장
- **AWS SQS/RabbitMQ** - 비동기 작업 처리를 위한 메시지 큐
- **AI 모델 서비스 연동** - 보이스팩 생성 및 합성을 위한 외부 AI 서비스 연동

## 기술 스택

### 주요 기술
- **언어**: Kotlin 1.9.22
- **프레임워크**: Spring Boot 3.2.3
- **빌드 도구**: Gradle (with Kotlin DSL)
- **데이터베이스**: MySQL (AWS RDS)
- **ORM**: JPA/Hibernate
- **API 문서화**: OpenAPI/Swagger

### 주요 라이브러리
- Spring Data JPA
- Spring Web
- Spring Validation
- Kotlinx Serialization
- Kotlinx Coroutines
- Ktor Client
- AWS SDK (S3, SQS)
- RabbitMQ
- JUnit 5, Mockk (테스트)

## 프로젝트 구조
백엔드 프로젝트는 다음과 같은 구조로 되어 있습니다:

```
backend/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── kr/ac/kookmin/cs/capstone/voicepack_platform/
│   │   │       ├── aiAssistant/      - AI 비서 관련 기능
│   │   │       ├── common/           - 공통 유틸리티
│   │   │       ├── config/           - 설정 클래스
│   │   │       ├── credit/           - 크레딧 시스템
│   │   │       ├── notification/      - 알림 기능
│   │   │       ├── quote/            - 견적 관련 기능
│   │   │       ├── sale/             - 판매 관련 기능
│   │   │       ├── user/             - 사용자 관리
│   │   │       ├── video2voicepack/  - 영상 기반 보이스팩
│   │   │       ├── voicepack/        - 보이스팩 관리
│   │   │       └── VoicepackPlatformApplication.kt
│   │   └── resources/
│   │       └── application.yaml      - 애플리케이션 설정
│   └── test/                         - 테스트 코드
├── build.gradle.kts                  - 빌드 설정
└── Dockerfile                        - 컨테이너 배포 설정
```

## API 엔드포인트

백엔드는 다음과 같은 주요 API 엔드포인트를 제공합니다:

### 사용자 관리
- `POST /api/users/signup` - 회원가입
- `POST /api/users/login` - 로그인
- `GET /api/users/profile` - 사용자 프로필 조회

### 보이스팩
- `POST /api/voicepack/upload` - 보이스팩 업로드
- `GET /api/voicepack/{id}` - 보이스팩 정보 조회
- `GET /api/voicepack/list` - 보이스팩 목록 조회
- `POST /api/voicepack/synthesis` - 텍스트 음성 합성 요청

### 영상 기반 보이스팩
- `POST /api/video2voicepack` - 영상 기반 보이스팩 생성

### 크레딧 관리
- `POST /api/credits/purchase` - 크레딧 구매
- `GET /api/credits/balance/{userId}` - 잔액 조회
- `GET /api/credits/history/{userId}` - 거래 내역 조회

### 판매 관리
- `GET /api/sales/summary` - 판매 통계 조회
- `GET /api/sales/history` - 판매 내역 조회

### AI 비서
- `POST /api/ai-assistant/setting` - AI 비서 설정
- `POST /api/ai-assistant/synthesis` - AI 비서 음성 합성

## 개발 환경 설정

### 필수 요구사항
- JDK 17 이상
- Gradle 7.0 이상
- Docker (선택사항: 컨테이너 실행용)

### 로컬 개발 환경 설정
1. 저장소 클론
   ```bash
   git clone https://github.com/your-repo/voicepack-platform.git
   cd voicepack-platform/backend
   ```

2. 설정 파일 생성
   ```bash
   cp src/main/resources/application.yaml-example src/main/resources/application.yaml
   # application.yaml 파일에 필요한 설정 추가
   ```

3. 애플리케이션 실행
   ```bash
   ./gradlew bootRun
   ```

4. 테스트 실행
   ```bash
   ./gradlew test
   ```

## 배포 방법

### Docker를 이용한 배포
1. Docker 이미지 빌드
   ```bash
   ./gradlew build
   docker build -t voicepack-platform-backend .
   ```

2. 이미지 실행
   ```bash
   docker run -p 8080:8080 voicepack-platform-backend
   ```

### AWS 배포
1. Elastic Beanstalk 설정
2. ECR에 이미지 푸시
3. ECS/Fargate로 컨테이너 실행

## FAQ

### Q: API 문서는 어떻게 확인할 수 있나요?
A: 서버 실행 후 `http://localhost:8080/swagger-ui.html`에서 Swagger UI로 API 문서를 확인할 수 있습니다.

### Q: 외부 AI 서비스 연동은 어떻게 설정하나요?
A: `application.yaml` 파일의 `ai.model.service` 섹션에 필요한 설정을 추가하세요.

### Q: 메시지 큐는 어떻게 구성되나요?
A: RabbitMQ와 AWS SQS를 사용하며, `application.yaml`의 `spring.rabbitmq` 및 `aws.sqs` 섹션에 설정을 추가하세요.

### Q: 테스트 데이터를 생성하는 방법이 있나요?
A: 개발 환경에서 `/api/voicepack/debug/create-voicepack` 엔드포인트를 사용하여 테스트 데이터를 생성할 수 있습니다.