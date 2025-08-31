package com.milsondev.servus.dtos;

import com.milsondev.servus.dtos.validation.NewPasswordDTOValidator;
import lombok.Data;

@Data
@NewPasswordDTOValidator
public class NewPasswordDTO {
    private String token;
    private String password;
    private String confirmPassword;
}