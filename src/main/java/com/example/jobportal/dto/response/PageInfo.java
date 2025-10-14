package com.example.jobportal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public  class PageInfo {
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public static PageInfo of(int page, int size, long total) {
        int totalPages = (int) Math.ceil((double) total / size);
        return new PageInfo(
                page,
                size,
                total,
                totalPages,
                page < totalPages - 1,
                page > 0
        );
    }
}

