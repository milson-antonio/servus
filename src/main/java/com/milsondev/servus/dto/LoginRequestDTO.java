package com.milsondev.servus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @NotBlank(message = "{validation.login.email.required}")
    @Email(message = "{validation.login.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.login.password.required}")
    private String password;
}