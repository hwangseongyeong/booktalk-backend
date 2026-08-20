package com.booktalk.domain.book.spine;

/**
 * 책등처럼 보이는 세로형 SVG를 직접 생성한다.
 * 배경색(primaryColor) + 위아래 띠(accentColor) + 세로로 회전된 제목 텍스트.
 */
public final class SpineSvgBuilder {

    private static final int WIDTH = 60;
    private static final int HEIGHT = 320;
    private static final int STRIPE_HEIGHT = 10;
    private static final int TITLE_MAX_LENGTH = 24;

    private SpineSvgBuilder() {
    }

    public static String build(String title, String primaryColor, String accentColor) {
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

        svg.append("<text x=\"").append(centerX).append("\" y=\"24\" fill=\"#ffffff\" fill-opacity=\"0.92\" ")
                .append("font-size=\"13\" font-family=\"'Noto Sans KR', sans-serif\" text-anchor=\"middle\" ")
                .append("transform=\"rotate(90 ").append(centerX).append(" 24)\">")
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
