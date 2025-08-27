package com.milsondev.servus.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequestDTO {
    @NotBlank(message = "{validation.reset.email.required}")
    @Email(message = "{validation.reset.email.invalid}")
    private String email;
}