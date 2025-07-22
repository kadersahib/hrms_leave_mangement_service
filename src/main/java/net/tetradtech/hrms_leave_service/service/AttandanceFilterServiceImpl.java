package net.tetradtech.hrms_leave_service.service;


import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.AttendanceDTO;
import net.tetradtech.hrms_leave_service.dto.AttendanceStatsDTO;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.mapper.AttendanceMapper;
import net.tetradtech.hrms_leave_service.model.AttendanceRecord;
import net.tetradtech.hrms_leave_service.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class AttandanceFilterServiceImpl implements AttandanceFilterService {

    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Override
    public List<AttendanceDTO> getFilterByDesignation(String designation) {
        List<AttendanceRecord> records = attendanceRepository.findByDesignationLike(designation);
        if (records.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No attendance records found for designation: " + designation);

        }
        return records.stream()
                .map(attendanceMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDTO> getFilterByDesignationAndDateRange(String designation, LocalDate startDate, LocalDate endDate) {
        List<AttendanceRecord> records = attendanceRepository.findByDesignationAndDateRange(designation, startDate, endDate);
        return records.stream()
                .map(attendanceMapper::mapToDTO)
                .collect(Collectors.toList());
    }


    @Override
    public List<AttendanceStatsDTO> getAllUserAttendanceStats() {
        List<UserDTO> users = userServiceClient.getAllUsers();
        List<AttendanceStatsDTO> statsList = new ArrayList<>();

        for (UserDTO user : users) {
            statsList.add(getUserAttendanceStats(user.getId()));
        }
        return statsList;
    }

    @Override
    public AttendanceStatsDTO getUserAttendanceStats(Long userId) {
        UserDTO user = userServiceClient.getUserById(userId);
        if (user == null){
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }

        long workingDays = attendanceRepository.countByUserIdAndIsWorkingDayTrue(userId);
        long onTimeDays = attendanceRepository.countByUserIdAndIsLateFalseAndIsWorkingDayTrue(userId);
        long lateDays = attendanceRepository.countByUserIdAndIsLateTrueAndIsWorkingDayTrue(userId);
        long weekendWorked = attendanceRepository.countWeekendWorkedDays(userId);

        AttendanceStatsDTO dto = new AttendanceStatsDTO();
        dto.setUserId(userId);
        dto.setTotalWorkingDays(workingDays);
        dto.setOnTimeDays(onTimeDays);
        dto.setLateDays(lateDays);
        dto.setWeekendDaysWorked(weekendWorked);

        return dto;
    }
}
