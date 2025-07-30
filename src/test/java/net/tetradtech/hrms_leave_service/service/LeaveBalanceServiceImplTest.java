package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
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

    // Test getAllLeaves() with supported type (Sick Leave)
    @Test
    void testGetAllLeaves_Success() {
        LeaveApplication leave = new LeaveApplication();
        leave.setUserId(1L);
        leave.setLeaveTypeId(1L); // Sick Leave
        leave.setAppliedDays(5);
        leave.setDeleted(false);

        when(leaveApplicationRepository.findByIsDeletedFalse()).thenReturn(List.of(leave));

        List<Map<String, Object>> result = leaveBalanceService.getAllLeaves();

        assertEquals(1, result.size());
        Map<String, Object> userData = result.getFirst();
        assertEquals(1L, userData.get("userId"));

        Map<String, Object> leaveData = (Map<String, Object>) userData.get("Sick Leave");
        assertEquals(5, leaveData.get("totalApplied"));
        assertEquals(25, leaveData.get("balanceDays")); // 30 - 5

        verify(leaveApplicationRepository, times(1)).findByIsDeletedFalse();
    }

    //  Test getAllLeaves() filters maternity/paternity
    @Test
    void testGetAllLeaves_FiltersUnsupportedTypes() {
        LeaveApplication maternityLeave = new LeaveApplication();
        maternityLeave.setUserId(2L);
        maternityLeave.setLeaveTypeId(3L); // Maternity
        maternityLeave.setAppliedDays(10);
        maternityLeave.setDeleted(false);

        when(leaveApplicationRepository.findByIsDeletedFalse()).thenReturn(List.of(maternityLeave));

        List<Map<String, Object>> result = leaveBalanceService.getAllLeaves();

        // Since isSupported() excludes 3L, result should be empty
        assertEquals(0, result.size());
    }

    // Test getLeavesByUserId() success with Personal Leave
    @Test
    void testGetLeavesByUserId_Success() {
        LeaveApplication leave = new LeaveApplication();
        leave.setUserId(2L);
        leave.setLeaveTypeId(2L); // Personal Leave
        leave.setAppliedDays(8);
        leave.setDeleted(false);

        when(userServiceClient.getUserById(2L)).thenReturn(new UserDTO());
        when(leaveApplicationRepository.findByUserIdAndIsDeletedFalse(2L)).thenReturn(List.of(leave));

        List<Map<String, Object>> result = leaveBalanceService.getLeavesByUserId(2L);

        assertEquals(1, result.size());
        Map<String, Object> userData = result.getFirst();
        assertEquals(2L, userData.get("userId"));

        Map<String, Object> leaveData = (Map<String, Object>) userData.get("Personal Leave");
        assertEquals(8, leaveData.get("totalApplied"));
        assertEquals(12, leaveData.get("balanceDays")); // 20 - 8

        verify(userServiceClient, times(1)).getUserById(2L);
    }

    @Test
    void testGetLeavesByUserId_UserNotFound() {
        when(userServiceClient.getUserById(99L)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> leaveBalanceService.getLeavesByUserId(99L));

        assertTrue(ex.getMessage().contains("User with ID 99 not found"));
    }

    @Test
    void testGetLeavesByUserId_NoLeaves() {
        when(userServiceClient.getUserById(2L)).thenReturn(new UserDTO());
        when(leaveApplicationRepository.findByUserIdAndIsDeletedFalse(2L)).thenReturn(Collections.emptyList());

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> leaveBalanceService.getLeavesByUserId(2L));

        assertTrue(ex.getMessage().contains("No leave data found for user ID 2"));
    }

    @Test
    void testGetLeaveBalanceByUserIdAndLeaveType_Success() {
        LeaveApplication leave = new LeaveApplication();
        leave.setUserId(3L);
        leave.setLeaveTypeId(1L); // Sick Leave
        leave.setAppliedDays(10);
        leave.setDeleted(false);

        when(userServiceClient.getUserById(3L)).thenReturn(new UserDTO());
        when(leaveApplicationRepository.findByUserIdAndLeaveTypeIdAndIsDeletedFalse(3L, 1L))
                .thenReturn(List.of(leave));

        Map<String, Object> result = leaveBalanceService.getLeaveBalanceByUserIdAndLeaveType(3L, 1L);

        assertEquals("Sick Leave", result.get("leaveTypeName"));
        assertEquals(10, result.get("totalApplied"));
        assertEquals(20, result.get("balanceDays")); // 30 - 10

        verify(leaveApplicationRepository, times(1))
                .findByUserIdAndLeaveTypeIdAndIsDeletedFalse(3L, 1L);
    }

    @Test
    void testGetLeaveBalanceByUserIdAndLeaveType_UserNotFound() {
        when(userServiceClient.getUserById(5L)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> leaveBalanceService.getLeaveBalanceByUserIdAndLeaveType(5L, 1L));

        assertTrue(ex.getMessage().contains("User with ID 5 not found"));
    }

    @Test
    void testGetLeaveBalanceByUserIdAndLeaveType_NoData() {
        when(userServiceClient.getUserById(6L)).thenReturn(new UserDTO());
        when(leaveApplicationRepository.findByUserIdAndLeaveTypeIdAndIsDeletedFalse(6L, 1L))
                .thenReturn(Collections.emptyList());

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> leaveBalanceService.getLeaveBalanceByUserIdAndLeaveType(6L, 1L));

        assertTrue(ex.getMessage().contains("No leave data found for user ID 6 and leaveTypeId 1"));
    }
}
