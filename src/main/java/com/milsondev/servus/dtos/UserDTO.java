package com.milsondev.servus.dtos;

import com.milsondev.servus.dtos.validation.UserDTOValidator;
import com.milsondev.servus.enums.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@UserDTOValidator
@Data
public class UserDTO {
    private UUID id;

    @NotBlank(message = "{validation.signup.firstName.required}")
    private String firstName;

    @NotBlank(message = "{validation.signup.lastName.required}")
    private String lastName;

    private String email;
    private String phone;
    private String password;
    private String confirmPassword;
    private Role role;
    private String dateOfBirth;
    private String nationality;
    private String passportNumber;
}
