package com.luc.qa.module.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnreadCountResponseDTO {

    private long count;
}
