package com.teamroute.saferoad.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStateDTO {

    // "processing", "completed", "non_processed" 중 하나
    @NotBlank
    private String state;
}