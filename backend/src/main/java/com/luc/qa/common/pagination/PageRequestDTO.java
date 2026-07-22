package com.luc.qa.common.pagination;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequestDTO {

    private int page = 0;
    private int size = 20;
}
