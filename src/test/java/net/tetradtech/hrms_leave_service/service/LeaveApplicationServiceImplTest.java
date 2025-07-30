package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.constants.LeaveStatus;
import net.tetradtech.hrms_leave_service.dto.LeaveRequestDTO;
import net.tetradtech.hrms_leave_service.dto.LeaveUpdateRequestDTO;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.mapper.LeaveApplicationMapper;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import net.tetradtech.hrms_leave_service.util.LeaveTypeUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeaveApplicationServiceImplTest {

    @InjectMocks
    private LeaveApplicationServiceImpl leaveService;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private LeaveApplicationRepository leaveApplicationRepository;

    @Mock
    private LeaveApplicationMapper leaveApplicationMapper;

    @Test
    void testApplyLeave_Success() {
        LeaveRequestDTO request = new LeaveRequestDTO();
        request.setUserId(1L);
        request.setLeaveId(1L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));

        UserDTO user = new UserDTO();
        user.setId(1L);

        when(userServiceClient.getUserById(1L)).thenReturn(user);
        when(leaveApplicationRepository.countOverlappingLeaves(
                1L,
                request.getStartDate(),
                request.getEndDate()
        )).thenReturn(0L);

        when(leaveApplicationRepository.getTotalUsedDaysForYear(
                1L, 1L, request.getStartDate().getYear()
        )).thenReturn(0);

        LeaveApplication newLeave = new LeaveApplication();
        when(leaveApplicationMapper.toNewLeaveApplication(any(), anyInt(), anyInt())).thenReturn(newLeave);
        when(leaveApplicationRepository.save(any())).thenReturn(newLeave);

        LeaveApplication result = leaveService.applyLeave(request);

        assertNotNull(result);
        verify(leaveApplicationRepository).save(newLeave);
    }

    @Test
    void InvalidUser() {
        LeaveRequestDTO request = new LeaveRequestDTO();
        request.setUserId(99L);

        when(userServiceClient.getUserById(99L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> leaveService.applyLeave(request));
    }

    @Test
    void StartDateInPast() {
        LeaveRequestDTO request = new LeaveRequestDTO();
        request.setUserId(1L);
        request.setStartDate(LocalDate.now().minusDays(1));
        request.setEndDate(LocalDate.now());

        when(userServiceClient.getUserById(1L)).thenReturn(new UserDTO());

        assertThrows(IllegalArgumentException.class, () -> leaveService.applyLeave(request));
    }

    @Test
    void EndDateBeforeStartDate() {
        LeaveRequestDTO request = new LeaveRequestDTO();
        request.setUserId(1L);
        request.setStartDate(LocalDate.now().plusDays(5));
        request.setEndDate(LocalDate.now().plusDays(3));

        when(userServiceClient.getUserById(1L)).thenReturn(new UserDTO());

        assertThrows(IllegalArgumentException.class, () -> leaveService.applyLeave(request));
    }

    @Test
    void ExceedsMaxDays() {
        LeaveRequestDTO request = new LeaveRequestDTO();
        request.setUserId(1L);
        request.setLeaveId(1L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(35)); // too many days

        when(userServiceClient.getUserById(1L)).thenReturn(new UserDTO());

        when(leaveApplicationRepository.countOverlappingLeaves(
                1L,
                request.getStartDate(),
                request.getEndDate()
        )).thenReturn(0L);

        when(leaveApplicationRepository.getTotalUsedDaysForYear(
                1L, 1L, request.getStartDate().getYear()
        )).thenReturn(0);

        assertThrows(IllegalArgumentException.class, () -> leaveService.applyLeave(request));
    }

    @Test
    void OverlappingDates() {
        LeaveRequestDTO request = new LeaveRequestDTO();
        request.setUserId(1L);
        request.setLeaveId(1L);
        request.setStartDate(LocalDate.now().plusDays(3));
        request.setEndDate(LocalDate.now().plusDays(5));

        when(userServiceClient.getUserById(1L)).thenReturn(new UserDTO());
        when(leaveApplicationRepository.countOverlappingLeaves(
                1L,
                request.getStartDate(),
                request.getEndDate()
        )).thenReturn(1L); // Simulate overlapping

        assertThrows(IllegalArgumentException.class,
                () -> leaveService.applyLeave(request));
    }

    @Test
    void RemainingDaysExceededDueToUsedDays() {
        LeaveRequestDTO request = new LeaveRequestDTO();
        request.setUserId(1L);
        request.setLeaveId(1L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3)); // 3 days requested

        when(userServiceClient.getUserById(1L)).thenReturn(new UserDTO());
        when(leaveApplicationRepository.countOverlappingLeaves(
                1L,
                request.getStartDate(),
                request.getEndDate()
        )).thenReturn(0L);
        when(leaveApplicationRepository.getTotalUsedDaysForYear(
                1L, 1L, request.getStartDate().getYear()
        )).thenReturn(9); // Already used 9 of 10 days

        try (MockedStatic<LeaveTypeUtil> mocked = Mockito.mockStatic(LeaveTypeUtil.class)) {
            mocked.when(() -> LeaveTypeUtil.getMaxDays(1L)).thenReturn(10);

            assertThrows(IllegalArgumentException.class,
                    () -> leaveService.applyLeave(request));
        }
    }


    @Test
    void testUpdateLeave_Success() {
        LeaveApplication existing = new LeaveApplication();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setStatus(LeaveStatus.PENDING);
        existing.setStartDate(LocalDate.now().plusDays(2));
        existing.setEndDate(LocalDate.now().plusDays(3));

        LeaveUpdateRequestDTO updatedData = new LeaveUpdateRequestDTO();
        updatedData.setUserId(1L);
        updatedData.setLeaveId(1L);
        updatedData.setStartDate(LocalDate.now().plusDays(2));
        updatedData.setEndDate(LocalDate.now().plusDays(4));
        updatedData.setDayOffType("leave");

        when(leaveApplicationRepository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(existing));

        // Mark lenient to avoid UnnecessaryStubbingException
        lenient().when(leaveApplicationRepository.findByUserIdAndIsDeletedFalse(1L))
                .thenReturn(Collections.singletonList(existing));

        when(leaveApplicationRepository.save(any()))
                .thenReturn(existing);

        LeaveApplication result = leaveService.updateLeave(1L, updatedData);

        assertNotNull(result);
        verify(leaveApplicationRepository).save(existing);
    }

    @Test
    void UserIdMismatch() {
        LeaveApplication existing = new LeaveApplication();
        existing.setId(1L);
        existing.setUserId(2L);
        existing.setStatus(LeaveStatus.PENDING);

        LeaveUpdateRequestDTO updatedData = new LeaveUpdateRequestDTO();
        updatedData.setUserId(1L);

        when(leaveApplicationRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> leaveService.updateLeave(1L, updatedData));
    }

    @Test
    void NotPendingStatus() {
        LeaveApplication existing = new LeaveApplication();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setStatus(LeaveStatus.APPROVED);

        LeaveUpdateRequestDTO updatedData = new LeaveUpdateRequestDTO();
        updatedData.setUserId(1L);

        when(leaveApplicationRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class, () -> leaveService.updateLeave(1L, updatedData));
    }

    @Test
    void testUpdateLeave_StartDateInPast() {
        LeaveApplication existing = new LeaveApplication();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setStatus(LeaveStatus.PENDING);

        LeaveUpdateRequestDTO updatedData = new LeaveUpdateRequestDTO();
        updatedData.setUserId(1L);
        updatedData.setStartDate(LocalDate.now().minusDays(2));
        updatedData.setEndDate(LocalDate.now());

        when(leaveApplicationRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> leaveService.updateLeave(1L, updatedData));
    }

    @Test
    void testUpdateLeave_EndDateBeforeStartDate() {
        LeaveApplication existing = new LeaveApplication();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setStatus(LeaveStatus.PENDING);

        LeaveUpdateRequestDTO updatedData = new LeaveUpdateRequestDTO();
        updatedData.setUserId(1L);
        updatedData.setStartDate(LocalDate.now().plusDays(5));
        updatedData.setEndDate(LocalDate.now().plusDays(3));

        when(leaveApplicationRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> leaveService.updateLeave(1L, updatedData));
    }

    @Test
    void testUpdateLeave_OverlappingDates() {
        LeaveApplication existing = new LeaveApplication();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setLeaveTypeId(1L);
        existing.setStatus(LeaveStatus.PENDING);
        existing.setStartDate(LocalDate.now().plusDays(2));
        existing.setEndDate(LocalDate.now().plusDays(3));

        LeaveUpdateRequestDTO updatedData = new LeaveUpdateRequestDTO();
        updatedData.setUserId(1L);
        updatedData.setLeaveId(1L);
        updatedData.setStartDate(LocalDate.now().plusDays(2));
        updatedData.setEndDate(LocalDate.now().plusDays(3));
        updatedData.setDayOffType("leave");

        when(leaveApplicationRepository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(existing));

        when(leaveApplicationRepository.countOverlappingLeavesForUpdate(
                eq(1L), eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(1); // Overlap found

        assertThrows(IllegalArgumentException.class,
                () -> leaveService.updateLeave(1L, updatedData));
    }


    @Test
    void testUpdateLeave_RemainingDaysExceeded() {
        LeaveApplication existing = new LeaveApplication();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setStatus(LeaveStatus.PENDING);
        existing.setStartDate(LocalDate.now().plusDays(2));
        existing.setEndDate(LocalDate.now().plusDays(3));

        LeaveUpdateRequestDTO updatedData = new LeaveUpdateRequestDTO();
        updatedData.setUserId(1L);
        updatedData.setLeaveId(1L);
        updatedData.setStartDate(LocalDate.now().plusDays(2));
        updatedData.setEndDate(LocalDate.now().plusDays(10)); // 9 days request
        updatedData.setDayOffType("leave");

        when(leaveApplicationRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existing));
        when(leaveApplicationRepository.countOverlappingLeavesForUpdate(anyLong(), anyLong(), any(), any()))
                .thenReturn(0);
        when(leaveApplicationRepository.getTotalUsedDaysForYearExcludingId(anyLong(), anyLong(), anyInt(), anyLong()))
                .thenReturn(5); // Already used 5 days

        try (MockedStatic<LeaveTypeUtil> mocked = Mockito.mockStatic(LeaveTypeUtil.class)) {
            mocked.when(() -> LeaveTypeUtil.getMaxDays(1L)).thenReturn(10);

            assertThrows(IllegalArgumentException.class,
                    () -> leaveService.updateLeave(1L, updatedData));
        }
    }

    @Test
    void getLeavesById_Success() {
        LeaveApplication leave = new LeaveApplication();
        leave.setUserId(1L);

        when(leaveApplicationRepository.findByUserIdAndIsDeletedFalse(1L)).thenReturn(List.of(leave));

        List<LeaveApplication> result = leaveService.getLeavesById(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getUserId());
        verify(leaveApplicationRepository, times(1)).findByUserIdAndIsDeletedFalse(1L);
    }

    @Test
    void getAllLeaves_Success() {
        LeaveApplication leave = new LeaveApplication();
        leave.setId(10L);

        when(leaveApplicationRepository.findByIsDeletedFalse()).thenReturn(List.of(leave));

        List<LeaveApplication> result = leaveService.getAllLeaves();

        assertEquals(1, result.size());
        assertEquals(10L, result.getFirst().getId());
        verify(leaveApplicationRepository, times(1)).findByIsDeletedFalse();
    }

    @Test
    void deleteById_Success() {
        LeaveApplication leave = new LeaveApplication();
        leave.setId(10L);
        leave.setUserId(1L);
        leave.setDeleted(false);

        when(leaveApplicationRepository.findById(10L)).thenReturn(Optional.of(leave));
        when(leaveApplicationRepository.save(any(LeaveApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        leaveService.deleteById(10L);

        assertTrue(leave.isDeleted());
        assertNotNull(leave.getDeletedAt());
        assertEquals("1", leave.getDeletedBy());
        verify(leaveApplicationRepository, times(1)).save(leave);
    }

    @Test
    void deleteById_NotFound() {
        when(leaveApplicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> leaveService.deleteById(99L));
    }

    @Test
    void cancelLeave_Success() {
        LeaveApplication leave = new LeaveApplication();
        leave.setId(10L);
        leave.setUserId(1L);
        leave.setStatus(LeaveStatus.PENDING);
        leave.setDeleted(false);

        when(leaveApplicationRepository.findById(10L)).thenReturn(Optional.of(leave));
        when(leaveApplicationRepository.save(any(LeaveApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveApplication result = leaveService.cancelLeave(10L);

        assertEquals(LeaveStatus.CANCELLED, result.getStatus());
        assertNotNull(result.getUpdatedAt());
        verify(leaveApplicationRepository, times(1)).save(leave);
    }

    @Test
    void cancelLeave_AlreadyDeleted() {
        LeaveApplication leave = new LeaveApplication();
        leave.setId(10L);
        leave.setDeleted(true);

        when(leaveApplicationRepository.findById(10L)).thenReturn(Optional.of(leave));

        assertThrows(IllegalArgumentException.class, () -> leaveService.cancelLeave(10L));
    }

    @Test
    void cancelLeave_StatusNotPending() {
        LeaveApplication leave = new LeaveApplication();
        leave.setId(10L);
        leave.setDeleted(false);
        leave.setStatus(LeaveStatus.APPROVED);

        when(leaveApplicationRepository.findById(10L)).thenReturn(Optional.of(leave));

        assertThrows(IllegalStateException.class, () -> leaveService.cancelLeave(10L));
    }

}

