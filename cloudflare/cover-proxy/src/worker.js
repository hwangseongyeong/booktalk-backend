/**
 * 표지 이미지 프록시 (Cloudflare Worker)
 *
 * 카카오/다음 CDN이 AWS(EC2) 서버 IP를 차단해서, 백엔드가 표지를 직접 못 받는다.
 * 이 Worker는 Cloudflare 엣지(비-AWS IP)에서 대신 이미지를 받아 백엔드로 넘겨준다.
 *
 * 사용: GET https://<worker>/?url=<표지 이미지 URL>
 *       헤더 X-Proxy-Token: <PROXY_TOKEN 시크릿>
 *
 * 보안:
 *  - PROXY_TOKEN 시크릿으로 인증 (오픈 프록시 방지)
 *  - ALLOWED_HOSTS 허용 목록으로 SSRF 방지 (카카오/다음 이미지 호스트만)
 */

const ALLOWED_HOSTS = [
  "search1.kakaocdn.net",
  "search2.kakaocdn.net",
  "search3.kakaocdn.net",
  "search4.kakaocdn.net",
  "img1.kakaocdn.net",
  "dn.kakaocdn.net",
  "t1.daumcdn.net",
  "i1.daumcdn.net",
];

const BROWSER_UA =
  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
  "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

export default {
  async fetch(request, env) {
    if (request.method !== "GET") {
      return new Response("method not allowed", { status: 405 });
    }

    // 공유 시크릿 검증
    if (env.PROXY_TOKEN && request.headers.get("X-Proxy-Token") !== env.PROXY_TOKEN) {
      return new Response("forbidden", { status: 403 });
    }

    const target = new URL(request.url).searchParams.get("url");
    if (!target) return new Response("missing url param", { status: 400 });

    let targetUrl;
    try {
      targetUrl = new URL(target);
    } catch {
      return new Response("bad url", { status: 400 });
    }

    // SSRF 방지: 허용된 이미지 호스트만
    if (!ALLOWED_HOSTS.includes(targetUrl.hostname)) {
      return new Response("host not allowed", { status: 403 });
    }

    const upstream = await fetch(targetUrl.toString(), {
      headers: {
        "User-Agent": BROWSER_UA,
        Referer: targetUrl.origin + "/",
        Accept: "image/avif,image/webp,image/apng,image/*,*/*;q=0.8",
      },
      cf: { cacheTtl: 86400, cacheEverything: true },
    });

    if (!upstream.ok) {
      return new Response("upstream error: " + upstream.status, { status: upstream.status });
    }

    const headers = new Headers();
    headers.set("Content-Type", upstream.headers.get("Content-Type") || "image/jpeg");
    headers.set("Cache-Control", "public, max-age=86400");
    return new Response(upstream.body, { status: 200, headers });
  },
};
