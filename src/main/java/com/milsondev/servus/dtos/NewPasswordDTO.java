package com.milsondev.servus.dtos;

import com.milsondev.servus.dtos.validator.NewPasswordDTOValidator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@NewPasswordDTOValidator
public class NewPasswordDTO {
    @NotBlank(message = "{validation.reset.token.required}")
    private String token;

    @NotBlank(message = "{validation.reset.password.required}")
    @Size(min = 1, message = "{validation.reset.password.size}")
    private String password;

    @NotBlank(message = "{validation.reset.confirm.required}")
    private String confirmPassword;
}