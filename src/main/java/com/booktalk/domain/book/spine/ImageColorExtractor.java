package com.booktalk.domain.book.spine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 표지 이미지에서 대표 색상(primary)과 그와 구분되는 보조 색상(accent)을 뽑아낸다.
 * 외부 이미지 라이브러리 없이 ImageIO + 색상 양자화(quantization)만으로 구현한 MVP 버전.
 * 정교한 팔레트 추출(예: Vibrant 알고리즘)이 필요해지면 이 클래스만 교체하면 된다.
 */
public final class ImageColorExtractor {

    private static final int SAMPLE_SIZE = 40; // 다운스케일해서 분석할 그리드 크기
    private static final int QUANTIZE_STEP = 32; // 채널당 양자화 단위 (256/32 = 8단계)
    private static final double MIN_ACCENT_DISTANCE = 60.0; // primary와 이만큼 떨어져야 accent 후보

    private ImageColorExtractor() {
    }

    public static ExtractedColors extract(byte[] imageBytes) throws IOException {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (original == null) {
            throw new IOException("이미지를 읽을 수 없습니다.");
        }

        BufferedImage sample = resize(original, SAMPLE_SIZE, SAMPLE_SIZE);
        Map<Integer, Integer> histogram = buildHistogram(sample);

        int primary = histogram.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0x808080);

        int accent = histogram.entrySet().stream()
                .filter(e -> colorDistance(e.getKey(), primary) > MIN_ACCENT_DISTANCE)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseGet(() -> darken(primary));

        return new ExtractedColors(toHex(primary), toHex(accent));
    }

    private static Map<Integer, Integer> buildHistogram(BufferedImage sample) {
        Map<Integer, Integer> histogram = new HashMap<>();
        for (int y = 0; y < sample.getHeight(); y++) {
            for (int x = 0; x < sample.getWidth(); x++) {
                int quantized = quantize(sample.getRGB(x, y));
                histogram.merge(quantized, 1, Integer::sum);
            }
        }
        return histogram;
    }

    private static BufferedImage resize(BufferedImage source, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        var g = resized.createGraphics();
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }

    private static int quantize(int rgb) {
        int r = ((rgb >> 16) & 0xFF) / QUANTIZE_STEP * QUANTIZE_STEP;
        int g = ((rgb >> 8) & 0xFF) / QUANTIZE_STEP * QUANTIZE_STEP;
        int b = (rgb & 0xFF) / QUANTIZE_STEP * QUANTIZE_STEP;
        return (r << 16) | (g << 8) | b;
    }

    private static double colorDistance(int a, int b) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return Math.sqrt(Math.pow(ar - br, 2) + Math.pow(ag - bg, 2) + Math.pow(ab - bb, 2));
    }

    private static int darken(int rgb) {
        int r = (int) (((rgb >> 16) & 0xFF) * 0.7);
        int g = (int) (((rgb >> 8) & 0xFF) * 0.7);
        int b = (int) ((rgb & 0xFF) * 0.7);
        return (r << 16) | (g << 8) | b;
    }

    private static String toHex(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }

    public record ExtractedColors(String primaryColor, String accentColor) {
    }
}
