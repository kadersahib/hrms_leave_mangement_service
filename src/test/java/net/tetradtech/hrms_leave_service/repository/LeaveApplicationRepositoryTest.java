package net.tetradtech.hrms_leave_service.repository;

import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Rollback
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // use real DB
class LeaveApplicationRepositoryTest {

    @Autowired
    private LeaveApplicationRepository repository;

    private LeaveApplication leave1;
    private LeaveApplication leave2;

    @BeforeEach
    void setup() {
        repository.deleteAll();

        leave1 = new LeaveApplication();
        leave1.setUserId(1L);
        leave1.setLeaveTypeId(2L);
        leave1.setStartDate(LocalDate.of(2025, 8, 1));
        leave1.setEndDate(LocalDate.of(2025, 8, 3));
        leave1.setDeleted(false);

        leave2 = new LeaveApplication();
        leave2.setUserId(1L);
        leave2.setLeaveTypeId(3L);
        leave2.setStartDate(LocalDate.of(2025, 8, 5));
        leave2.setEndDate(LocalDate.of(2025, 8, 6));
        leave2.setDeleted(false);

        repository.save(leave1);
        repository.save(leave2);
    }

    @Test
    void testFindByIdAndIsDeletedFalse() {
        Optional<LeaveApplication> result = repository.findByIdAndIsDeletedFalse(leave1.getId());
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getUserId());
    }

    @Test
    void testFindByUserIdAndLeaveTypeIdAndIsDeletedFalse() {
        List<LeaveApplication> result = repository.findByUserIdAndLeaveTypeIdAndIsDeletedFalse(1L, 2L);
        assertEquals(1, result.size());
        assertEquals(2L, result.getFirst().getLeaveTypeId());
    }

    @Test
    void testFindByUserIdAndIsDeletedFalse() {
        List<LeaveApplication> result = repository.findByUserIdAndIsDeletedFalse(1L);
        assertEquals(2, result.size());
    }

    @Test
    void testFindByIsDeletedFalse() {
        List<LeaveApplication> result = repository.findByIsDeletedFalse();
        assertEquals(2, result.size());
    }

    @Test
    void testFindAllByUserIdAndIsDeletedFalse() {
        List<LeaveApplication> result = repository.findAllByUserIdAndIsDeletedFalse(1L);
        assertEquals(2, result.size());
    }

    @Test
    void testExistsByUserIdAndLeaveTypeIdAndStartDateBetween() {
        boolean exists = repository.existsByUserIdAndLeaveTypeIdAndStartDateBetween(
                1L, 2L, LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 3));
        assertTrue(exists);
    }

    @Test
    void testCountOverlappingLeaves() {
        long count = repository.countOverlappingLeaves(
                1L,
                LocalDate.of(2025, 8, 2),  // falls inside leave1 (2025-08-01 to 2025-08-03)
                LocalDate.of(2025, 8, 2)
        );
        assertEquals(1L, count); // One leave overlaps, assert with long literal
    }

    @Test
    void testCountOverlappingLeavesExcludingId() {
        int count = repository.countOverlappingLeavesForUpdate(
                1L,
                leave1.getId(),  // exclude leave1
                LocalDate.of(2025, 8, 2),
                LocalDate.of(2025, 8, 2)
        );
        assertEquals(0, count); // Excluding leave1 => 0 overlaps
    }

    @Test
    void testGetTotalUsedDaysForYear() {
        leave1.setAppliedDays(3);
        leave2.setAppliedDays(2);
        repository.save(leave1);
        repository.save(leave2);

        int totalDays = repository.getTotalUsedDaysForYear(1L, 2L, 2025);
        assertEquals(3, totalDays);
    }

    @Test
    void testGetTotalUsedDaysForYearExcludingId() {
        leave1.setAppliedDays(3);
        leave2.setAppliedDays(2);
        repository.save(leave1);
        repository.save(leave2);

        int totalDays = repository.getTotalUsedDaysForYearExcludingId(
                1L, 2L, 2025, leave1.getId());
        assertEquals(0, totalDays); // leave1 excluded
    }

}
