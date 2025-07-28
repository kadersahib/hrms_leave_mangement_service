package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.constants.DayOffType;
import net.tetradtech.hrms_leave_service.constants.LeaveStatus;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MateAndPateServiceImplTest {

    @Mock
    private LeaveApplicationRepository leaveRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private MateAndPateServiceImpl service;

    private UserDTO femaleUser;
    private UserDTO maleUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        femaleUser = new UserDTO();
        femaleUser.setId(1L);
        femaleUser.setGender("FEMALE");

        maleUser = new UserDTO();
        maleUser.setId(2L);
        maleUser.setGender("MALE");
    }

    @Test
    void testApplyLeave_WithAndWithoutFile() throws Exception {
        when(userServiceClient.getUserById(1L)).thenReturn(femaleUser);
        when(leaveRepository.existsByUserIdAndLeaveTypeIdAndStartDateBetween(anyLong(), anyLong(), any(), any()))
                .thenReturn(false);
        when(leaveRepository.save(any(LeaveApplication.class)))
                .thenAnswer(invocation -> {
                    LeaveApplication leave = invocation.getArgument(0);
                    leave.setId(100L);
                    return leave;
                });
        //   With File - Should succeed
        MockMultipartFile file = new MockMultipartFile(
                "file", "medical.pdf", "application/pdf", "test-data".getBytes()
        );

        LeaveApplication result = service.applyLeave(
                1L, 3L, DayOffType.LEAVE, 10L,
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2025, 8, 10),
                file
        );

        assertNotNull(result);
        assertEquals("medical.pdf", result.getDocumentName());
        verify(leaveRepository, times(2)).save(any(LeaveApplication.class));

        //  Without File - Should throw exception
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.applyLeave(
                        1L, 3L, DayOffType.LEAVE, 10L,
                        LocalDate.of(2025, 8, 1),
                        LocalDate.of(2025, 8, 10),
                        null // no file
                )
        );

        assertEquals("Document is required for applying 3 leave.", ex.getMessage());
        verify(leaveRepository, times(2)).save(any(LeaveApplication.class)); // no extra save call
    }


    @Test
    void testUpdateLeave_WithAndWithoutFile() throws Exception {
        // Setup existing leave
        LeaveApplication existing = new LeaveApplication();
        existing.setId(200L);
        existing.setStatus(LeaveStatus.PENDING);
        existing.setStartDate(LocalDate.now());

        when(leaveRepository.findById(200L)).thenReturn(Optional.of(existing));
        when(leaveRepository.save(any(LeaveApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //  Update with file (should succeed)
        MockMultipartFile file = new MockMultipartFile(
                "file", "update.pdf", "application/pdf", "update-bytes".getBytes()
        );

        LeaveApplication result = service.updateLeave(
                200L, 1L, 3L, DayOffType.LEAVE, 5L,
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2025, 8, 3),
                file
        );

        assertNotNull(result);
        assertEquals("update.pdf", result.getDocumentName());
        assertNotNull(result.getDocumentData());
        assertTrue(result.getDocumentPath().contains("/api/leaveDocument/download/200"));
        verify(leaveRepository, times(1)).save(any(LeaveApplication.class));

        // Update without file (should throw exception)
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.updateLeave(
                        200L, 1L, 3L, DayOffType.LEAVE, 5L,
                        LocalDate.of(2025, 8, 10),
                        LocalDate.of(2025, 8, 12),
                        null
                )
        );

        assertEquals("Document is required when updating leave.", ex.getMessage());
        verify(leaveRepository, times(1)).save(any(LeaveApplication.class)); // still only 1 save call
    }

    @Test
    void testRejectExceedingMaternityDays() {
        when(userServiceClient.getUserById(1L)).thenReturn(femaleUser);
        when(leaveRepository.existsByUserIdAndLeaveTypeIdAndStartDateBetween(anyLong(), anyLong(), any(), any()))
                .thenReturn(false);

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = start.plusDays(150); // 151 days (exceeds 100)

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.applyLeave(
                        1L, 3L, DayOffType.LEAVE, 10L,
                        start, end, null
                )
        );

        assertEquals("Maternity leave cannot exceed 100 days.", ex.getMessage());
        verify(leaveRepository, never()).save(any());
    }

    @Test
    void testRejectExceedingPaternityDays() {
        when(userServiceClient.getUserById(2L)).thenReturn(maleUser);
        when(leaveRepository.existsByUserIdAndLeaveTypeIdAndStartDateBetween(anyLong(), anyLong(), any(), any()))
                .thenReturn(false);
        when(leaveRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0)); // ✅ Prevent NPE

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = start.plusDays(30); // 20 working days even with weekends

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.applyLeave(
                        2L, 4L, DayOffType.LEAVE, 10L,
                        start, end, null
                )
        );

        assertEquals("Paternity leave cannot exceed 20 days.", ex.getMessage());
        verify(leaveRepository, never()).save(any());
    }

    @Test
    void testRejectMaternityForMaleUser() {
        when(userServiceClient.getUserById(2L)).thenReturn(maleUser);

        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "bytes".getBytes()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.applyLeave(
                        2L, 3L, DayOffType.LEAVE, 5L,
                        LocalDate.of(2025, 8, 1),
                        LocalDate.of(2025, 8, 5),
                        file
                )
        );

        assertEquals("Maternity leave only applicable to female users.", ex.getMessage());
        verify(leaveRepository, never()).save(any());
    }
    @Test
    void testRejectPaternityForFemaleUser() {
        when(userServiceClient.getUserById(3L)).thenReturn(femaleUser); // returning female user

        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "bytes".getBytes()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.applyLeave(
                        3L, 4L, DayOffType.LEAVE, 5L,
                        LocalDate.of(2025, 9, 1),
                        LocalDate.of(2025, 9, 5),
                        file
                )
        );

        assertEquals("Paternity leave only applicable to male users.", ex.getMessage());
        verify(leaveRepository, never()).save(any());
    }

}
