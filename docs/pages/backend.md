---
title: "백엔드 개발 가이드"
layout: default
nav_order: 4
parent: 가이드
has_toc: false
---

# **백엔드 개발 가이드**

{: .no_toc }

## 목차

{: .no_toc }

- TOC
{:toc}

---

## 소개
본 백엔드 시스템은 Covos 플랫폼을 위한 서버 애플리케이션입니다. 사용자가 보이스팩을 생성, 판매, 구매할 수 있는 API를 제공하며, AI 음성 합성 및 영상 기반 보이스팩 생성 등의 기능을 제공합니다.

---

## 시스템 아키텍처
이 시스템은 Spring Boot 기반의 백엔드 서버로 구현되어 있으며 다음과 같은 구성요소로 이루어져 있습니다:

- **Spring Boot 애플리케이션** - 핵심 비즈니스 로직 처리
- **MySQL 데이터베이스** - 사용자, 보이스팩, 거래 정보 등 데이터 저장
- **AWS S3** - 음성 파일, 이미지 등 미디어 저장
- **AWS SQS/RabbitMQ** - 비동기 작업 처리를 위한 메시지 큐
- **AI 모델 서비스 연동** - 보이스팩 생성 및 합성을 위한 외부 AI 서비스 연동

---

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

---

## 프로젝트 구조
백엔드 프로젝트는 다음과 같은 구조로 되어 있습니다:

```
backend/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── kr/ac/kookmin/cs/capstone/voicepack_platform/
│   │   │       ├── aiAssistant/      - AI 리포터 관련 기능
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

---

## API 엔드포인트

백엔드는 다음과 같은 주요 API 엔드포인트를 제공합니다:

### 1. 사용자 관리
- `POST /api/users/signup` - 회원가입
- `POST /api/users/login` - 로그인
- `GET /api/users/profile` - 사용자 프로필 조회

### 2. 보이스팩 관리
- `GET /api/voicepack` - 보이스팩 목록 조회 (필터 및 사용자 ID로 조회 가능)
- `GET /api/voicepack/{voicepackId}` - 보이스팩 1개 상세 조회
- `DELETE /api/voicepack/{voicepackId}` - 보이스팩 삭제
- `PATCH /api/voicepack/{voicepackId}` - 보이스팩 공개 여부 변경

### 3. 보이스팩 변환/합성
- `POST /api/voicepack/convert` - 음성 파일을 보이스팩으로 변환 (multipart/form-data)
- `GET /api/voicepack/convert/status/{id}` - 보이스팩 변환 상태 조회 (Polling)
- `POST /api/voicepack/synthesis` - 보이스팩 기반 TTS(음성합성) 생성 요청 (비동기)
- `GET /api/voicepack/synthesis/status/{id}` - 음성합성(TTS) 상태 조회 (Polling)
- `GET /api/voicepack/example/{voicepackId}` - 보이스팩 예시 음성 파일 조회

### 4. 보이스팩 사용권
- `POST /api/voicepack/usage-right` - 보이스팩 사용권 획득(구매/제작자 자동 획득)
- `GET /api/voicepack/usage-right` - 사용자가 보유한 보이스팩 목록 조회

### 5. 영상 기반 보이스팩
- `POST /api/video2voicepack` - 영상 기반 보이스팩 생성

### 6. 크레딧 관리
- `POST /api/credits/purchase` - 크레딧 구매
- `GET /api/credits/balance/{userId}` - 잔액 조회
- `GET /api/credits/history/{userId}` - 거래 내역 조회

### 7. 판매 관리
- `GET /api/sales/summary` - 판매 통계 조회
- `GET /api/sales/history` - 판매 내역 조회

### 8. AI 리포터
- `POST /api/ai-assistant/setting` - AI 리포터 설정
- `POST /api/ai-assistant/synthesis` - AI 리포터 음성 합성

---

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

---

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

### CI/CD (Github Actions 기반 자동 배포)

본 프로젝트의 백엔드는 AWS EC2 서버에 Github Actions를 활용한 CI/CD 파이프라인으로 자동 배포됩니다.

- main 브랜치에 push 시, Github Actions가 Gradle 빌드, Docker 이미지 빌드 및 Docker Hub 푸시, 그리고 EC2 서버에 원격 배포까지 자동으로 수행합니다.
- application.yml 등 민감 정보는 Github Secrets를 통해 안전하게 관리합니다.

아래는 실제로 사용한 Github Actions workflow 예시입니다.

```yaml
name: Java CI/CD with Gradle & Docker

