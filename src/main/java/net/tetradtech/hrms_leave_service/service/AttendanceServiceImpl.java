package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.AttendanceDTO;
import net.tetradtech.hrms_leave_service.dto.AttendanceSummaryDTO;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.model.AttendanceRecord;
import net.tetradtech.hrms_leave_service.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final LocalTime OFFICE_START = LocalTime.of(9, 30);
    private static final LocalTime ABSENT_CUTOFF = LocalTime.of(18, 0);

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Override
    public AttendanceDTO clockIn(Long userId) {
        UserDTO user = userServiceClient.getUserById(userId);
        if (user == null) {
            System.out.println(" No user found with ID: " + userId);

            throw new IllegalArgumentException("Invalid user ID: " + userId);
        }
        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceRepository.findByUserIdAndDateAndIsDeletedFalse(userId, today)
                .orElse(new AttendanceRecord());

        if (record.getClockInTime() != null) {
            throw new IllegalStateException("User already clocked in.");
        }

        LocalDateTime now = LocalDateTime.now();
        record.setUserId(userId);
        record.setDate(today);
        record.setClockInTime(now);
        record.setCreatedBy("System");
        record.setLate(now.toLocalTime().isAfter(OFFICE_START));
        record.setAbsent(false);

        return mapToDTO(attendanceRepository.save(record));
    }

    @Override
    public AttendanceDTO clockOut(Long userId) {
        UserDTO user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Invalid user ID: " + userId);
        }

        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceRepository.findByUserIdAndDateAndIsDeletedFalse(userId, today)
                .orElseThrow(() -> new IllegalArgumentException("No clock-in record found"));

        record.setClockOutTime(LocalDateTime.now());

        return mapToDTO(attendanceRepository.save(record));
    }




    @Override
    public AttendanceSummaryDTO getMonthlySummary(Long userId, int year, int month) {
        List<AttendanceRecord> records = attendanceRepository
                .findByUserIdAndIsDeletedFalse(userId).stream()
                .filter(r -> r.getDate().getYear() == year && r.getDate().getMonthValue() == month)
                .toList();

        int totalPresent = (int) records.stream().filter(r -> !r.isAbsent()).count();
        int totalAbsent = (int) records.stream().filter(AttendanceRecord::isAbsent).count();
        int totalLate = (int) records.stream().filter(r -> r.isLate() && !r.isAbsent()).count();

        return new AttendanceSummaryDTO(userId, totalPresent, totalAbsent, totalLate);
    }
    @Override
    public List<AttendanceSummaryDTO> getMonthlySummaryForAllUsers(int year, int month) {
        List<UserDTO> allUsers = userServiceClient.getAllUsers();

        return allUsers.stream().map(user -> {
            List<AttendanceRecord> records = attendanceRepository
                    .findByUserIdAndIsDeletedFalse(user.getId()).stream()
                    .filter(r -> r.getDate().getYear() == year && r.getDate().getMonthValue() == month)
                    .toList();

            int totalPresent = (int) records.stream().filter(r -> !r.isAbsent()).count();
            int totalAbsent = (int) records.stream().filter(AttendanceRecord::isAbsent).count();
            int totalLate = (int) records.stream().filter(r -> r.isLate() && !r.isAbsent()).count();

            return new AttendanceSummaryDTO(user.getId(), totalPresent, totalAbsent, totalLate);
        }).collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDTO> getDailyLogs(Long userId) {
        return attendanceRepository.findByUserIdAndIsDeletedFalse(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDTO> getAllDailyLogs() {
        return attendanceRepository.findByIsDeletedFalse().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    private AttendanceDTO mapToDTO(AttendanceRecord record) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(record.getId());
        dto.setUserId(record.getUserId());
        dto.setDate(record.getDate());
        dto.setClockInTime(record.getClockInTime());
        dto.setClockOutTime(record.getClockOutTime());
        dto.setLate(record.isLate());
        dto.setAbsent(record.isAbsent());

        UserDTO user = userServiceClient.getUserById(record.getUserId());
        dto.setName(user != null ? user.getName() : "Unknown");

        return dto;
    }
}
