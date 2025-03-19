# jdk17 Image Start
FROM openjdk:17

# jar 파일 빌드
RUN ./gradlew clean build

# jar 파일 복제
COPY build/libs/*.jar app.jar

# 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]