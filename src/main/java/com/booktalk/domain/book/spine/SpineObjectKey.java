package com.booktalk.domain.book.spine;

/**
 * 책등 이미지의 저장 키(경로)를 bookId로부터 결정적으로 계산한다.
 * 모든 저장소 구현(Local/S3/R2)이 동일한 키를 쓰도록 이 헬퍼로 일원화한다.
 *
 * 한 폴더에 모든 파일이 쌓이면 콘솔 탐색/관리가 어려워지므로, bookId를 6자리로 0-패딩한 뒤
 * 앞 3자리 / 뒤 3자리를 디렉터리로 사용해 2단계로 분산 저장한다.
 *
 *   bookId=6      -> spines/000/006/6.svg
 *   bookId=12345  -> spines/012/345/12345.svg
 *   bookId=1234567-> spines/123/456/1234567.svg  (6자리 초과분은 파일명에만 남음)
 *
 * 파일명은 항상 전체 bookId라 샤드가 겹쳐도 충돌하지 않으며, DB 조회 없이 경로를 재계산할 수 있다.
 */
final class SpineObjectKey {

    private static final String PREFIX = "spines";

    private SpineObjectKey() {
    }

    /** 예: "spines/012/345/12345.svg" */
    static String forBook(Long bookId) {
        String padded = String.format("%06d", bookId);
        String shard1 = padded.substring(0, 3);
        String shard2 = padded.substring(3, 6);
        return PREFIX + "/" + shard1 + "/" + shard2 + "/" + bookId + ".svg";
    }
}
