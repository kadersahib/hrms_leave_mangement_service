package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.Enum.AttendanceSource;
import net.tetradtech.hrms_leave_service.Enum.AttendanceStatus;
import net.tetradtech.hrms_leave_service.Enum.AttendanceType;
import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.AttendanceDTO;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.model.AttendanceRecord;
import net.tetradtech.hrms_leave_service.repository.AttendanceRepository;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
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

        UserDTO user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Invalid user ID: " + userId);
        }

        LocalDate today = LocalDate.now();
        Optional<AttendanceRecord> existing = attendanceRepository
                .findByUserIdAndDateAndIsDeletedFalse(userId, today);
        if (existing.isPresent()) {
            throw new IllegalStateException("Already clocked in.");
        }

        LocalDateTime now = LocalDateTime.now();
        AttendanceRecord record = new AttendanceRecord();

        record.setUserId(userId);
        record.setDate(today);
        record.setClockInTime(now);
        record.setLate(now.toLocalTime().isAfter(OFFICE_START));
        record.setDeleted(false);
        record.setWorkingDay(true);
        record.setStatus(now.toLocalTime().isAfter(OFFICE_START) ? AttendanceStatus.LATE : AttendanceStatus.ONTIME);
        record.setSource(AttendanceSource.MANUAL);
        record.setCreatedBy("System");
        record.setCreatedAt(LocalDateTime.now());

        return mapToDTO(attendanceRepository.save(record));
    }


    @Override
    public AttendanceDTO clockOut(Long userId) {
        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceRepository.findByUserIdAndDateAndIsDeletedFalse(userId, today)
                .orElseThrow(() -> new IllegalStateException("Clock-in not found."));

        if (record.getClockOutTime() != null) {
            throw new IllegalStateException("Already clocked out.");
        }

        LocalDateTime now = LocalDateTime.now();
        record.setClockOutTime(now);

        if (record.getClockInTime() != null) {
            Duration duration = Duration.between(record.getClockInTime(), now);
            long hours = duration.toHours();

            if (hours >= 8) {
                record.setAttendanceType(AttendanceType.FULL_TIME);
            } else if (hours >= 4) {
                record.setAttendanceType(AttendanceType.HALF_DAY);
            } else {
                record.setAttendanceType(AttendanceType.SHORT_HOURS);
            }
        }

        record.setUpdatedAt(LocalDateTime.now());
        record.setUpdatedBy("System");

        return mapToDTO(attendanceRepository.save(record));
    }


    @Scheduled(cron = "0 0 18 * * ?")
    public void autoMarkAbsentees() {
        LocalDate today = LocalDate.now();
        List<UserDTO> allUsers = userServiceClient.getAllUsers();
        for (UserDTO user : allUsers) {
            boolean exists = attendanceRepository.existsByUserIdAndDateAndIsDeletedFalse(user.getId(), today);
            if (!exists) {
                AttendanceRecord r = new AttendanceRecord();
                r.setUserId(user.getId());
                r.setDate(today);
                r.setStatus(AttendanceStatus.ABSENT);
                r.setSource(AttendanceSource.AUTO);
                r.setCreatedBy("System");
                r.setCreatedAt(LocalDateTime.now());
                attendanceRepository.save(r);
            }
        }
    }



    @Override
    public List<AttendanceDTO> getAllAttendanceRecords() {
        return attendanceRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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

    private AttendanceDTO mapToDTO(AttendanceRecord r) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(r.getId());
        dto.setUserId(r.getUserId());
        dto.setDate(r.getDate());
        dto.setClockInTime(r.getClockInTime());
        dto.setClockOutTime(r.getClockOutTime());
        dto.setLate(r.isLate());

        // Safely handle enum to string conversion
        dto.setStatus(r.getStatus() != null ? r.getStatus().name() : null);
        dto.setAttendanceType(r.getAttendanceType() != null ? r.getAttendanceType().name() : null);

        // Avoid NullPointerException if user is not found
        UserDTO user = userServiceClient.getUserById(r.getUserId());
        dto.setName(user != null ? user.getName() : "Unknown");

        return dto;
    }





}
