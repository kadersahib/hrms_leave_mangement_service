package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.CalendarAttendanceDTO;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class LeaveCalenderServiceImpl implements LeaveCalenderService {

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Override
    public List<CalendarAttendanceDTO> getUserMonthlyCalendar(Long userId, int year, int month) {
        UserDTO user = userServiceClient.getUserById(userId);
        if (user == null) throw new IllegalArgumentException("User not found");

        List<CalendarAttendanceDTO> result = new ArrayList<>();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<LeaveApplication> leaves = leaveApplicationRepository.findApprovedLeavesByUserIdAndDateRange(userId, start, end);

        for (LeaveApplication leave : leaves) {
            LocalDate leaveStart = leave.getStartDate().isBefore(start) ? start : leave.getStartDate();
            LocalDate leaveEnd = leave.getEndDate().isAfter(end) ? end : leave.getEndDate();

            for (LocalDate d = leaveStart; !d.isAfter(leaveEnd); d = d.plusDays(1)) {
                CalendarAttendanceDTO dto = new CalendarAttendanceDTO();
                dto.setUserId(userId);
                dto.setName(user.getName());
                dto.setDate(d);
                dto.setStatus("LEAVE");
                dto.setLeaveTypeId(leave.getLeaveTypeId());
                result.add(dto);
            }
        }

        return result;
    }

    @Override
    public List<CalendarAttendanceDTO> getAllUsersMonthlyCalendar(int year, int month) {
        List<UserDTO> users = userServiceClient.getAllUsers();
        List<CalendarAttendanceDTO> all = new ArrayList<>();

        for (UserDTO user : users) {
            all.addAll(getUserMonthlyCalendar(user.getId(), year, month));
        }
        return all;
    }
}
