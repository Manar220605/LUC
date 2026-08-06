package com.luc.qa.module.alumni.dto;

import com.luc.qa.module.alumni.entity.VerificationStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminVerificationFilterDTO {

    private VerificationStatus status;
    private int page = 0;
    private int size = 20;
}
