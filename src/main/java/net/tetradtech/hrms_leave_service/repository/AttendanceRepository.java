package net.tetradtech.hrms_leave_service.repository;

import net.tetradtech.hrms_leave_service.Enum.AttendanceStatus;
import net.tetradtech.hrms_leave_service.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {
    Optional<AttendanceRecord> findByUserIdAndDateAndIsDeletedFalse(Long userId, LocalDate date);
    List<AttendanceRecord> findByDateAndIsDeletedFalse(LocalDate date);
    boolean existsByUserIdAndDateAndIsDeletedFalse(Long userId, LocalDate date);
    int countByDateAndIsDeletedFalseAndStatusIn(LocalDate date, List<AttendanceStatus> status);
    Optional<AttendanceRecord> findTopByUserIdAndIsDeletedFalseOrderByDateDesc(Long userId);
    Optional<AttendanceRecord> findByUserIdAndDate(Long userId, LocalDate date);


    @Query("SELECT a FROM AttendanceRecord a WHERE LOWER(a.designation) LIKE " +
            "LOWER(CONCAT('%', :designation, '%')) AND a.isDeleted = false")
    List<AttendanceRecord> findByDesignationLike(@Param("designation") String designation);

    @Query("SELECT a FROM AttendanceRecord a WHERE " +
            "LOWER(a.designation) LIKE LOWER(CONCAT('%', :designation, '%')) AND " +
            "a.date BETWEEN :startDate AND :endDate AND " +
            "a.isDeleted = false")
    List<AttendanceRecord> findByDesignationAndDateRange(@Param("designation") String designation,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);




}