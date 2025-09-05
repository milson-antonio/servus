package com.milsondev.servus.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtherPersonDetailsDTO {

    @NotBlank(message = "{validation.other.firstName.required}")
    private String otherFirstName;

    @NotBlank(message = "{validation.other.lastName.required}")
    private String otherLastName;

    private String otherDob;
    private String otherNationality;
    private String otherPassportNumber;
    private String otherEmail;
    private String otherPhone;

    // To carry over the service selection
    private String service;
}
