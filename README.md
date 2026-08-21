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
   # 값 채워넣기 (DB 접속정보, JWT secret, 카카오 API 키 등)
   ```
3. 실행
   ```bash
   ./gradlew bootRun
   ```
4. Swagger UI: http://localhost:8080/swagger-ui.html

## 개발 시 참고
- 이 프로젝트의 컨벤션/우선순위는 `CLAUDE.md` 참고 (Claude Code 사용 시에도 이 문서를 자동으로 읽습니다)
- 스키마 변경은 반드시 Flyway 마이그레이션(`src/main/resources/db/migration`)으로 관리
- 도서 검색(`KakaoBookClient`)은 카카오 로그인과 같은 REST API 키를 재사용합니다. 승인 대기가 없어 키만 있으면 바로 동작합니다.
  키가 없거나 카카오 API가 응답하지 않아도 예외를 던지지 않고 빈 목록을 반환하므로, 로컬 DB 검색 결과는 항상 정상적으로 내려갑니다.
  (알라딘 연동(`AladinClient`)도 코드는 남아있지만 현재는 사용하지 않습니다 — TTBKey 승인 나면 전환 가능)

## 책등 이미지 파이프라인
책 등록(`POST /api/v1/books`) 시 `BookService.register()` 안에서 1회 자동으로 실행됩니다.

1. `ImageColorExtractor`가 표지 이미지(`coverImageUrl`)를 내려받아 대표색(primary)/보조색(accent)을 추출
   (순수 `ImageIO` + 색상 양자화, 외부 라이브러리 없음). 표지가 없거나 추출에 실패하면
   `FallbackPalette`가 제목 기반으로 고정 팔레트 색상을 대신 골라줍니다.
2. `SpineSvgBuilder`가 그 색상으로 책등 모양 SVG를 문자열로 직접 생성
3. `SpineStorage` 구현체가 SVG를 저장하고 공개 URL을 반환 (`storage.mode`로 전환)
4. `Book.updateSpineAssets()`로 `spineImageUrl`/`primaryColor`/`accentColor`를 채움

### 저장 방식 전환 (로컬 ↔ R2)
`storage.mode` 값에 따라 스프링이 두 구현체 중 하나만 빈으로 올립니다.

- **`local` (기본값)** — `LocalSpineStorage`가 `storage.local.upload-dir` 아래 `spines/{bookId}.svg`로 저장하고,
  `LocalUploadResourceConfig`가 그 디렉터리를 `/uploads/**` 정적 경로로 서빙합니다.
  R2/AWS 계정이 없어도 바로 동작합니다. (`.gitignore`에 `/uploads/` 추가되어 있어 커밋 안 됨)
- **`r2`** — `R2SpineStorage`가 Cloudflare R2(S3 호환 API)에 업로드합니다.
  R2 계정 만드시면 `application-local.yml`에서 `storage.mode: r2`로 바꾸고 `r2:` 섹션만 채우면 됩니다.
  R2 API 토큰은 Cloudflare 대시보드 > R2 > Manage API tokens에서 발급하고,
  버킷의 "Public access"를 켜면 나오는 `https://pub-xxxx.r2.dev` 주소를 `public-base-url`에 넣으면 됩니다.

두 방식 모두 업로드가 실패해도(디스크 오류, R2 미설정 등) 등록 자체는 막지 않고 색상만 채웁니다.
프런트는 `spineImageUrl`이 없으면 `primaryColor` 배경의 색상 블록으로 자동 대체해서 보여줍니다.

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
| GET | `/api/v1/books?query=` | - | 책 검색. 로컬 DB + 카카오 책 검색 결과를 함께 반환 (`id`가 있으면 이미 등록된 책, 없으면 카카오 결과라 등록 먼저 필요) |
| GET | `/api/v1/books/{id}` | - | 책 상세 |
| POST | `/api/v1/reading-records` | O | 독서 시작(`bookId`, `startDate`) |
| PATCH | `/api/v1/reading-records/{id}/complete` | O | 완독 처리(`endDate`, `rating`, `oneLineNote`) |
| GET | `/api/v1/reading-records?status=READING\|COMPLETED` | O | 내 독서 기록 목록 |
| GET | `/api/v1/shelves/monthly?yearMonth=2026-07` | O | 월별 서재(완독한 책 목록 + 권수) |

## EC2 배포

ALB 없이 EC2 한 대에 앱+MySQL+Caddy(자동 HTTPS)를 `docker-compose`로 함께 띄우는 방식.
(ECS/Elastic Beanstalk는 ALB 고정비가 트래픽 0이어도 월 15~20달러 이상 나가서, 트래픽 거의 없는 개발 단계에는 이 방식이 훨씬 저렴합니다.)

### 1. EC2 인스턴스 준비
- **인스턴스 타입**: `t3.micro` 또는 `t4g.micro` (ARM, 더 저렴)
- **AMI**: Ubuntu 22.04/24.04 또는 Amazon Linux 2023
- **보안 그룹**: 22(SSH, 내 IP만), 80(HTTP), 443(HTTPS) 인바운드 허용
- **Elastic IP** 할당해서 재부팅해도 IP 안 바뀌게 해두는 걸 추천

### 2. 서버에 Docker 설치
```bash
sudo apt update && sudo apt install -y docker.io docker-compose-plugin
sudo usermod -aG docker $USER   # 다시 로그인해야 적용됨
```

### 3. 저장소 배포
```bash
git clone https://github.com/hwangseongyeong/booktalk-backend.git
cd booktalk-backend
cp .env.example .env
nano .env   # 실제 값 채우기 (DB 비밀번호, JWT_SECRET, 소셜 로그인 키 등)
```

도메인이 있다면 `Caddyfile`의 `your-domain.com`을 실제 도메인으로, 없다면 우선 `:80 { reverse_proxy app:8080 }` 형태로 바꿔서 IP로만 접속 (이 경우 HTTPS는 나중에 도메인 연결 후 적용).

### 4. 실행
```bash
docker compose up -d --build
docker compose logs -f app   # 정상 기동 확인
```

### 5. 프런트/소셜 로그인 콘솔에 반영
- Vercel의 `NEXT_PUBLIC_API_BASE_URL`을 `https://your-domain.com/api/v1`로 변경
- 카카오/네이버/구글/페이스북 각 개발자 콘솔에 새 `redirect_uri`(`https://프런트도메인/oauth/callback/{provider}`) 등록
  (대부분 소셜 로그인은 프로덕션에서 `https://`만 허용하고 `http://`는 localhost 예외 외엔 거부합니다)

### 참고
- 책등 이미지는 기본값(`STORAGE_MODE=local`)이면 `uploads-data` 볼륨에 저장되어 `docker compose down`/재시작에도 유지됩니다.
  다만 EC2 인스턴스 자체를 지우면 함께 사라지므로, 나중에 여유 생기면 `STORAGE_MODE=r2`(또는 AWS S3)로 옮기는 걸 추천합니다.
- MySQL도 같은 EC2 인스턴스 안에서 컨테이너로 도는 구조라 별도 RDS 비용이 없습니다. 트래픽이 늘어나면 그때 RDS 분리를 고려하면 됩니다.
