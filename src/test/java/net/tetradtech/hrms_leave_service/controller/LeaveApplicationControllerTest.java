package net.tetradtech.hrms_leave_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.tetradtech.hrms_leave_service.dto.LeaveRequestDTO;
import net.tetradtech.hrms_leave_service.dto.LeaveUpdateRequestDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.service.LeaveApplicationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LeaveApplicationController.class)
class LeaveApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LeaveApplicationService leaveApplicationService;

    @Test
    void testApplyLeave_Success() throws Exception {
        LeaveRequestDTO request = new LeaveRequestDTO(
                1L,                        // userId
                1L,                        // leaveTypeId
                LocalDate.now().plusDays(1), // startDate
                LocalDate.now().plusDays(3), // endDate
                2L,                        // reportingId
                "Full Day",                // dayOffType
                "Personal Work",           // reason
                null                       // leaveOtherReason (optional)
        );

        LeaveApplication leave = new LeaveApplication();
        leave.setId(100L);

        Mockito.when(leaveApplicationService.applyLeave(any(LeaveRequestDTO.class)))
                .thenReturn(leave);

        mockMvc.perform(post("/api/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(100L));
    }


    @Test
    void testUpdateLeave_Success() throws Exception {
        LeaveUpdateRequestDTO updateRequest = new LeaveUpdateRequestDTO();
        updateRequest.setUserId(1L);
        updateRequest.setLeaveTypeId(1L);
        updateRequest.setStartDate(LocalDate.now().plusDays(1));
        updateRequest.setEndDate(LocalDate.now().plusDays(3));
        updateRequest.setReportingId(2L);
        updateRequest.setDayOffType("leave");
        updateRequest.setReason("Medical Emergency");

        LeaveApplication updatedLeave = new LeaveApplication();
        updatedLeave.setId(200L);

        Mockito.when(leaveApplicationService.updateLeave(eq(1L), any(LeaveUpdateRequestDTO.class)))
                .thenReturn(updatedLeave);

        mockMvc.perform(put("/api/leaves/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(200L));
    }

    @Test
    void testGetAllLeaves() throws Exception {
        LeaveApplication leave = new LeaveApplication();
        leave.setId(300L);

        Mockito.when(leaveApplicationService.getAllLeaves()).thenReturn(List.of(leave));

        mockMvc.perform(get("/api/leaves/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(300L));
    }

    @Test
    void testGetLeavesById() throws Exception {
        LeaveApplication leave = new LeaveApplication();
        leave.setId(400L);

        Mockito.when(leaveApplicationService.getLeaveById(1L)).thenReturn(leave);

        mockMvc.perform(get("/api/leaves/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(400L));
    }


    @Test
    void testDeleteLeave() throws Exception {
        Mockito.doNothing().when(leaveApplicationService).deleteById(1L);

        mockMvc.perform(delete("/api/leaves/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Leave deleted (soft)"));
    }

    @Test
    void testCancelLeave() throws Exception {
        LeaveApplication cancelled = new LeaveApplication();
        cancelled.setId(500L);

        Mockito.when(leaveApplicationService.cancelLeave(1L)).thenReturn(cancelled);

        mockMvc.perform(post("/api/leaves/cancel/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(500L));
    }
}
