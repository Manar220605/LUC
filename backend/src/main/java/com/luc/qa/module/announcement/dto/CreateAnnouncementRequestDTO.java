package com.luc.qa.module.announcement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAnnouncementRequestDTO {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 20000)
    private String body;

    private Boolean published;
}
