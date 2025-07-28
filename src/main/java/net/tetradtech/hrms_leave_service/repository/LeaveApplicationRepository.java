
package net.tetradtech.hrms_leave_service.repository;

import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.constants.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {


    Optional<LeaveApplication> findByIdAndIsDeletedFalse(Long id);

    List<LeaveApplication> findByUserIdAndLeaveTypeIdAndIsDeletedFalse(Long userId, Long leaveTypeId);
    List<LeaveApplication> findByUserIdAndIsDeletedFalse(Long userId);
    List<LeaveApplication> findByIsDeletedFalse();
    List<LeaveApplication> findAllByUserIdAndIsDeletedFalse(Long userId);


    boolean existsByUserIdAndLeaveTypeIdAndStartDateBetween(
            Long userId, Long leaveTypeId, LocalDate start, LocalDate end);


}
