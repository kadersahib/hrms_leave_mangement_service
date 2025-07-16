
package net.tetradtech.hrms_leave_service.repository;

import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.Enum.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {
//    Optional<LeaveApplication> // Return type: may or may not find a result
//            findTopBy                  // "Top" means get the first record after sorting
//    UserIdAndIsDeletedFalse    // WHERE user_id = ? AND is_deleted = false
//    OrderByCreatedAtDesc();    // ORDER BY created_at DESC (most recent first)

    Optional<LeaveApplication> findTopByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);
    List<LeaveApplication> findByUserIdAndIsDeletedFalse(Long userId);
    List<LeaveApplication> findByIsDeletedFalse();
    Optional<LeaveApplication> findByIdAndIsDeletedFalse(Long id);

    //status validation
    List<LeaveApplication> findByStatusAndIsDeletedFalse(LeaveStatus status);
    List<LeaveApplication> findByLeaveTypeIdAndIsDeletedFalse(Long leaveTypeId);
    List<LeaveApplication> findByUserIdAndStatusAndIsDeletedFalse(Long userId, LeaveStatus status);
    boolean existsByUserIdAndIsDeletedFalse(Long userId);
    List<LeaveApplication> findByUserIdAndStartDateGreaterThanEqualAndEndDateLessThanEqualAndIsDeletedFalse(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<LeaveApplication> findByUserIdAndLeaveTypeIdAndIsDeletedFalse(Long userId, Long leaveTypeId);


    @Query("SELECT l FROM LeaveApplication l WHERE l.userId = :userId AND l.status = 'APPROVED' AND l.startDate <= :end AND l.endDate >= :start")
    List<LeaveApplication> findApprovedLeavesByUserIdAndDateRange(Long userId, LocalDate start, LocalDate end);


    List<LeaveApplication> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);


}
