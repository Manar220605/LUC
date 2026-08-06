package com.luc.qa.module.moderation.dto;

import com.luc.qa.module.moderation.entity.ResolutionAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResolveReportRequestDTO {

    @Size(max = 2000)
    private String resolutionNote;

    @NotNull
    private ResolutionAction action;
}
