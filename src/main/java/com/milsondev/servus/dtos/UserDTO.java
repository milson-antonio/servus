package com.milsondev.servus.dtos;

import com.milsondev.servus.dtos.validator.UserDTOValidator;
import com.milsondev.servus.enums.Role;
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
    private Role role;
}