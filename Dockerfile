# Gradle 빌드를 위한 베이스 이미지
FROM openjdk:17 AS build
WORKDIR /workspace/app

# 소스 코드와 Gradle 래퍼를 컨테이너로 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY src src

# 애플리케이션 빌드
RUN ./gradlew build -x test

# 런타임 이미지 준비
FROM openjdk:17-jre-slim
VOLUME /tmp

# 'dev' 프로파일로 설정
ENV SPRING_PROFILES_ACTIVE=dev

ARG JAR_FILE=/workspace/app/build/libs/*.jar
COPY --from=build ${JAR_FILE} app.jar

# 환경 변수를 사용하여 실행 시 Spring 프로파일 지정
ENTRYPOINT ["java","-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}","-jar","/app.jar"]
