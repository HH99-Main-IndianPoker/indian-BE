# Build argument 추가
ARG JAR_FILE=build/libs/indianfrog-0.0.1-SNAPSHOT.jar

FROM openjdk:17
WORKDIR /app

# 수정된 COPY 명령. ARG로 받은 JAR_FILE 변수 사용
COPY ${JAR_FILE} /app/indianfrog-0.0.1-SNAPSHOT.jar

EXPOSE 8080

# 환경 변수 추가
ENV SPRING_PROFILES_ACTIVE=dev

CMD ["java","-jar","indianfrog-0.0.1-SNAPSHOT.jar"]
