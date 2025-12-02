# 1. 베이스 이미지 설정 (Java 17 버전 사용)
# 프로젝트 Java 버전에 맞춰서 17을 21이나 11로 변경 가능합니다.
FROM amazoncorretto:17

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 빌드된 JAR 파일을 Docker 이미지 내부로 복사
# build/libs/*.jar 부분은 실제 빌드된 jar 파일 경로입니다.
# 보통 *SNAPSHOT.jar 또는 *-plain.jar가 아닌 실행 가능한 jar를 복사합니다.
COPY build/libs/*-SNAPSHOT.jar app.jar

# 4. 컨테이너 실행 시 실행될 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]