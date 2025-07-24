
package net.tetradtech.hrms_leave_service.repository;

import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.constants.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {


    Optional<LeaveApplication> findByIdAndIsDeletedFalse(Long id);
    Optional<LeaveApplication> findByUserIdAndLeaveTypeNameIgnoreCaseAndIsDeletedFalse(Long userId, String leaveTypeName);

    LeaveApplication findByUserIdAndLeaveTypeNameAndIsDeletedFalse(Long userId, String leaveTypeName);
    List<LeaveApplication> findByUserIdAndIsDeletedFalse(Long userId);
    List<LeaveApplication> findByIsDeletedFalse();
    List<LeaveApplication> findAllByUserIdAndIsDeletedFalse(Long userId);








//    @Query("SELECT COALESCE(SUM(la.appliedDays), 0) FROM LeaveApplication la " +
//            "WHERE la.userId = :userId AND la.status = 'APPROVED' " +
//            "AND YEAR(la.startDate) = :year AND la.isDeleted = false")
//    long sumApprovedLeaveDaysByUserIdAndYear(@Param("userId") Long userId,
//                                             @Param("year") int year);


//    Optional<LeaveApplication> findByIdAndIsDeletedFalse(Long id);

    //status validation
    List<LeaveApplication> findByStatusAndIsDeletedFalse(LeaveStatus status);
//    List<LeaveApplication> findByLeaveTypeIdAndIsDeletedFalse(Long leaveTypeId);
    List<LeaveApplication> findByUserIdAndStatusAndIsDeletedFalse(Long userId, LeaveStatus status);
    boolean existsByUserIdAndIsDeletedFalse(Long userId);
    List<LeaveApplication> findByUserIdAndStartDateGreaterThanEqualAndEndDateLessThanEqualAndIsDeletedFalse(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

//    List<LeaveApplication> findByUserIdAndLeaveTypeIdAndIsDeletedFalse(Long userId, Long leaveTypeId);
//    Optional<LeaveApplication> findByUserIdAndLeaveTypeId(Long userId, Long leaveTypeId);
//
//
//    @Query("SELECT l FROM LeaveApplication l WHERE l.userId = :userId AND l.status = 'APPROVED' AND l.startDate <= :end AND l.endDate >= :start")
//    List<LeaveApplication> findApprovedLeavesByUserIdAndDateRange(Long userId, LocalDate start, LocalDate end);
//
//
//    List<LeaveApplication> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);
//
//    @Query("SELECT l FROM LeaveApplication l WHERE l.userId = :userId " +
//            "AND :today BETWEEN l.startDate AND l.endDate " +
//            "AND l.status = :status " +
//            "AND l.isDeleted = false")
//    List<LeaveApplication> findApprovedLeavesForToday(@Param("userId") Long userId,
//                                                      @Param("today") LocalDate today,
//                                                      @Param("status") LeaveStatus status);

}
