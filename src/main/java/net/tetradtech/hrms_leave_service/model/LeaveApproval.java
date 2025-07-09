package net.tetradtech.hrms_leave_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "leave_approval")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long leaveId;

    private String action; // APPROVED or REJECTED
    private String performedBy;

    private String comment;

    private LocalDateTime timestamp;

    // Audit fields
    private LocalDateTime createdAt;
    private String createdBy;

    private LocalDateTime updatedAt;
    private String updatedBy;

    // Soft delete fields
    private boolean isDeleted;
    private LocalDateTime deletedAt;
    private String deletedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.timestamp = LocalDateTime.now(); // setting action timestamp also
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void softDelete(String deletedByUser) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedByUser;
    }
}
