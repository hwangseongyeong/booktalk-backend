package com.booktalk.domain.book.spine;

/**
 * 바탕화면(월페이퍼) 느낌의 세로형 SVG 책등을 생성한다.
 *
 * 저해상도(카카오 120px 썸네일) 표지를 세로로 잘라 배경에 깔면 뿌옇게 뭉개져 품질이 낮으므로,
 * 표지는 색상 추출에만 쓰고 책등은 색상 기반으로 새로 그린다.
 *
 * 실제 양장본처럼 보이게 하는 장치(조명 sweep·헤드밴드·밑줄 프레이밍)는 쓰지 않고,
 * 추출한 두 색을 부드럽게 섞은 그라데이션 배경으로 '바탕화면 이미지' 같은 결을 낸다.
 *
 * 구성:
 * - 배경: primary → 중간 혼합색 → accent 로 이어지는 대각 소프트 그라데이션 + 은은한 상단 글로우
 * - 제목: 책등 중앙을 채우는 히어로 텍스트(정자 세로쓰기)
 * - 저자: 제목 아래 / 출판사: 테일(맨 아래)에 고정
 * - 그라데이션 위에서도 읽히도록 모든 글자에 부드러운 그림자(halo)
 *
 * 모든 글자는 회전 없이 똑바로 세운 세로쓰기(정자)이며, 길이에 따라 겹치지 않게 자동 축약한다.
 */
public final class SpineSvgBuilder {

    private static final int WIDTH = 60;
    private static final int HEIGHT = 320;

    private static final int HEAD_MARGIN = 20;  // 제목 영역 상단 여백
    private static final int TAIL_MARGIN = 22;   // 출판사 하단 안전 여백(테일밴드 위)
    private static final int GROUP_GAP = 12;      // 제목그룹 ~ 출판사 간격

    private static final int TITLE_FONT_SIZE = 16;
    private static final int TITLE_LINE_HEIGHT = 17;
    private static final int TITLE_MAX_LENGTH = 12;

    private static final int AUTHOR_FONT_SIZE = 10;
    private static final int AUTHOR_LINE_HEIGHT = 12;
    private static final int AUTHOR_MAX_LENGTH = 6;
    private static final int TITLE_AUTHOR_GAP = 12;

    private static final int PUBLISHER_FONT_SIZE = 9;
    private static final int PUBLISHER_LINE_HEIGHT = 11;
    private static final int PUBLISHER_MAX_LENGTH = 6;

    private static final int SEP_PAD = 9;         // 제목 그룹 상하 여백

    private SpineSvgBuilder() {
    }

    /** 색상 기반 책등(기본이자 유일한 렌더 경로). primary=배경, accent=밴드/밑줄. */
    public static String buildFromColors(String title, String author, String publisher,
                                         String primaryColor, String accentColor) {
        int centerX = WIDTH / 2;
        String midColor = blend(primaryColor, accentColor, 0.45);
        String ink = contrastText(midColor);
        String halo = ink.equals("#ffffff") ? "#000000" : "#ffffff";

        String titleText = stripSpaces(title);
        String authorText = cut(stripSpaces(author), AUTHOR_MAX_LENGTH);
        String publisherText = cut(stripSpaces(publisher), PUBLISHER_MAX_LENGTH);

        // 출판사: 테일(맨 아래) 고정
        int pubHeight = publisherText.isEmpty() ? 0 : publisherText.length() * PUBLISHER_LINE_HEIGHT;
        int pubTop = HEIGHT - TAIL_MARGIN - pubHeight;

        // 제목+저자 그룹이 놓일 영역
        int regionTop = HEAD_MARGIN;
        int regionBottom = (publisherText.isEmpty() ? HEIGHT - TAIL_MARGIN : pubTop - GROUP_GAP);

        // 제목 길이 자동 축약(저자/밑줄 공간 확보)
        int authorBlock = authorText.isEmpty() ? 0 : (TITLE_AUTHOR_GAP + authorText.length() * AUTHOR_LINE_HEIGHT);
        int maxTitleChars = Math.max(1, (regionBottom - regionTop - 2 * SEP_PAD - authorBlock) / TITLE_LINE_HEIGHT);
        titleText = cut(titleText, Math.min(TITLE_MAX_LENGTH, maxTitleChars));

        int titleHeight = titleText.length() * TITLE_LINE_HEIGHT;
        int authorHeight = authorText.isEmpty() ? 0 : authorText.length() * AUTHOR_LINE_HEIGHT;

        // 그룹 전체(밑줄+제목+저자)를 영역 중앙에 배치
        int groupHeight = 2 * SEP_PAD + titleHeight + (authorText.isEmpty() ? 0 : (TITLE_AUTHOR_GAP + authorHeight));
        int groupTop = regionTop + Math.max(0, (regionBottom - regionTop - groupHeight) / 2);

        int titleTop = groupTop + SEP_PAD;
        int authorTop = titleTop + titleHeight + TITLE_AUTHOR_GAP;

        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(WIDTH)
                .append("\" height=\"").append(HEIGHT)
                .append("\" viewBox=\"0 0 ").append(WIDTH).append(" ").append(HEIGHT).append("\">");

        // 월페이퍼 그라데이션 + 글자 그림자(halo) 정의
        svg.append("<defs>")
                // 대각 소프트 그라데이션: primary → 중간 혼합색 → accent
                .append("<linearGradient id=\"bg\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\">")
                .append("<stop offset=\"0\" stop-color=\"").append(primaryColor).append("\"/>")
                .append("<stop offset=\"0.55\" stop-color=\"").append(midColor).append("\"/>")
                .append("<stop offset=\"1\" stop-color=\"").append(accentColor).append("\"/>")
                .append("</linearGradient>")
                // 상단에서 은은하게 번지는 빛(월페이퍼 느낌)
                .append("<radialGradient id=\"glow\" cx=\"0.3\" cy=\"0.12\" r=\"0.9\">")
                .append("<stop offset=\"0\" stop-color=\"#ffffff\" stop-opacity=\"0.18\"/>")
                .append("<stop offset=\"1\" stop-color=\"#ffffff\" stop-opacity=\"0\"/>")
                .append("</radialGradient>")
                // 글자 halo: 그라데이션 어느 지점에서도 읽히도록 부드러운 그림자
                .append("<filter id=\"ts\" x=\"-50%\" y=\"-50%\" width=\"200%\" height=\"200%\">")
                .append("<feDropShadow dx=\"0\" dy=\"0\" stdDeviation=\"1.1\" flood-color=\"")
                .append(halo).append("\" flood-opacity=\"0.45\"/>")
                .append("</filter>")
                .append("</defs>");

        // 배경(그라데이션) + 상단 글로우
        svg.append("<rect x=\"0\" y=\"0\" width=\"").append(WIDTH).append("\" height=\"").append(HEIGHT)
                .append("\" fill=\"url(#bg)\"/>");
        svg.append("<rect x=\"0\" y=\"0\" width=\"").append(WIDTH).append("\" height=\"").append(HEIGHT)
                .append("\" fill=\"url(#glow)\"/>");

        // 제목(히어로) + 저자 + 출판사 (모두 halo 그림자 적용)
        svg.append("<g filter=\"url(#ts)\">");
        svg.append(verticalText(titleText, centerX, titleTop,
                TITLE_FONT_SIZE, TITLE_LINE_HEIGHT, ink, null));
        if (!authorText.isEmpty()) {
            svg.append(verticalText(authorText, centerX, authorTop,
                    AUTHOR_FONT_SIZE, AUTHOR_LINE_HEIGHT, ink, "0.9"));
        }
        if (!publisherText.isEmpty()) {
            svg.append(verticalText(publisherText, centerX, pubTop,
                    PUBLISHER_FONT_SIZE, PUBLISHER_LINE_HEIGHT, ink, "0.9"));
        }
        svg.append("</g>");

        svg.append("</svg>");
        return svg.toString();
    }

