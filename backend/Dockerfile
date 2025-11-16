# jdk17 Image Start
FROM eclipse-temurin:17-jdk-alpine

# jar 파일 복제
COPY build/libs/*.jar app.jar

# 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]
