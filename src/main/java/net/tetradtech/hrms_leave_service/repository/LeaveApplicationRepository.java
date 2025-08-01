
package net.tetradtech.hrms_leave_service.repository;

import net.tetradtech.hrms_leave_service.model.LeaveApplication;
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


    @Query("SELECT COUNT(l) FROM LeaveApplication l " +
            "WHERE l.userId = :userId AND l.isDeleted = false " +
            "AND l.startDate <= :endDate AND l.endDate >= :startDate")
    long countOverlappingLeaves(@Param("userId") Long userId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);


    @Query("SELECT COUNT(l) FROM LeaveApplication l " +
            "WHERE l.userId = :userId " +
            "AND l.status NOT IN ('CANCELLED', 'REJECTED') " +
            "AND l.startDate <= :endDate " +
            "AND l.endDate >= :startDate " +
            "AND l.id <> :excludeId")
    int countOverlappingLeavesForUpdate(@Param("userId") Long userId,
                                        @Param("excludeId") Long excludeId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);



    @Query("SELECT COALESCE(SUM(l.appliedDays), 0) FROM LeaveApplication l " +
            "WHERE l.userId = :userId AND l.leaveTypeId = :leaveTypeId " +
            "AND l.status IN ('PENDING', 'APPROVED') " +
            "AND YEAR(l.startDate) = :year")
    int getTotalUsedDaysForYear(@Param("userId") Long userId,
                                @Param("leaveTypeId") Long leaveTypeId,
                                @Param("year") int year);

    @Query("SELECT COALESCE(SUM(l.appliedDays), 0) FROM LeaveApplication l " +
            "WHERE l.userId = :userId AND l.leaveTypeId = :leaveTypeId " +
            "AND l.status IN ('PENDING', 'APPROVED') " +
            "AND YEAR(l.startDate) = :year AND l.id <> :excludeId")
    int getTotalUsedDaysForYearExcludingId(@Param("userId") Long userId,
                                           @Param("leaveTypeId") Long leaveTypeId,
                                           @Param("year") int year,
                                           @Param("excludeId") Long excludeId);


}
