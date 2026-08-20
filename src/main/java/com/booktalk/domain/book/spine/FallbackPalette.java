package com.booktalk.domain.book.spine;

/**
 * 표지 이미지가 없거나(직접 등록) 색상 추출에 실패한 경우를 위한 기본 팔레트.
 * 책 제목을 시드로 고정 인덱스를 뽑아서, 같은 책은 항상 같은 색이 나오게 한다.
 */
public final class FallbackPalette {

    private static final String[][] PALETTE = {
            {"#8B5E3C", "#6B4526"},
            {"#4A6C6F", "#2F4A4D"},
            {"#7A6C5D", "#564B3F"},
            {"#5B6B8C", "#3D4A63"},
            {"#8C5B6B", "#63404A"},
            {"#6B8C5B", "#4A6340"},
            {"#8C7A5B", "#63563E"},
            {"#5B7A8C", "#3E5663"},
    };

    private FallbackPalette() {
    }

    public static ImageColorExtractor.ExtractedColors pick(String seed) {
        int index = Math.floorMod(seed == null ? 0 : seed.hashCode(), PALETTE.length);
        return new ImageColorExtractor.ExtractedColors(PALETTE[index][0], PALETTE[index][1]);
    }
}
