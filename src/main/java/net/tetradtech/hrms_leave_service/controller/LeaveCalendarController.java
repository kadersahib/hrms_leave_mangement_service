package net.tetradtech.hrms_leave_service.controller;

import net.tetradtech.hrms_leave_service.dto.LeaveCalendarDTO;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import net.tetradtech.hrms_leave_service.service.LeaveCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leave-calendar")
public class LeaveCalendarController {

    @Autowired
    private LeaveCalendarService leaveCalendarService;
    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaveCalendarDTO>>> getLeaveCalendar() {
        List<LeaveCalendarDTO> calendarData = leaveCalendarService.getCalendarData();
        return ResponseEntity.ok(
                new ApiResponse<>("success", "Leave calendar fetched successfully", calendarData)
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<LeaveCalendarDTO>>> getUserCalendar(@PathVariable Long userId) {
        List<LeaveCalendarDTO> data = leaveCalendarService.getCalendarDataByUser(userId);
        return ResponseEntity.ok(new ApiResponse<>("success", "Calendar data for user " + userId, data));
    }

}
