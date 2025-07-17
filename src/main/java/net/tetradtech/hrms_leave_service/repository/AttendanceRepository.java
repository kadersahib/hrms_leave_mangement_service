package net.tetradtech.hrms_leave_service.repository;

import net.tetradtech.hrms_leave_service.Enum.AttendanceStatus;
import net.tetradtech.hrms_leave_service.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {
    Optional<AttendanceRecord> findByUserIdAndDateAndIsDeletedFalse(Long userId, LocalDate date);
    List<AttendanceRecord> findByDateAndIsDeletedFalse(LocalDate date);
    boolean existsByUserIdAndDateAndIsDeletedFalse(Long userId, LocalDate date);
    int countByDateAndIsDeletedFalseAndStatusIn(LocalDate date, List<AttendanceStatus> status);
    Optional<AttendanceRecord> findTopByUserIdAndIsDeletedFalseOrderByDateDesc(Long userId);

}