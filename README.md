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
- 알라딘 검색(`AladinClient`)은 TTBKey가 없거나 알라딘 API가 응답하지 않아도 예외를 던지지 않고 빈 목록을 반환합니다.
  로컬 DB 검색 결과는 항상 정상적으로 내려가니, 로컬 개발 중 TTBKey 없이도 앱은 문제없이 동작합니다.

## 로그인 흐름 (카카오/네이버/구글/페이스북)
프런트(Vercel)와 백엔드(Render)가 다른 도메인에 배포되는 구조라, Spring Security의 서버 주도
리다이렉트 방식 대신 아래 흐름으로 구현했습니다.

1. 프런트가 각 제공자의 인가 화면으로 사용자를 직접 이동시킴 (`redirect_uri`는 프런트 콜백 URL)
2. 제공자가 프런트 콜백 URL로 `code`(네이버는 `state`도)를 붙여 리다이렉트
3. 프런트가 그 `code`를 `POST /api/v1/auth/{provider}/login` 으로 백엔드에 전달
4. 백엔드가 각 제공자의 토큰/사용자정보 엔드포인트를 직접 호출해 신원을 확인하고,
   BookTalk 자체 access/refresh JWT를 발급해서 응답
5. 이후 모든 API는 `Authorization: Bearer {accessToken}` 헤더로 인증

## API (Phase 1 - 코어 MVP)
모든 응답은 `{ success, data, message }` 형태로 감싸져 내려갑니다.

### 인증
| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| POST | `/api/v1/auth/{provider}/login` | - | 소셜 로그인. provider: `kakao`\|`naver`\|`google`\|`facebook` |
| POST | `/api/v1/auth/refresh` | - | refreshToken으로 토큰 재발급 |
| GET | `/api/v1/auth/me` | O | 내 정보 조회 |

### 도서 / 독서 기록 / 서재
| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| POST | `/api/v1/books` | O | 책 등록(직접 입력). ISBN 중복 시 기존 책 반환 |
| GET | `/api/v1/books?query=` | - | 책 검색. 로컬 DB + 알라딘 API 결과를 함께 반환 (`id`가 있으면 이미 등록된 책, 없으면 알라딘 결과라 등록 먼저 필요) |
| GET | `/api/v1/books/{id}` | - | 책 상세 |
| POST | `/api/v1/reading-records` | O | 독서 시작(`bookId`, `startDate`) |
| PATCH | `/api/v1/reading-records/{id}/complete` | O | 완독 처리(`endDate`, `rating`, `oneLineNote`) |
| GET | `/api/v1/reading-records?status=READING\|COMPLETED` | O | 내 독서 기록 목록 |
| GET | `/api/v1/shelves/monthly?yearMonth=2026-07` | O | 월별 서재(완독한 책 목록 + 권수) |
