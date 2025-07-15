package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.AttendanceDTO;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.model.AttendanceRecord;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.repository.AttendanceRepository;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final LocalTime OFFICE_START = LocalTime.of(10, 0);

    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private LeaveApplicationRepository leaveRepository;



    @Override
    public AttendanceDTO clockIn(Long userId) {
        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceRepository.findByUserIdAndDateAndIsDeletedFalse(userId, today)
                .orElse(new AttendanceRecord());

        if (record.getClockInTime() != null) throw new IllegalStateException("Already clocked in.");

        LocalDateTime now = LocalDateTime.now();
        record.setUserId(userId);
        record.setDate(today);
        record.setClockInTime(now);
        record.setLate(now.toLocalTime().isAfter(OFFICE_START));
        record.setAbsent(false);
        record.setDeleted(false);
        record.setWorkingDay(true);
        record.setStatus(now.toLocalTime().isAfter(OFFICE_START) ? "LATE" : "ONTIME");
        record.setSource("MANUAL");
        record.setCreatedBy("System");
        record.setCreatedAt(LocalDateTime.now());

        return mapToDTO(attendanceRepository.save(record));
    }

    @Override
    public AttendanceDTO clockOut(Long userId) {
        AttendanceRecord record = attendanceRepository.findByUserIdAndDateAndIsDeletedFalse(userId, LocalDate.now())
                .orElseThrow(() -> new IllegalStateException("Clock-in not found"));
        record.setClockOutTime(LocalDateTime.now());
        return mapToDTO(attendanceRepository.save(record));
    }

    @Scheduled(cron = "0 0 24 * * ?")
    public void autoMarkAbsentees() {
        LocalDate today = LocalDate.now();
        List<UserDTO> allUsers = userServiceClient.getAllUsers();
        for (UserDTO user : allUsers) {
            boolean exists = attendanceRepository.existsByUserIdAndDateAndIsDeletedFalse(user.getId(), today);
            if (!exists) {
                AttendanceRecord r = new AttendanceRecord();
                r.setUserId(user.getId());
                r.setDate(today);
                r.setStatus("ABSENT");
                r.setSource("AUTO");
                r.setCreatedBy("System");
                r.setCreatedAt(LocalDateTime.now());
                attendanceRepository.save(r);
            }
        }
    }



    @Override
    public List<AttendanceDTO> getAttendanceForCalendar(Long userId, int year, int month) {
        return attendanceRepository.findByUserIdAndIsDeletedFalse(userId).stream()
                .filter(r -> r.getDate().getYear() == year && r.getDate().getMonthValue() == month)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private AttendanceDTO mapToDTO(AttendanceRecord r) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(r.getId());
        dto.setUserId(r.getUserId());
        dto.setDate(r.getDate());
        dto.setClockInTime(r.getClockInTime());
        dto.setClockOutTime(r.getClockOutTime());
        dto.setLate(r.isLate());
        dto.setStatus(r.getStatus());
        dto.setAttendanceType(r.getAttendanceType());
        dto.setName(userServiceClient.getUserById(r.getUserId()).getName());
        return dto;
    }
    @Override
    public List<AttendanceDTO> getAllUserDailyLogs(LocalDate date) {
        return attendanceRepository.findByDateAndIsDeletedFalse(date).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AttendanceDTO getUserDailyLog(Long userId, LocalDate date) {
        AttendanceRecord record = attendanceRepository.findByUserIdAndDateAndIsDeletedFalse(userId, date)
                .orElseThrow(() -> new IllegalArgumentException("No record found"));
        return mapToDTO(record);
    }


    @Override
    public List<AttendanceDTO> getMonthlyCalendar(Long userId, int year, int month) {
        List<AttendanceDTO> calendarDays = new ArrayList<>();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        List<AttendanceRecord> attendanceRecords =
                attendanceRepository.findByUserIdAndDateBetween(userId, monthStart, monthEnd);

        List<LeaveApplication> approvedLeaves =
                leaveRepository.findApprovedLeavesByUserIdAndDateRange(userId, monthStart, monthEnd);

        UserDTO user = userServiceClient.getUserById(userId);

        for (LocalDate date = monthStart; !date.isAfter(monthEnd); date = date.plusDays(1)) {
            final LocalDate currentDate = date;

            AttendanceRecord record = attendanceRecords.stream()
                    .filter(r -> r.getDate().equals(currentDate))
                    .findFirst()
                    .orElse(null);

            AttendanceDTO dto = new AttendanceDTO();
            dto.setUserId(userId);
            dto.setDate(currentDate);
            dto.setName(user.getName());

            if (record != null) {
                dto.setId(record.getId());
                dto.setClockInTime(record.getClockInTime());
                dto.setClockOutTime(record.getClockOutTime());
                dto.setStatus(record.getStatus());
                dto.setLate(record.isLate());
                dto.setAttendanceType(record.getAttendanceType());
            } else {
                boolean isOnLeave = approvedLeaves.stream()
                        .anyMatch(l -> !currentDate.isBefore(l.getStartDate()) && !currentDate.isAfter(l.getEndDate()));

                dto.setStatus(isOnLeave ? "LEAVE" : "ABSENT");
            }

            calendarDays.add(dto);
        }

        return calendarDays;
    }

}
