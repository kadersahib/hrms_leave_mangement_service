package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.Enum.*;
import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.AttendanceDTO;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.mapper.AttendanceMapper;
import net.tetradtech.hrms_leave_service.model.AttendanceRecord;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.repository.AttendanceRepository;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final LocalTime OFFICE_START = LocalTime.of(10, 0);

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AttendanceMapper attendanceMapper;

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

        // Step 1: Check if the user is on approved leave today
        List<LeaveApplication> leaves = leaveApplicationRepository
                .findApprovedLeavesForToday(userId, today, LeaveStatus.APPROVED);

        DayOffType dayOffType = DayOffType.FULLDAY; // Default

        for (LeaveApplication leave : leaves) {
            dayOffType = leave.getDayOffType(); // Use leave day type from leave application

            if (dayOffType == DayOffType.FULLDAY) {
                throw new IllegalStateException("Cannot clock in. Full day leave is approved.");
            }

            LocalTime now = LocalTime.now();

            if (dayOffType == DayOffType.FIRSTOFF && now.isBefore(LocalTime.NOON)) {
                throw new IllegalStateException("Cannot clock in during First Off. Leave is approved.");
            }

            if (dayOffType == DayOffType.SECONDOFF && now.isAfter(LocalTime.NOON)) {
                throw new IllegalStateException("Cannot clock in during Second Off. Leave is approved.");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LocalTime clockInTime = now.toLocalTime();

        AttendanceRecord record = new AttendanceRecord();
        record.setUserId(userId);
        record.setDate(today);
        record.setClockInTime(now);
        record.setLate(clockInTime.isAfter(OFFICE_START));
        record.setDeleted(false);
        record.setWorkingDay(true);
        record.setStatus(clockInTime.isAfter(OFFICE_START) ? AttendanceStatus.LATE : AttendanceStatus.ONTIME);
        record.setSource(AttendanceSource.MANUAL);
        record.setCreatedBy("System");
        record.setCreatedAt(now);
        record.setDayOffType(dayOffType);

        return attendanceMapper.mapToDTO(attendanceRepository.save(record));
    }

    @Override
    public AttendanceDTO clockOut(Long userId) {
        UserDTO user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Invalid user ID: " + userId);
        }

        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceRepository.findByUserIdAndDateAndIsDeletedFalse(userId, today)
                .orElseThrow(() -> new IllegalStateException("No clock-in record found for today."));

        if (record.getClockOutTime() != null) {
            throw new IllegalStateException("Already clocked out.");
        }

        LocalDateTime now = LocalDateTime.now();
        record.setClockOutTime(now);
        record.setUpdatedAt(now);
        record.setUpdatedBy("System");

        return attendanceMapper.mapToDTO(attendanceRepository.save(record));
    }




    @Scheduled(cron = "0 0 20 * * ?")
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
                .map(attendanceMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDTO> getAllUserDailyLogs(LocalDate date) {
        return attendanceRepository.findByDateAndIsDeletedFalse(date).stream()
                .map(attendanceMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AttendanceDTO getUserDailyLog(Long userId, LocalDate date) {
        AttendanceRecord record = attendanceRepository.findByUserIdAndDateAndIsDeletedFalse(userId, date)
                .orElseThrow(() -> new IllegalArgumentException("No record found"));
        return attendanceMapper.mapToDTO(record);
    }

    @Override
    public int getDailyPresentCount(LocalDate date) {
        List<AttendanceStatus> presentStatuses = Arrays.asList(AttendanceStatus.ONTIME, AttendanceStatus.LATE);
        return attendanceRepository.countByDateAndIsDeletedFalseAndStatusIn(date, presentStatuses);
    }

    @Override
    public void deleteRecentAttendanceByUserId(Long userId) {
        Optional<AttendanceRecord> optionalRecord =
                attendanceRepository.findTopByUserIdAndIsDeletedFalseOrderByDateDesc(userId);

        if (optionalRecord.isEmpty()) {
            throw new IllegalArgumentException("No attendance record found for user ID: " + userId);
        }

        AttendanceRecord record = optionalRecord.get();
        record.setDeleted(true);
        record.setUpdatedAt(LocalDateTime.now());
        record.setUpdatedBy("System");

        attendanceRepository.save(record);
    }


}
