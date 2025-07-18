package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.Enum.*;
import net.tetradtech.hrms_leave_service.client.DesignationClient;
import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.AttendanceDTO;
import net.tetradtech.hrms_leave_service.dto.DesignationDTO;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.mapper.AttendanceMapper;
import net.tetradtech.hrms_leave_service.model.AttendanceRecord;
import net.tetradtech.hrms_leave_service.repository.AttendanceRepository;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final LocalTime OFFICE_START = LocalTime.of(10, 0);

    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private DesignationClient designationClient;
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
        LocalDateTime now = LocalDateTime.now();

        Optional<AttendanceRecord> existing = attendanceRepository.findByUserIdAndDate(userId, today);
        if (existing.isPresent()) {
            throw new IllegalStateException("Already clocked in for today.");
        }

        DesignationDTO designation = designationClient.getDesignationById(user.getDesignation());
        if (designation == null) {
            throw new IllegalArgumentException("Invalid designation ID for user.");
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setUserId(userId);
        record.setDate(today);
        record.setClockInTime(now);
        record.setLate(now.toLocalTime().isAfter(OFFICE_START));
        record.setStatus(now.toLocalTime().isAfter(OFFICE_START) ? AttendanceStatus.LATE : AttendanceStatus.ONTIME);
        record.setSource(AttendanceSource.MANUAL);
        record.setDesignation(designation.getTitle());
        record.setCreatedBy("SYSTEM");
        record.setCreatedAt(now);
        record.setWorkingDay(true);
        record.setDeleted(false);

        AttendanceRecord savedRecord = attendanceRepository.save(record);
        return attendanceMapper.mapToDTO(savedRecord); // Ensure your mapper includes the 'designation' field
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

    @Scheduled(cron = "0 0 18 * * ?")
    public void autoMarkAbsentees() {
        LocalDate today = LocalDate.now();
        List<UserDTO> allUsers = userServiceClient.getAllUsers();
        for (UserDTO user : allUsers) {
            boolean exists = attendanceRepository.existsByUserIdAndDateAndIsDeletedFalse(user.getId(), today);
            if (!exists) {
                AttendanceRecord record = new AttendanceRecord();
                record.setUserId(user.getId());
                record.setDate(today);
                record.setStatus(AttendanceStatus.ABSENT);
                record.setSource(AttendanceSource.AUTO);
                record.setCreatedBy("System");
                record.setCreatedAt(LocalDateTime.now());
                attendanceRepository.save(record);
            }
        }
    }


    @Override
    public List<AttendanceDTO> getAllUserDailyLogs(LocalDate date) {
        List<AttendanceRecord> records = attendanceRepository.findByDateAndIsDeletedFalse(date);
        List<AttendanceDTO> result = new ArrayList<>();

        for (AttendanceRecord record : records) {
            AttendanceDTO dto = attendanceMapper.mapToDTO(record);

            UserDTO user = userServiceClient.getUserById(record.getUserId());
            if (user != null) {
                DesignationDTO designation = designationClient.getDesignationById(user.getDesignation());
                dto.setDesignation(designation != null ? designation.getTitle() : null);
            } else {
                dto.setDesignation(null);
            }

            result.add(dto);
        }

        return result;
    }


    @Override
    public AttendanceDTO getUserDailyLog(Long userId, LocalDate date) {
        AttendanceRecord record = attendanceRepository.findByUserIdAndDateAndIsDeletedFalse(userId, date)
                .orElseThrow(() -> new IllegalArgumentException("No record found"));

        AttendanceDTO dto = attendanceMapper.mapToDTO(record);

        UserDTO user = userServiceClient.getUserById(userId);
        if (user != null) {
            DesignationDTO designation = designationClient.getDesignationById(user.getDesignation());
            dto.setDesignation(designation != null ? designation.getTitle() : null);
        } else {
            dto.setDesignation(null);
        }

        return dto;
    }

    @Override
    public List<AttendanceDTO> getAllAttendanceRecords() {
        List<AttendanceRecord> records = attendanceRepository.findAll();
        List<AttendanceDTO> result = new ArrayList<>();

        for (AttendanceRecord record : records) {
            AttendanceDTO dto = attendanceMapper.mapToDTO(record);

            UserDTO user = userServiceClient.getUserById(record.getUserId());
            if (user != null) {
                DesignationDTO designation = designationClient.getDesignationById(user.getDesignation());
                dto.setDesignation(designation != null ? designation.getTitle() : null);
            } else {
                dto.setDesignation(null);
            }
            result.add(dto);
        }
        return result;
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

    @Override
    public List<AttendanceDTO> getAttendanceByDesignation(String designation) {
        List<AttendanceRecord> records = attendanceRepository.findByDesignationLike(designation);
        return records.stream()
                .map(attendanceMapper::mapToDTO)
                .collect(Collectors.toList());
    }


    @Override
    public List<AttendanceDTO> getAttendanceByDesignationAndDateRange(String designation, LocalDate startDate, LocalDate endDate) {
        List<AttendanceRecord> records = attendanceRepository.findByDesignationAndDateRange(designation, startDate, endDate);
        return records.stream()
                .map(attendanceMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllDesignations() {
        return attendanceRepository.findAll().stream()
                .map(AttendanceRecord::getDesignation)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }



}
