# --- 빌드 스테이지 ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 의존성 레이어를 먼저 캐시하기 위해 gradle 관련 파일만 먼저 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || true

# 소스 복사 후 실제 빌드
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# --- 실행 스테이지 ---
FROM eclipse-temurin:21-jre
WORKDIR /app

# 로컬 저장 모드(storage.mode=local)일 때 책등 이미지가 저장될 디렉터리.
# docker-compose에서 이 경로를 볼륨으로 마운트해서 컨테이너 재시작에도 유지되게 한다.
RUN mkdir -p /app/uploads

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
