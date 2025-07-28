package net.tetradtech.hrms_leave_service.controller;

import net.tetradtech.hrms_leave_service.dto.LeaveBalanceDTO;
import net.tetradtech.hrms_leave_service.service.LeaveBalanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeaveBalanceController.class)
class LeaveBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaveBalanceService leaveBalanceService;

    @Test
    void testGetAllLeaveBalances() throws Exception {
        LeaveBalanceDTO dto = new LeaveBalanceDTO("John Doe", 1L, 101L, 5, 10);
        when(leaveBalanceService.getAllLeaves()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/leave-balance/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].name").value("John Doe"));
    }

    @Test
    void testGetLeaveBalancesByUserId() throws Exception {
        LeaveBalanceDTO dto = new LeaveBalanceDTO("John Doe", 1L, 101L, 5, 10);
        when(leaveBalanceService.getLeavesByUserId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/leave-balance/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].userId").value(1));
    }

    @Test
    void testGetLeaveBalanceWithData() throws Exception {
        LeaveBalanceDTO dto = new LeaveBalanceDTO("John Doe", 1L, 101L, 5, 10);
        when(leaveBalanceService.getLeaveBalanceByUserIdAndLeaveType(1L, 101L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/leave-balance")
                        .param("userId", "1")
                        .param("leaveTypeId", "101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].leaveId").value(101));
    }

    @Test
    void testGetLeaveBalanceNoData() throws Exception {
        when(leaveBalanceService.getLeaveBalanceByUserIdAndLeaveType(1L, 101L)).thenReturn(List.of());

        mockMvc.perform(get("/api/leave-balance")
                        .param("userId", "1")
                        .param("leaveTypeId", "101"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"));
    }
}
