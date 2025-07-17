package net.tetradtech.hrms_leave_service.Enum;

public enum DayOffType {
    FULLDAY,
    FIRSTOFF,
    SECONDOFF;

    public static DayOffType fromString(String value) {
        if (value == null) return null;
        try {
            return DayOffType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // or throw custom exception
        }
    }
}
