package net.tetradtech.hrms_leave_service.util;

public class LeaveTypeUtil {
    private static final int SICK_LEAVE = 30;
    private static final int PERSONAL_LEAVE = 20;

    public static final String MATERNITY = "MATERNITY";
    public static final String PATERNITY = "PATERNITY";
    public static final int MAX_MATERNITY_DAYS = 120;
    public static final int MAX_PATERNITY_DAYS = 20;

    public static int getMaxDays(Long leaveTypeId) {
        if (leaveTypeId == 1L) {
            return SICK_LEAVE;
        } else if (leaveTypeId == 2L) {
            return PERSONAL_LEAVE;
        } else if (leaveTypeId == 3L) {
            return MAX_MATERNITY_DAYS;
        } else if (leaveTypeId == 4L) {
            return MAX_PATERNITY_DAYS;
        } else if (leaveTypeId == 5L) {
            return 10; // Example: Max 10 days for "Others"
        } else {
            throw new IllegalArgumentException("Unsupported leave type ID: " + leaveTypeId);
        }
    }


    public static String getLeaveTypeName(Long leaveTypeId) {
        if (leaveTypeId == 1L) {
            return "Sick Leave";
        } else if (leaveTypeId == 2L) {
            return "Personal Leave";
        } else if (leaveTypeId == 3L) {
            return "Maternity";
        } else if (leaveTypeId == 4L) {
            return "Paternity";
        } else if (leaveTypeId == 5L) {
            return "Others";
        } else {
            return "Unknown";
        }
    }


    public static boolean isSupported(Long leaveTypeId) {
        return leaveTypeId != 3L && leaveTypeId != 3L;
    }
}
