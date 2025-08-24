package com.milsondev.servus.dto;

import com.milsondev.servus.dto.validator.UserDTOValidator;
import lombok.Data;

import java.util.UUID;

@UserDTOValidator
@Data
public class UserDTO {
    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String confirmPassword;
}