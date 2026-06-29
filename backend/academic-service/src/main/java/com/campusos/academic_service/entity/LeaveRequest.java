package com.campusos.academic_service.entity;

import com.campusos.academic_service.enums.LeaveStatus;
import com.campusos.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "leave_request",
        indexes = {
                @Index(name = "idx_leave_class_status", columnList = "school_id,class_label,status"),
                @Index(name = "idx_leave_student", columnList = "student_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "class_label", nullable = false, length = 15)
    private String classLabel;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 10)
    private LeaveStatus status = LeaveStatus.PENDING;

    @Column(name = "requested_by_parent_user_id", nullable = false)
    private UUID requestedByParentUserId;

    @Column(name = "decided_by_teacher_id")
    private UUID decidedByTeacherId;

    @Column(name = "decision_note", length = 500)
    private String decisionNote;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;
}
