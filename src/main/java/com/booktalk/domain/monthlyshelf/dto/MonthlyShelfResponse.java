package com.booktalk.domain.monthlyshelf.dto;

import java.util.List;

public record MonthlyShelfResponse(
        String yearMonth,
        int bookCount,
        List<ShelfBookItem> books
) {
}
