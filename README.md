# BookTalk Backend

독서를 시각적 서재로 기록/공유하는 앱의 백엔드 (Spring Boot).

## 시작하기

1. MySQL 데이터베이스 생성
   ```sql
   CREATE DATABASE booktalk CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   ```
2. 로컬 설정 파일 준비
   ```bash
   cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
   # 값 채워넣기 (DB 접속정보, JWT secret, 알라딘 API 키, AWS 등)
   ```
3. 실행
   ```bash
   ./gradlew bootRun
   ```
4. Swagger UI: http://localhost:8080/swagger-ui.html

## 개발 시 참고
- 이 프로젝트의 컨벤션/우선순위는 `CLAUDE.md` 참고 (Claude Code 사용 시에도 이 문서를 자동으로 읽습니다)
- 스키마 변경은 반드시 Flyway 마이그레이션(`src/main/resources/db/migration`)으로 관리
