package com.booktalk.domain.book.spine;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * 표지 이미지를 책등 비율(세로 단면)로 중앙 크롭·축소한 뒤 JPEG data URI로 인코딩한다.
 * 이 data URI를 SVG에 그대로 임베드하면 "표지의 세로 슬라이스"처럼 보이는 책등이 된다.
 *
 * 책등 원본(60x320)의 2배 해상도(120x640)로 인코딩해 고DPI 화면에서도 선명하게 보이도록 한다.
 */
final class CoverImageEncoder {

    private static final int OUT_WIDTH = 120;
    private static final int OUT_HEIGHT = 640;

    private CoverImageEncoder() {
    }

    /** 예: "data:image/jpeg;base64,/9j/4AAQ..." */
    static String toSpineSliceDataUri(byte[] imageBytes) throws IOException {
        BufferedImage src = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (src == null) {
            throw new IOException("표지 이미지를 디코드할 수 없습니다.");
        }

        BufferedImage slice = cropCenterToAspect(src, OUT_WIDTH, OUT_HEIGHT);
        BufferedImage scaled = scale(slice, OUT_WIDTH, OUT_HEIGHT);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (!ImageIO.write(scaled, "jpeg", out)) {
            throw new IOException("JPEG 인코딩에 실패했습니다.");
        }
        return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
    }

    /** 소스에서 targetW:targetH 비율의 세로 단면을 중앙 기준으로 잘라낸다. */
    private static BufferedImage cropCenterToAspect(BufferedImage src, int targetW, int targetH) {
        double targetAspect = (double) targetW / targetH;
        int srcW = src.getWidth();
        int srcH = src.getHeight();

        int cropW = (int) Math.round(srcH * targetAspect);
        int cropH = srcH;
        if (cropW > srcW) { // 세로가 충분히 길지 않으면 가로 기준으로 자른다
            cropW = srcW;
            cropH = (int) Math.round(srcW / targetAspect);
        }
        int cropX = Math.max(0, (srcW - cropW) / 2);
        int cropY = Math.max(0, (srcH - cropH) / 2);
        cropW = Math.min(cropW, srcW - cropX);
        cropH = Math.min(cropH, srcH - cropY);
        return src.getSubimage(cropX, cropY, cropW, cropH);
    }

    private static BufferedImage scale(BufferedImage src, int w, int h) {
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB); // JPEG는 알파 미지원 → RGB
        var g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
        g.dispose();
        return dst;
    }
}
