package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.dto.LeaveBalanceDTO;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveBalanceServiceImplTest {

    @InjectMocks
    private LeaveBalanceServiceImpl leaveBalanceService;

    @Mock
    private LeaveApplicationRepository leaveApplicationRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Test
    void testGetAllLeaves() {
        LeaveApplication leave = new LeaveApplication();
        leave.setUserId(1L);
        leave.setLeaveTypeId(100L);
        leave.setRemainingDays(5);
        leave.setTotalLeaveDays(10);

        when(leaveApplicationRepository.findByIsDeletedFalse()).thenReturn(List.of(leave));

        UserDTO user = new UserDTO();
        user.setName("John");
        when(userServiceClient.getUserById(1L)).thenReturn(user);

        List<LeaveBalanceDTO> result = leaveBalanceService.getAllLeaves();

        assertEquals(1, result.size());
        assertEquals("John", result.getFirst().getName());
        assertEquals(100L, result.getFirst().getLeaveId());
        verify(leaveApplicationRepository, times(1)).findByIsDeletedFalse();
    }

    @Test
    void testGetLeavesByUserId_Success() {
        LeaveApplication leave = new LeaveApplication();
        leave.setUserId(2L);
        leave.setLeaveTypeId(200L);
        leave.setRemainingDays(8);
        leave.setTotalLeaveDays(15);

        when(leaveApplicationRepository.findByUserIdAndIsDeletedFalse(2L)).thenReturn(List.of(leave));

        UserDTO user = new UserDTO();
        user.setName("Alice");
        when(userServiceClient.getUserById(2L)).thenReturn(user);

        List<LeaveBalanceDTO> result = leaveBalanceService.getLeavesByUserId(2L);

        assertEquals(1, result.size());
        assertEquals("Alice", result.getFirst().getName());
        assertEquals(200L, result.getFirst().getLeaveId());
        verify(userServiceClient, times(1)).getUserById(2L);
    }

    @Test
    void testGetLeavesByUserId_UserNotFound() {
        when(userServiceClient.getUserById(99L)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                leaveBalanceService.getLeavesByUserId(99L));

        assertEquals("User with ID 99 not found", ex.getMessage());
    }

    @Test
    void testGetLeaveBalanceByUserIdAndLeaveType_Success() {
        LeaveApplication leave = new LeaveApplication();
        leave.setUserId(3L);
        leave.setLeaveTypeId(300L);
        leave.setRemainingDays(6);
        leave.setTotalLeaveDays(12);

        when(leaveApplicationRepository.findAllByUserIdAndIsDeletedFalse(3L)).thenReturn(List.of(leave));

        UserDTO user = new UserDTO();
        user.setName("Bob");
        when(userServiceClient.getUserById(3L)).thenReturn(user);

        List<LeaveBalanceDTO> result = leaveBalanceService.getLeaveBalanceByUserIdAndLeaveType(3L, 300L);

        assertEquals(1, result.size());
        assertEquals("Bob", result.getFirst().getName());
        assertEquals(300L, result.getFirst().getLeaveId());
    }

    @Test
    void testGetLeaveBalanceByUserIdAndLeaveType_UserNotFound() {
        when(userServiceClient.getUserById(5L)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                leaveBalanceService.getLeaveBalanceByUserIdAndLeaveType(5L, 500L));

        assertEquals("User ID not found: 5", ex.getMessage());
    }

    @Test
    void testGetLeaveBalanceByUserIdAndLeaveType_NoLeavesFound() {
        UserDTO user = new UserDTO();
        user.setName("Charlie");
        when(userServiceClient.getUserById(6L)).thenReturn(user);

        when(leaveApplicationRepository.findAllByUserIdAndIsDeletedFalse(6L)).thenReturn(Collections.emptyList());

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                leaveBalanceService.getLeaveBalanceByUserIdAndLeaveType(6L, 600L));

        assertEquals("No leaves found for leaveTypeId: 600", ex.getMessage());
    }
}
