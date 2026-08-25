package com.luc.qa.module.mentorship.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMentorshipRequestDTO {

    @NotNull
    private Long alumniUserId;

    @NotBlank
    @Size(min = 10, max = 500)
    private String message;
}
