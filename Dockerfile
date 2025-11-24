# 1) Java 17 JDK 이미지 사용
FROM eclipse-temurin:17-jdk

# 2) 컨테이너 작업 디렉토리 설정
WORKDIR /app

# 3) 프로젝트 파일 전체 복사
COPY . .

# 4) Gradle Wrapper를 통해 Spring Boot JAR 빌드
RUN chmod +x ./gradlew
RUN ./gradlew bootJar

# 5) 빌드된 JAR 실행
CMD ["java", "-jar", "build/libs/newchromeproject-0.0.1-SNAPSHOT.jar"]
