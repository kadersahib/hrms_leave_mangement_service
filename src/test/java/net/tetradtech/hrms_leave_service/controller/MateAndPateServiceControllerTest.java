package net.tetradtech.hrms_leave_service.controller;

import net.tetradtech.hrms_leave_service.constants.DayOffType;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import net.tetradtech.hrms_leave_service.service.MateAndPateService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MateAndPateServiceController.class)
class MateAndPateServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MateAndPateService leaveDocumentService;

    @MockBean
    private LeaveApplicationRepository leaveApplicationRepository;

    @Test
    void testApplyLeave() throws Exception {
        LeaveApplication leave = new LeaveApplication();
        leave.setUserId(1L);
        leave.setLeaveTypeId(3L);
        leave.setDayOffType(DayOffType.LEAVE);
        leave.setStartDate(LocalDate.of(2025, 8, 1));
        leave.setEndDate(LocalDate.of(2025, 8, 5));
        leave.setDocumentName("medical.pdf");

        when(leaveDocumentService.applyLeave(anyLong(), anyLong(), any(), anyLong(), any(), any(), any()))
                .thenReturn(leave);

        MockMultipartFile file = new MockMultipartFile(
                "file", "medical.pdf", "application/pdf", "test".getBytes()
        );

        mockMvc.perform(multipart("/api/leaveDocument/apply")
                        .file(file)
                        .param("userId", "1")
                        .param("leaveTypeId", "3")
                        .param("dayOffType", "LEAVE")
                        .param("reportingId", "10")
                        .param("startDate", "2025-08-01")
                        .param("endDate", "2025-08-05")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.documentName").value("medical.pdf"));
    }

    @Test
    void testUpdateLeave() throws Exception {
        LeaveApplication leave = new LeaveApplication();
        leave.setUserId(2L);
        leave.setLeaveTypeId(4L);
        leave.setDayOffType(DayOffType.LEAVE);
        leave.setDocumentName("update.pdf");

        when(leaveDocumentService.updateLeave(anyLong(), anyLong(), anyLong(), any(), anyLong(), any(), any(), any()))
                .thenReturn(leave);

        MockMultipartFile file = new MockMultipartFile(
                "file", "update.pdf", "application/pdf", "update-bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/leaveDocument/apply/200")
                        .file(file)
                        .with(req -> { req.setMethod("PUT"); return req; }) // change to PUT
                        .param("userId", "2")
                        .param("leaveTypeId", "4")
                        .param("dayOffType", "LEAVE")
                        .param("reportingId", "10")
                        .param("startDate", "2025-08-10")
                        .param("endDate", "2025-08-12")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.documentName").value("update.pdf"));
    }

    @Test
    void testDownloadDocument_Success() throws Exception {
        LeaveApplication leave = new LeaveApplication();
        leave.setId(100L);
        leave.setDocumentName("file.pdf");
        leave.setDocumentData("pdf-bytes".getBytes());

        when(leaveApplicationRepository.findById(100L))
                .thenReturn(Optional.of(leave));

        mockMvc.perform(get("/api/leaveDocument/download/100"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"file.pdf\""))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes("pdf-bytes".getBytes()));
    }

    @Test
    void testDownloadDocument_NoFile() throws Exception {
        LeaveApplication leave = new LeaveApplication();
        leave.setId(200L);
        leave.setDocumentName("nofile.pdf");
        leave.setDocumentData(null);

        when(leaveApplicationRepository.findById(200L)).thenReturn(Optional.of(leave));

        mockMvc.perform(get("/api/leaveDocument/download/200"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("No document attached for this leave."));
    }

    @Test
    void testDownloadDocument_NotFound() throws Exception {
        when(leaveApplicationRepository.findById(300L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/leaveDocument/download/300"))
                .andExpect(status().isInternalServerError()); // RuntimeException expected
    }
}
