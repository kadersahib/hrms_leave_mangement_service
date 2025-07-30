package net.tetradtech.hrms_leave_service.controller;

import net.tetradtech.hrms_leave_service.service.LeaveBalanceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LeaveBalanceController.class)
class LeaveBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaveBalanceService leaveBalanceService;

    @Test
    void testGetAllLeaveBalances() throws Exception {
        List<Map<String, Object>> mockData = List.of(
                Map.of("userId", 1L, "Sick Leave", Map.of("totalApplied", 5, "balanceDays", 25))
        );

        Mockito.when(leaveBalanceService.getAllLeaves()).thenReturn(mockData);

        mockMvc.perform(get("/api/leave-balance/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].userId").value(1L))
                .andExpect(jsonPath("$.data[0]['Sick Leave'].totalApplied").value(5))
                .andExpect(jsonPath("$.data[0]['Sick Leave'].balanceDays").value(25));

    }

    @Test
    void testGetLeavesByUserId() throws Exception {
        List<Map<String, Object>> mockData = List.of(
                Map.of("userId", 2L, "Personal Leave", Map.of("totalApplied", 3, "balanceDays", 17))
        );

        Mockito.when(leaveBalanceService.getLeavesByUserId(2L)).thenReturn(mockData);

        mockMvc.perform(get("/api/leave-balance/2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].userId").value(2L))
                .andExpect(jsonPath("$.data[0]['Personal Leave'].totalApplied").value(3));
    }

    @Test
    void testGetLeaveBalanceByUserIdAndLeaveType() throws Exception {
        Map<String, Object> mockData = Map.of(
                "leaveTypeName", "Sick Leave",
                "totalApplied", 4,
                "balanceDays", 26
        );

        Mockito.when(leaveBalanceService.getLeaveBalanceByUserIdAndLeaveType(3L, 1L))
                .thenReturn(mockData);

        mockMvc.perform(get("/api/leave-balance/user/3/leaveTypeId/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.leaveTypeName").value("Sick Leave"))
                .andExpect(jsonPath("$.data.totalApplied").value(4))
                .andExpect(jsonPath("$.data.balanceDays").value(26));
    }

}
