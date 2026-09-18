# 표지 이미지 프록시 (Cloudflare Worker)

카카오/다음 CDN이 AWS 서버 IP를 차단해 백엔드가 표지를 직접 못 받는 문제를 우회한다.
Cloudflare 엣지(비-AWS IP)에서 표지를 대신 받아 백엔드로 넘겨준다.

## 배포

```bash
cd cloudflare/cover-proxy

# 1) Cloudflare 로그인 (최초 1회)
npx wrangler login

# 2) 프록시 인증 토큰(시크릿) 설정 — 백엔드 COVER_PROXY_TOKEN과 같은 값
#    충분히 긴 랜덤값 사용:  openssl rand -hex 24
npx wrangler secret put PROXY_TOKEN

# 3) 배포
npx wrangler deploy
```

배포되면 `https://booktalk-cover-proxy.<계정>.workers.dev` 주소가 나온다.

## 동작 확인

```bash
curl -s -o /tmp/c.jpg -w "%{http_code} %{content_type}\n" \
  -H "X-Proxy-Token: <PROXY_TOKEN>" \
  "https://booktalk-cover-proxy.<계정>.workers.dev/?url=https%3A%2F%2Fsearch1.kakaocdn.net%2Fthumb%2FR120x174.q85%2F%3Ffname%3D..."
```
`200 image/jpeg` 면 성공 (카카오가 Cloudflare IP는 차단 안 함을 확인).

## 백엔드 연동

EC2 `.env`에 아래 설정 후 app 재기동:
```
COVER_PROXY_BASE_URL=https://booktalk-cover-proxy.<계정>.workers.dev
COVER_PROXY_TOKEN=<위 PROXY_TOKEN과 동일>
```
설정되면 `SpineAssetService`가 표지 조회를 이 프록시 경유로 우선 시도하고, 실패 시 직접 조회로 폴백한다.

## 보안
- `PROXY_TOKEN` 시크릿으로 인증 (오픈 프록시 방지)
- `ALLOWED_HOSTS` 허용 목록으로 카카오/다음 이미지 호스트만 프록시 (SSRF 방지)