    /** 글자를 눕히지 않고 한 글자씩 아래로 쌓는 세로쓰기 &lt;text&gt; 블록. topY는 첫 글자 칸의 위쪽 y. */
    private static String verticalText(String text, int centerX, int topY,
                                       int fontSize, int lineHeight, String fill, String fillOpacity) {
        StringBuilder sb = new StringBuilder();
        sb.append("<text fill=\"").append(fill).append("\"");
        if (fillOpacity != null) {
            sb.append(" fill-opacity=\"").append(fillOpacity).append("\"");
        }
        sb.append(" font-size=\"").append(fontSize)
                .append("\" font-weight=\"700\" font-family=\"'Noto Sans KR', sans-serif\" ")
                .append("text-anchor=\"middle\" dominant-baseline=\"central\">");
        for (int i = 0; i < text.length(); i++) {
            int y = topY + i * lineHeight + lineHeight / 2;
            sb.append("<tspan x=\"").append(centerX).append("\" y=\"").append(y).append("\">")
                    .append(escapeXml(String.valueOf(text.charAt(i)))).append("</tspan>");
        }
        sb.append("</text>");
        return sb.toString();
    }

    /** 두 hex 색을 비율 t(0=a, 1=b)로 선형 혼합한 hex를 반환한다. */
    private static String blend(String a, String b, double t) {
        try {
            String ha = a.startsWith("#") ? a.substring(1) : a;
            String hb = b.startsWith("#") ? b.substring(1) : b;
            int ra = Integer.parseInt(ha.substring(0, 2), 16);
            int ga = Integer.parseInt(ha.substring(2, 4), 16);
            int ba = Integer.parseInt(ha.substring(4, 6), 16);
            int rb = Integer.parseInt(hb.substring(0, 2), 16);
            int gb = Integer.parseInt(hb.substring(2, 4), 16);
            int bb = Integer.parseInt(hb.substring(4, 6), 16);
            int r = (int) Math.round(ra + (rb - ra) * t);
            int g = (int) Math.round(ga + (gb - ga) * t);
            int bl = (int) Math.round(ba + (bb - ba) * t);
            return String.format("#%02x%02x%02x", r, g, bl);
        } catch (Exception e) {
            return a;
        }
    }

    /** 배경색 밝기에 따라 잘 보이는 글자색(어두운 색/흰색)을 고른다. */
    private static String contrastText(String hex) {
        try {
            String h = hex.startsWith("#") ? hex.substring(1) : hex;
            int r = Integer.parseInt(h.substring(0, 2), 16);
            int g = Integer.parseInt(h.substring(2, 4), 16);
            int b = Integer.parseInt(h.substring(4, 6), 16);
            double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
            return luminance > 0.6 ? "#1f1f1f" : "#ffffff";
        } catch (Exception e) {
            return "#ffffff";
        }
    }

    private static String stripSpaces(String text) {
        return text == null ? "" : text.replaceAll("\\s+", "");
    }

    /** 최대 길이를 넘으면 말줄임표(…)로 자른다. */
    private static String cut(String text, int max) {
        if (text.length() <= max) {
            return text;
        }
        return max <= 1 ? text.substring(0, 1) : text.substring(0, max - 1) + "…";
    }

    private static String escapeXml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
