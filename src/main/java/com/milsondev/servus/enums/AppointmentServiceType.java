package com.milsondev.servus.enums;

public enum AppointmentServiceType {
    passportRenewal("Passport Renewal"),
    visaApplication("Visa Application"),
    consularRegistration("Consular Registration"),
    documentLegalization("Document Legalization"),
    notarialServices("Notarial Services"),
    emergencyServices("Emergency Services");

    private final String label;

    AppointmentServiceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static AppointmentServiceType fromInput(String input) {
        if (input == null) return null;
        String norm = normalize(input);
        // Try exact enum name
        for (AppointmentServiceType t : values()) {
            if (t.name().equalsIgnoreCase(input.trim())) return t;
        }
        // Try normalized against known tokens
        for (AppointmentServiceType t : values()) {
            if (normalize(t.name()).equals(norm)) return t;
            if (normalize(t.getLabel()).equals(norm)) return t;
        }
        // Some common camelCase variants
        if (norm.equals("passportRenewal")) return passportRenewal;
        if (norm.equals("visaApplication")) return visaApplication;
        if (norm.equals("consularRegistration")) return consularRegistration;
        if (norm.equals("documentLegalization")) return documentLegalization;
        if (norm.equals("notarialServices")) return notarialServices;
        if (norm.equals("emergencyServices")) return emergencyServices;
        return null;
    }

    private static String normalize(String v) {
        String s = v == null ? "" : v.trim();
        // Replace non-letters/numbers with nothing and lower
        s = s.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        // Also remove underscores for enum names
        s = s.replace("_", "");
        return s;
    }
}
