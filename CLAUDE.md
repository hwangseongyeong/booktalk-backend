# BookTalk Backend - Claude Code 가이드

이 문서는 Claude Code가 매 세션마다 프로젝트 컨텍스트를 빠르게 파악하기 위한 참고 문서입니다.
새 기능을 만들 때는 이 문서의 컨벤션을 따라주세요.

## 프로젝트 개요
독서 기록을 시각적 서재(책꽂이)로 보여주는 SNS형 독서 앱의 백엔드.
목표: MVP(시연 가능 수준) 우선, 프로덕션 레벨 최적화는 이후 단계.

## 기술 스택
- Java 21, Spring Boot 3.x
- Spring Data JPA + MySQL (utf8mb4_general_ci)
- Spring Security + JWT (OAuth2 소셜 로그인 - 카카오/구글 중 택1로 시작)
- Gradle (Kotlin DSL)
- Flyway (DB 마이그레이션)

## 패키지 구조 컨벤션
도메인 중심 패키지 구조를 따릅니다. 계층(controller/service/repository)이 아니라 도메인이 최상위입니다.

```
com.booktalk
├── domain
│   ├── user            # 회원, 인증
│   ├── book             # 도서 마스터 데이터 (알라딘 API 연동)
│   ├── readingrecord     # 독서 기록 (시작일/완독/별점/메모)
│   └── monthlyshelf      # 월별 서재 집계
│       ├── {Domain}.java              # Entity
│       ├── {Domain}Repository.java    # JPA Repository
│       ├── {Domain}Service.java       # 비즈니스 로직
│       ├── {Domain}Controller.java    # REST API
│       └── dto/                       # 요청/응답 DTO
└── global
    ├── config          # Security, CORS, Swagger 등 설정
    ├── security         # JWT 필터, OAuth2 핸들러
    └── common           # 공통 응답 포맷, 예외 처리
```

## 네이밍 규칙
- REST API 경로: `/api/v1/{리소스명 복수형}` (예: `/api/v1/books`, `/api/v1/reading-records`)
- Entity 필드: camelCase, DB 컬럼: snake_case (Hibernate naming strategy가 자동 변환)
- DTO는 `{Domain}RequestDto` / `{Domain}ResponseDto` 형태로 명명

## MVP 범위 (지금 단계에서 하지 않는 것)
- 결제/구독 로직 (Premium) → 이후 단계
- AI 독서 리포트 → 이후 단계
- 다중 소셜 로그인 → 1개만 (카카오 또는 구글)
- 정교한 예외처리/전역 에러 핸들링 → 기본 수준만
- 테스트 코드 → 핵심 로직(책등 캐싱 등)만 최소한으로

## 핵심 기능 우선순위
1. 회원가입/로그인 (소셜 1개, JWT)
2. 도서 검색/등록 (알라딘 API 연동, ISBN 검색)
3. 독서 기록 CRUD
4. **책등 이미지 생성 및 캐싱** (가장 중요한 차별화 포인트 - 아래 참고)
5. 책꽂이/월별 서재 조회 API

## 책등 이미지 생성 파이프라인 (중요)
- 표지 이미지 → 색상 추출(java 서버사이드, 아래 라이브러리 고려) → SVG/PNG 책등 이미지 생성 → S3 업로드 → URL을 book.spine_image_url에 저장
- 이 로직은 도서 최초 등록 시 1회만 실행 (매 요청마다 재생성 금지)
- 서버사이드 이미지 처리 후보: `java-image-scaling`, 또는 Node.js 배치 서버를 별도로 두는 방안도 고려 가능 (Claude Code에게 물어볼 때 이 트레이드오프를 먼저 논의할 것)

## 코드 작성 시 참고
- 새 도메인 추가 시 위 패키지 구조를 그대로 따라서 생성
- API 응답은 공통 포맷(`global.common.ApiResponse<T>`) 사용
- 환경변수/시크릿은 `application-local.yml`에만 작성하고 절대 커밋하지 말 것 (.gitignore 처리됨)
