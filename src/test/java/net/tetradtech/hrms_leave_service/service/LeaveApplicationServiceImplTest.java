package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.constants.LeaveStatus;
import net.tetradtech.hrms_leave_service.dto.LeaveRequestDTO;
import net.tetradtech.hrms_leave_service.dto.LeaveUpdateRequestDTO;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.mapper.LeaveApplicationMapper;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
    void applyLeave_Success() {
        LeaveApplicationServiceImpl spyService = Mockito.spy(leaveService);

        LeaveRequestDTO request = new LeaveRequestDTO();
        request.setUserId(1L);
        request.setLeaveId(1L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setReportingId(10L);
        request.setDayOffType("leave");

        UserDTO user = new UserDTO();
        user.setId(1L);

        LeaveApplication leaveApp = new LeaveApplication();
        leaveApp.setId(101L);   // ✅ ID is 101L

        when(userServiceClient.getUserById(1L)).thenReturn(user);
        when(leaveApplicationRepository.findByUserIdAndIsDeletedFalse(1L)).thenReturn(Collections.emptyList());
        when(leaveApplicationRepository.findByUserIdAndLeaveTypeIdAndIsDeletedFalse(1L, 1L)).thenReturn(Collections.emptyList());
        when(leaveApplicationMapper.toNewLeaveApplication(any(), anyInt(), anyInt())).thenReturn(leaveApp);
        when(leaveApplicationRepository.save(any(LeaveApplication.class))).thenReturn(leaveApp);

        doReturn(3).when(spyService).calculateWorkingDays(any(LocalDate.class), any(LocalDate.class));

        LeaveApplication result = spyService.applyLeave(request);

        assertNotNull(result);
        assertEquals(101L, result.getId()); //  Match mocked ID
        verify(spyService, times(1)).calculateWorkingDays(any(), any());
        verify(leaveApplicationRepository, times(1)).save(any());
    }

    @Test
    void applyLeave_InvalidUser_ThrowsException() {
        LeaveRequestDTO request = new LeaveRequestDTO();
        request.setUserId(99L);
        request.setLeaveId(1L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(2));
        request.setReportingId(1L);
        request.setDayOffType("Leave");
        when(userServiceClient.getUserById(99L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> leaveService.applyLeave(request));
    }

    @Test
    void applyLeave_InvalidDates_ThrowsException() {
        // Case 1: Start date is in the past
        LeaveRequestDTO pastStartRequest = new LeaveRequestDTO();
        pastStartRequest.setUserId(1L);
        pastStartRequest.setLeaveId(1L);
        pastStartRequest.setStartDate(LocalDate.now().minusDays(1));  // yesterday (past)
        pastStartRequest.setEndDate(LocalDate.now().plusDays(2));     // future
        pastStartRequest.setReportingId(1L);
        pastStartRequest.setDayOffType("Leave");

        UserDTO user = new UserDTO();
        when(userServiceClient.getUserById(1L)).thenReturn(user);

        assertThrows(IllegalArgumentException.class, () -> leaveService.applyLeave(pastStartRequest),
                "Expected exception for leave start date in the past");

        // Case 2: End date is before start date
        LeaveRequestDTO endBeforeStartRequest = new LeaveRequestDTO();
        endBeforeStartRequest.setUserId(1L);
        endBeforeStartRequest.setLeaveId(1L);
        endBeforeStartRequest.setStartDate(LocalDate.now().plusDays(5)); // future start
        endBeforeStartRequest.setEndDate(LocalDate.now().plusDays(2));   // earlier than start

        endBeforeStartRequest.setReportingId(1L);
        endBeforeStartRequest.setDayOffType("Leave");

        assertThrows(IllegalArgumentException.class, () -> leaveService.applyLeave(endBeforeStartRequest),
                "Expected exception when end date is before start date");
    }

    @Test
    void applyLeave_ActiveLeaveExists_ThrowsException() {
        LeaveRequestDTO request = new LeaveRequestDTO();
        request.setUserId(1L);
        request.setLeaveId(2L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));

        UserDTO user = new UserDTO();
        user.setId(1L);

        LeaveApplication activeLeave = new LeaveApplication();
        activeLeave.setStatus(LeaveStatus.PENDING);

        when(userServiceClient.getUserById(1L)).thenReturn(user);
        when(leaveApplicationRepository.findByUserIdAndLeaveTypeIdAndIsDeletedFalse(1L, 2L))
                .thenReturn(List.of(activeLeave));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> leaveService.applyLeave(request));

        assertEquals("You already have an active leave request for this leave type. Wait until its status changes.", ex.getMessage());
        verify(leaveApplicationRepository, times(1))
                .findByUserIdAndLeaveTypeIdAndIsDeletedFalse(1L, 2L);
    }

    @Test
    void applyLeave_NoActiveLeave_AllowsRequest() {
        LeaveRequestDTO request = new LeaveRequestDTO();
        request.setUserId(1L);
        request.setLeaveId(2L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));

        UserDTO user = new UserDTO();
        user.setId(1L);

        when(userServiceClient.getUserById(1L)).thenReturn(user);
        when(leaveApplicationRepository.findByUserIdAndIsDeletedFalse(1L)).thenReturn(Collections.emptyList());
        when(leaveApplicationRepository.findByUserIdAndLeaveTypeIdAndIsDeletedFalse(1L, 2L))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> leaveService.applyLeave(request));
    }
    @Test
    void applyLeave_DuplicateLeave_ThrowsException() {
        LeaveApplicationServiceImpl spyService = Mockito.spy(leaveService);

        LeaveRequestDTO request = new LeaveRequestDTO();
        request.setUserId(1L);
        request.setLeaveId(1L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setReportingId(1L);
        request.setDayOffType("Leave");

        UserDTO user = new UserDTO();
        when(userServiceClient.getUserById(1L)).thenReturn(user);

        LeaveApplication existing = new LeaveApplication();
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());

        when(leaveApplicationRepository.findByUserIdAndIsDeletedFalse(1L)).thenReturn(List.of(existing));

        assertThrows(IllegalArgumentException.class, () -> spyService.applyLeave(request));
    }

    @Test
    void applyLeave_ExceedsMaxDays_ThrowsException() {
        LeaveApplicationServiceImpl spyService = Mockito.spy(leaveService);

        LeaveRequestDTO request = new LeaveRequestDTO();
        request.setUserId(1L);
        request.setLeaveId(1L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(15));

        UserDTO user = new UserDTO();
        when(userServiceClient.getUserById(1L)).thenReturn(user);
        when(leaveApplicationRepository.findByUserIdAndIsDeletedFalse(1L)).thenReturn(Collections.emptyList());
        when(leaveApplicationRepository.findByUserIdAndLeaveTypeIdAndIsDeletedFalse(1L, 1L)).thenReturn(Collections.emptyList());

        // Force calculateWorkingDays to return 40 (exceeds max sick leave 30)
        doReturn(40).when(spyService).calculateWorkingDays(any(LocalDate.class), any(LocalDate.class));

        assertThrows(IllegalArgumentException.class, () -> spyService.applyLeave(request));
    }

    @Test
    void getMaxDaysForLeaveType_Test() {
        assertEquals(30, leaveService.getMaxDaysForLeaveType(1L));
        assertEquals(20, leaveService.getMaxDaysForLeaveType(2L));
        assertThrows(IllegalArgumentException.class, () -> leaveService.getMaxDaysForLeaveType(5L));
    }

    // Update Method
    @Test
    void updateLeave_Success() {
        LeaveApplicationServiceImpl spyService = Mockito.spy(leaveService);

        Long leaveId = 1L;

        LeaveApplication existingLeave = new LeaveApplication();
        existingLeave.setId(leaveId);
        existingLeave.setUserId(1L);
        existingLeave.setStatus(LeaveStatus.PENDING);
        existingLeave.setStartDate(LocalDate.now().plusDays(5));
        existingLeave.setEndDate(LocalDate.now().plusDays(7));
        existingLeave.setLeaveTypeId(1L);

        LeaveUpdateRequestDTO updatedData = new LeaveUpdateRequestDTO();
        updatedData.setUserId(1L);
        updatedData.setLeaveId(1L);
        updatedData.setStartDate(LocalDate.now().plusDays(10));
        updatedData.setEndDate(LocalDate.now().plusDays(12));
        updatedData.setReportingId(100L);
        updatedData.setDayOffType("leave");

        when(leaveApplicationRepository.findByIdAndIsDeletedFalse(leaveId))
                .thenReturn(Optional.of(existingLeave));
        when(leaveApplicationRepository.findByUserIdAndIsDeletedFalse(1L))
                .thenReturn(Collections.singletonList(existingLeave));
        when(leaveApplicationRepository.findByUserIdAndLeaveTypeIdAndIsDeletedFalse(1L, 1L))
                .thenReturn(Collections.singletonList(existingLeave));
        when(leaveApplicationRepository.save(any(LeaveApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doReturn(3).when(spyService)
                .calculateWorkingDays(any(LocalDate.class), any(LocalDate.class));

        LeaveApplication result = spyService.updateLeave(leaveId, updatedData);

        assertNotNull(result);
        assertEquals(updatedData.getStartDate(), result.getStartDate());
        assertEquals(3, result.getTotalLeaveDays());
        verify(leaveApplicationRepository, times(1)).save(any());
    }

    @Test
    void updateLeave_LeaveNotFound_ThrowsException() {
        when(leaveApplicationRepository.findByIdAndIsDeletedFalse(99L))
                .thenReturn(Optional.empty());

        LeaveUpdateRequestDTO updatedData = new LeaveUpdateRequestDTO();
        updatedData.setUserId(1L);
        updatedData.setLeaveId(1L);
        updatedData.setStartDate(LocalDate.now().plusDays(1));
        updatedData.setEndDate(LocalDate.now().plusDays(2));
        updatedData.setReportingId(100L);
        updatedData.setDayOffType("leave");

        assertThrows(IllegalArgumentException.class, () -> leaveService.updateLeave(99L, updatedData));
    }

    @Test
    void updateLeave_UserIdMismatch_ThrowsException() {
        LeaveApplication existing = new LeaveApplication();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setStatus(LeaveStatus.PENDING);

        LeaveUpdateRequestDTO updatedData = new LeaveUpdateRequestDTO();
        updatedData.setUserId(2L);
        updatedData.setLeaveId(1L);
        updatedData.setStartDate(LocalDate.now().plusDays(5));
        updatedData.setEndDate(LocalDate.now().plusDays(7));
        updatedData.setReportingId(100L);
        updatedData.setDayOffType("leave");

        when(leaveApplicationRepository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> leaveService.updateLeave(1L, updatedData));
    }

    @Test
    void updateLeave_StatusNotPending_ThrowsException() {
        LeaveApplication existing = new LeaveApplication();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setStatus(LeaveStatus.APPROVED); // Not pending

        LeaveUpdateRequestDTO updatedData = new LeaveUpdateRequestDTO();
        updatedData.setUserId(1L);
        updatedData.setLeaveId(1L);
        updatedData.setStartDate(LocalDate.now().plusDays(1));
        updatedData.setEndDate(LocalDate.now().plusDays(2));
        updatedData.setReportingId(100L);
        updatedData.setDayOffType("leave");

        when(leaveApplicationRepository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class, () -> leaveService.updateLeave(1L, updatedData));
    }


    @Test
    void updateLeave_StartDateInPast_ThrowsException() {
        LeaveApplication existing = new LeaveApplication();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setStatus(LeaveStatus.PENDING);

        LeaveUpdateRequestDTO updatedData = new LeaveUpdateRequestDTO();
        updatedData.setUserId(1L);
        updatedData.setLeaveId(1L);
        updatedData.setStartDate(LocalDate.now().minusDays(1));
        updatedData.setEndDate(LocalDate.now().plusDays(2));
        updatedData.setReportingId(100L);
        updatedData.setDayOffType("FULL_DAY");

        when(leaveApplicationRepository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> leaveService.updateLeave(1L, updatedData));
    }

    @Test
    void updateLeave_EndDateBeforeStartDate_ThrowsException() {
        LeaveApplication existing = new LeaveApplication();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setStatus(LeaveStatus.PENDING);

        LeaveUpdateRequestDTO updatedData = new LeaveUpdateRequestDTO();
        updatedData.setUserId(1L);
        updatedData.setLeaveId(1L);
        updatedData.setStartDate(LocalDate.now().plusDays(5));
        updatedData.setEndDate(LocalDate.now().plusDays(2));
        updatedData.setReportingId(100L);
        updatedData.setDayOffType("leave");

        when(leaveApplicationRepository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> leaveService.updateLeave(1L, updatedData));
    }

    @Test
    void updateLeave_OverlappingDates_ThrowsException() {
        LeaveApplicationServiceImpl spyService = Mockito.spy(leaveService);

        LeaveApplication existing = new LeaveApplication();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setStatus(LeaveStatus.PENDING);

        LeaveApplication anotherLeave = new LeaveApplication();
        anotherLeave.setId(2L);
        anotherLeave.setUserId(1L);
        anotherLeave.setStartDate(LocalDate.now().plusDays(6));
        anotherLeave.setEndDate(LocalDate.now().plusDays(8));

        LeaveUpdateRequestDTO updatedData = new LeaveUpdateRequestDTO();
        updatedData.setUserId(1L);
        updatedData.setLeaveId(1L);
        updatedData.setStartDate(LocalDate.now().plusDays(6));
        updatedData.setEndDate(LocalDate.now().plusDays(8));
        updatedData.setReportingId(100L);
        updatedData.setDayOffType("leave");

        when(leaveApplicationRepository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(existing));
        when(leaveApplicationRepository.findByUserIdAndIsDeletedFalse(1L))
                .thenReturn(List.of(existing, anotherLeave));

        assertThrows(IllegalArgumentException.class, () -> spyService.updateLeave(1L, updatedData));
    }

    @Test
    void updateLeave_ExceedsBalance_ThrowsException() {
        LeaveApplicationServiceImpl spyService = Mockito.spy(leaveService);

        LeaveApplication existing = new LeaveApplication();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setStatus(LeaveStatus.PENDING);
        existing.setLeaveTypeId(1L);

        LeaveUpdateRequestDTO updatedData = new LeaveUpdateRequestDTO();
        updatedData.setUserId(1L);
        updatedData.setLeaveId(1L);
        updatedData.setStartDate(LocalDate.now().plusDays(1));
        updatedData.setEndDate(LocalDate.now().plusDays(20));
        updatedData.setReportingId(100L);
        updatedData.setDayOffType("leave");

        LeaveApplication used = new LeaveApplication();
        used.setId(2L);
        used.setUserId(1L);
        used.setLeaveTypeId(1L);
        used.setStartDate(LocalDate.now().plusDays(30));
        used.setEndDate(LocalDate.now().plusDays(40));

        when(leaveApplicationRepository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(existing));
        when(leaveApplicationRepository.findByUserIdAndIsDeletedFalse(1L))
                .thenReturn(List.of(existing));
        when(leaveApplicationRepository.findByUserIdAndLeaveTypeIdAndIsDeletedFalse(1L, 1L))
                .thenReturn(List.of(existing, used));

        doReturn(15).when(spyService)
                .calculateWorkingDays(any(LocalDate.class), any(LocalDate.class));

        assertThrows(IllegalArgumentException.class, () -> spyService.updateLeave(1L, updatedData));
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
    void deleteById_NotFound_ThrowsException() {
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
    void cancelLeave_AlreadyDeleted_ThrowsException() {
        LeaveApplication leave = new LeaveApplication();
        leave.setId(10L);
        leave.setDeleted(true);

        when(leaveApplicationRepository.findById(10L)).thenReturn(Optional.of(leave));

        assertThrows(IllegalArgumentException.class, () -> leaveService.cancelLeave(10L));
    }

    @Test
    void cancelLeave_StatusNotPending_ThrowsException() {
        LeaveApplication leave = new LeaveApplication();
        leave.setId(10L);
        leave.setDeleted(false);
        leave.setStatus(LeaveStatus.APPROVED);

        when(leaveApplicationRepository.findById(10L)).thenReturn(Optional.of(leave));

        assertThrows(IllegalStateException.class, () -> leaveService.cancelLeave(10L));
    }

}

