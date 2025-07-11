package net.tetradtech.hrms_leave_service.repository;

import net.tetradtech.hrms_leave_service.model.LeaveApproval;
import org.springframework.data.jpa.repository.JpaRepository;


public interface LeaveApprovalRepository extends JpaRepository<LeaveApproval, Long> {
}
