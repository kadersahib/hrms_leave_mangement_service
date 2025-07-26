package net.tetradtech.hrms_leave_service.constants;

public enum DayOffType {
    LEAVE, FIRSTOFF, SECONDOFF;

    public static DayOffType fromString(String value) {
        if (value == null) return null;
        try {
            return DayOffType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid DayOffType: " + value);
        }
    }
}
