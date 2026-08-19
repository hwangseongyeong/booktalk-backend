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
- 아직 로그인/인증이 없어서 모든 요청은 `DemoUserProvider`가 만드는 단일 데모 사용자 기준으로 동작합니다.
  실제 JWT/OAuth2 인증이 붙으면 이 부분을 SecurityContext 기반으로 교체할 예정입니다.

## API (Phase 1 - 코어 MVP)
모든 응답은 `{ success, data, message }` 형태로 감싸져 내려갑니다.

| 메서드 | 경로 | 설명 |
|---|---|---|
| POST | `/api/v1/books` | 책 등록(직접 입력). ISBN 중복 시 기존 책 반환 |
| GET | `/api/v1/books?query=` | 책 검색(로컬 DB 대상, 알라딘 연동 전) |
| GET | `/api/v1/books/{id}` | 책 상세 |
| POST | `/api/v1/reading-records` | 독서 시작(`bookId`, `startDate`) |
| PATCH | `/api/v1/reading-records/{id}/complete` | 완독 처리(`endDate`, `rating`, `oneLineNote`) |
| GET | `/api/v1/reading-records?status=READING\|COMPLETED` | 내 독서 기록 목록 |
| GET | `/api/v1/shelves/monthly?yearMonth=2026-07` | 월별 서재(완독한 책 목록 + 권수) |
