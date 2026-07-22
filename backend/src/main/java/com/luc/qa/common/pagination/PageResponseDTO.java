package com.luc.qa.common.pagination;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PageResponseDTO<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
