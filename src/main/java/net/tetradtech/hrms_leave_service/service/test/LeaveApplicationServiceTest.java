//package net.tetradtech.hrms_leave_service.service.test;
//
//
//import net.tetradtech.hrms_leave_service.client.UserServiceClient;
//import net.tetradtech.hrms_leave_service.dto.LeaveRequestDTO;
//import net.tetradtech.hrms_leave_service.dto.UserDTO;
//import net.tetradtech.hrms_leave_service.model.LeaveApplication;
//import net.tetradtech.hrms_leave_service.mapper.LeaveApplicationMapper;
//import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
//
//import net.tetradtech.hrms_leave_service.service.LeaveApplicationService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.time.LocalDate;
//import java.util.Collections;
//
//import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//public class LeaveApplicationServiceT   est {
//
//    @Mock
//    private UserServiceClient userServiceClient;
//
//    @Mock
//    private LeaveApplicationRepository leaveApplicationRepository;
//
//    @Mock
//    private LeaveApplicationMapper leaveApplicationMapper;
//
//    @InjectMocks
//    private LeaveApplicationService leaveApplicationService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testApplyLeave_FirstTime_Success() {
//        // Arrange
//        Long userId = 1L;
//        String leaveType = "Sick Leave";
//        LocalDate startDate = LocalDate.now().plusDays(1);
//        LocalDate endDate = LocalDate.now().plusDays(3);
//
//        LeaveRequestDTO request = new LeaveRequestDTO();
//        request.setUserId(userId);
//        request.setLeaveTypeName(leaveType);
//        request.setStartDate(startDate);
//        request.setEndDate(endDate);
//
//        UserDTO userDTO = new UserDTO();
//        userDTO.setId(userId);
//
//        when(userServiceClient.getUserById(userId)).thenReturn(userDTO);
//        when(leaveApplicationRepository.findByUserIdAndIsDeletedFalse(userId)).thenReturn(Collections.emptyList());
//        when(leaveApplicationRepository.findByUserIdAndLeaveTypeNameAndIsDeletedFalse(userId, leaveType)).thenReturn(null);
//
//        LeaveApplication newLeave = new LeaveApplication();
//        newLeave.setUserId(userId);
//        newLeave.setLeaveTypeName(leaveType);
//        newLeave.setStartDate(startDate);
//        newLeave.setEndDate(endDate);
//        newLeave.setRemainingDays(17); // 20 - 3
//
//        when(leaveApplicationMapper.toNewLeaveApplication(any(), anyInt(), anyInt())).thenReturn(newLeave);
//        when(leaveApplicationRepository.save(any())).thenReturn(newLeave);
//
//        // Act
//        LeaveApplication result = leaveApplicationService.applyLeave(request);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(userId, result.getUserId());
//        assertEquals("Sick Leave", result.getLeaveTypeName());
//        assertEquals(17, result.getRemainingDays());
//
//        verify(userServiceClient).getUserById(userId);
//        verify(leaveApplicationRepository).save(newLeave);
//    }
//
//    @Test
//    void testApplyLeave_InvalidUser_ThrowsException() {
//        LeaveRequestDTO request = new LeaveRequestDTO();
//        request.setUserId(99L);
//        when(userServiceClient.getUserById(99L)).thenReturn(null);
//
//        Exception ex = assertThrows(IllegalArgumentException.class,
//                () -> leaveApplicationService.applyLeave(request));
//
//        assertEquals("Invalid user ID: 99", ex.getMessage());
//    }
//
//    @Test
//    void testApplyLeave_DateInPast_ThrowsException() {
//        LeaveRequestDTO request = new LeaveRequestDTO();
//        request.setUserId(1L);
//        request.setStartDate(LocalDate.now().minusDays(1));
//        request.setEndDate(LocalDate.now().plusDays(1));
//
//        when(userServiceClient.getUserById(1L)).thenReturn(new UserDTO());
//
//        Exception ex = assertThrows(IllegalArgumentException.class,
//                () -> leaveApplicationService.applyLeave(request));
//
//        assertEquals("Start date cannot be in the past.", ex.getMessage());
//    }
//
//    @Test
//    void testApplyLeave_LeaveAlreadyExists_ThrowsException() {
//        LeaveRequestDTO request = new LeaveRequestDTO();
//        request.setUserId(1L);
//        request.setStartDate(LocalDate.now().plusDays(1));
//        request.setEndDate(LocalDate.now().plusDays(3));
//        request.setLeaveTypeName("Sick Leave");
//
//        LeaveApplication existing = new LeaveApplication();
//        existing.setStartDate(request.getStartDate());
//        existing.setEndDate(request.getEndDate());
//
//        when(userServiceClient.getUserById(1L)).thenReturn(new UserDTO());
//        when(leaveApplicationRepository.findByUserIdAndIsDeletedFalse(1L)).thenReturn(Collections.singletonList(existing));
//
//        Exception ex = assertThrows(IllegalArgumentException.class,
//                () -> leaveApplicationService.applyLeave(request));
//
//        assertEquals("Leave already applied for the same date range.", ex.getMessage());
//    }
//}
