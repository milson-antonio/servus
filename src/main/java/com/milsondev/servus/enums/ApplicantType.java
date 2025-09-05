package com.milsondev.servus.enums;

public enum ApplicantType {
    SELF,
    OTHER;

    public static ApplicantType fromInput(String input) {
        if (input == null) {
            return null;
        }
        for (ApplicantType type : values()) {
            if (type.name().equalsIgnoreCase(input.trim())) {
                return type;
            }
        }
        return null;
    }
}