on:
  push:
    branches: [ "backend/main" ]
  workflow_dispatch:

jobs:
  build-docker-image:
    runs-on: ubuntu-latest
    permissions:
      contents: read
    steps:
      - uses: actions/checkout@v4
      # JDK 17 설정
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      # Gradle 설정
      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@af1da67850ed9a4cedd57bfd976089dd991e2582 # v4.0.0
      # Gradle wrapper 실행 권한 부여
      - name: Grant execute permission for gradlew
        run: chmod +x backend/gradlew
      # GitHub Secrets에서 application.yml 파일 생성
      - name: Create application.yml from GitHub Secrets
        run: |
          mkdir -p backend/src/main/resources
          echo "${{ secrets.APPLICATION }}" | base64 --decode > backend/src/main/resources/application.yml
      # Gradle 빌드 실행
      - name: Build with Gradle Wrapper
        working-directory: backend
        run: ./gradlew build
      # Docker 이미지 빌드
      - name: Build Docker Image
        working-directory: backend
        run: |
          docker build -t ${{ secrets.DOCKERHUB_USERNAME }}/2025capstone:latest .
      # Docker Hub 로그인
      - name: Login to Docker Hub
        uses: docker/login-action@v2
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_PASSWORD }}
      # Docker Hub에 이미지 푸시
      - name: Push Docker Image to Docker Hub
        run: docker push ${{ secrets.DOCKERHUB_USERNAME }}/2025capstone:latest

  run-docker-image-on-ec2:
    needs: build-docker-image
    runs-on: ubuntu-latest
    steps:
      # EC2에 SSH 접속하기 위해 known_hosts 파일에 EC2 호스트 추가
      - name: Update Known Hosts
        run: |
          mkdir -p ~/.ssh
          ssh-keyscan -H ${{ secrets.EC2_HOST }} >> ~/.ssh/known_hosts
      # EC2에 Docker 이미지 배포
      - name: Deploy to EC2
        uses: appleboy/ssh-action@v0.1.6
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ${{ secrets.EC2_USER }}
          key: ${{ secrets.EC2_SSH_KEY }}
          script: |
            # 기존 컨테이너가 있으면 중지하고 삭제
            sudo docker stop $(sudo docker ps -q --filter ancestor=${{ secrets.DOCKERHUB_USERNAME }}/2025capstone)
            sudo docker rm $(sudo docker ps -aq --filter ancestor=${{ secrets.DOCKERHUB_USERNAME }}/2025capstone)
            # 최신 Docker 이미지 가져오기
            sudo docker pull ${{ secrets.DOCKERHUB_USERNAME }}/2025capstone:latest
            # 새 컨테이너 실행
            sudo docker run --rm -it -d -p 8080:8080 --name 2025capstone ${{ secrets.DOCKERHUB_USERNAME }}/2025capstone:latest
            # 사용하지 않는 Docker 이미지 및 캐시 정리
            sudo docker system prune -f
```

---

## FAQ

### Q: API 문서는 어떻게 확인할 수 있나요?
A: 서버 실행 후 `http://localhost:8080/swagger-ui.html`에서 Swagger UI로 API 문서를 확인할 수 있습니다.

### Q: 외부 AI 서비스 연동은 어떻게 설정하나요?
A: `application.yaml` 파일의 `ai.model.service` 섹션에 필요한 설정을 추가하세요.

### Q: 메시지 큐는 어떻게 구성되나요?
A: RabbitMQ와 AWS SQS를 사용하며, `application.yaml`의 `spring.rabbitmq` 및 `aws.sqs` 섹션에 설정을 추가하세요.

### Q: 테스트 데이터를 생성하는 방법이 있나요?
A: 개발 환경에서 `/api/voicepack/debug/create-voicepack` 엔드포인트를 사용하여 테스트 데이터를 생성할 수 있습니다.