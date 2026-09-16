package com.booktalk.domain.book.spine;

/**
 * 책등처럼 보이는 세로형 SVG를 생성한다.
 *
 * - buildFromCover: 표지 이미지를 세로 단면으로 임베드해 실제 표지와 비슷하게 보이도록 한다(기본).
 * - buildFromColors: 표지가 없거나 임베드에 실패했을 때 쓰는 색상 기반 폴백.
 */
public final class SpineSvgBuilder {

    private static final int WIDTH = 60;
    private static final int HEIGHT = 320;
    private static final int STRIPE_HEIGHT = 10;
    private static final int TITLE_MAX_LENGTH = 24;

    private SpineSvgBuilder() {
    }

    /**
     * 표지 세로 슬라이스를 배경으로 깐 책등.
     * @param coverDataUri CoverImageEncoder가 만든 "data:image/jpeg;base64,..." 형태의 표지 단면
     */
    public static String buildFromCover(String title, String coverDataUri) {
        String safeTitle = escapeXml(truncate(title, TITLE_MAX_LENGTH));
        int centerX = WIDTH / 2;

        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(WIDTH)
                .append("\" height=\"").append(HEIGHT)
                .append("\" viewBox=\"0 0 ").append(WIDTH).append(" ").append(HEIGHT).append("\">");

        // 제목 가독성용: 세로 중앙에 어두운 스크림(그라데이션), 글자엔 드롭섀도
        svg.append("<defs>")
                .append("<linearGradient id=\"scrim\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"0\">")
                .append("<stop offset=\"0\" stop-color=\"#000\" stop-opacity=\"0\"/>")
                .append("<stop offset=\"0.5\" stop-color=\"#000\" stop-opacity=\"0.45\"/>")
                .append("<stop offset=\"1\" stop-color=\"#000\" stop-opacity=\"0\"/>")
                .append("</linearGradient>")
                .append("<filter id=\"ds\" x=\"-50%\" y=\"-50%\" width=\"200%\" height=\"200%\">")
                .append("<feDropShadow dx=\"0\" dy=\"0\" stdDeviation=\"1.2\" flood-color=\"#000\" flood-opacity=\"0.75\"/>")
                .append("</filter>")
                .append("</defs>");

        // 표지 세로 단면(이미 책등 비율로 크롭되어 있으므로 그대로 채운다)
        svg.append("<image href=\"").append(coverDataUri)
                .append("\" x=\"0\" y=\"0\" width=\"").append(WIDTH).append("\" height=\"").append(HEIGHT)
                .append("\" preserveAspectRatio=\"xMidYMid slice\" />");

        svg.append("<rect x=\"0\" y=\"0\" width=\"").append(WIDTH).append("\" height=\"").append(HEIGHT)
                .append("\" fill=\"url(#scrim)\" />");

        int centerY = HEIGHT / 2;
        svg.append("<text x=\"").append(centerX).append("\" y=\"").append(centerY).append("\" fill=\"#ffffff\" ")
                .append("font-size=\"13\" font-weight=\"600\" font-family=\"'Noto Sans KR', sans-serif\" ")
                .append("text-anchor=\"middle\" dominant-baseline=\"central\" filter=\"url(#ds)\" ")
                .append("transform=\"rotate(90 ").append(centerX).append(" ").append(centerY).append(")\">")
                .append(safeTitle).append("</text>");

        svg.append("</svg>");
        return svg.toString();
    }

    /** 색상 기반 폴백: 배경색(primary) + 위아래 띠(accent) + 세로 제목. */
    public static String buildFromColors(String title, String primaryColor, String accentColor) {
        String safeTitle = escapeXml(truncate(title, TITLE_MAX_LENGTH));
        int stripeY = HEIGHT - STRIPE_HEIGHT;
        int centerX = WIDTH / 2;

        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(WIDTH)
                .append("\" height=\"").append(HEIGHT)
                .append("\" viewBox=\"0 0 ").append(WIDTH).append(" ").append(HEIGHT).append("\">");

        svg.append("<rect x=\"0\" y=\"0\" width=\"").append(WIDTH).append("\" height=\"").append(HEIGHT)
                .append("\" fill=\"").append(primaryColor).append("\" />");

        svg.append("<rect x=\"0\" y=\"0\" width=\"").append(WIDTH).append("\" height=\"").append(STRIPE_HEIGHT)
                .append("\" fill=\"").append(accentColor).append("\" />");

        svg.append("<rect x=\"0\" y=\"").append(stripeY).append("\" width=\"").append(WIDTH)
                .append("\" height=\"").append(STRIPE_HEIGHT).append("\" fill=\"").append(accentColor).append("\" />");

        int centerY = HEIGHT / 2;
        svg.append("<text x=\"").append(centerX).append("\" y=\"").append(centerY).append("\" fill=\"#ffffff\" fill-opacity=\"0.92\" ")
                .append("font-size=\"13\" font-family=\"'Noto Sans KR', sans-serif\" text-anchor=\"middle\" dominant-baseline=\"central\" ")
                .append("transform=\"rotate(90 ").append(centerX).append(" ").append(centerY).append(")\">")
                .append(safeTitle).append("</text>");

        svg.append("</svg>");
        return svg.toString();
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "…" : text;
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
