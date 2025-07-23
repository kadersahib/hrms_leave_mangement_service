//package net.tetradtech.hrms_leave_service.controller;
//
//import net.tetradtech.hrms_leave_service.dto.CalendarAttendanceDTO;
//import net.tetradtech.hrms_leave_service.response.ApiResponse;
//import net.tetradtech.hrms_leave_service.service.LeaveCalenderService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/calendar")
//public class LeaveCalendarController {
//
//    @Autowired
//    private LeaveCalenderService CalendarService;
//
//
//    @GetMapping("/user")
//    public ResponseEntity<ApiResponse<List<CalendarAttendanceDTO>>> getUserMonthlyCalendar(
//            @RequestParam Long userId,
//            @RequestParam int year,
//            @RequestParam int month) {
//        try {
//            List<CalendarAttendanceDTO> calendar = CalendarService.getUserMonthlyCalendar(userId, year, month);
//            return ResponseEntity.ok(new ApiResponse<>("success", "Fetched user Leave calendar", calendar));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(new ApiResponse<>("error", e.getMessage(), null));
//        }
//    }
//
//    @GetMapping("/all")
//    public ResponseEntity<ApiResponse<List<CalendarAttendanceDTO>>> getAllUserMonthlyCalendar(
//            @RequestParam int year,
//            @RequestParam int month) {
//        List<CalendarAttendanceDTO> calendar = CalendarService.getAllUsersMonthlyCalendar(year, month);
//        return ResponseEntity.ok(new ApiResponse<>("success", "Fetched all user Leave calendars", calendar));
//    }
//
//
//
//}
