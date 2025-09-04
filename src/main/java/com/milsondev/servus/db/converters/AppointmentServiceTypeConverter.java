package com.milsondev.servus.db.converters;

import com.milsondev.servus.enums.AppointmentServiceType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AppointmentServiceTypeConverter implements AttributeConverter<AppointmentServiceType, String> {
    @Override
    public String convertToDatabaseColumn(AppointmentServiceType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public AppointmentServiceType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        // Accept legacy values like "passportRenewal" etc.
        AppointmentServiceType parsed = AppointmentServiceType.fromInput(dbData);
        if (parsed != null) return parsed;
        // As a last resort, try valueOf to throw a clear error
        return AppointmentServiceType.valueOf(dbData);
    }
}
