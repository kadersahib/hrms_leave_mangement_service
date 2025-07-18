package net.tetradtech.hrms_leave_service.Enum;

public enum DayOffType {
    LEAVE, FIRSTOFF, SECONDOFF;

    public static DayOffType fromString(String value) {
        return DayOffType.valueOf(value.toUpperCase());
    }
}

