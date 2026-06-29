package com.campusos.academic_service.entity;

import com.campusos.academic_service.enums.AttendanceSession;
import com.campusos.academic_service.enums.AttendanceStatus;
import com.campusos.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "attendance",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_att_student_date_session",
                columnNames = {"student_id", "attendance_date", "session"}),
        indexes = {
                @Index(name = "idx_att_class_date", columnList = "school_id,class_label,attendance_date,session"),
                @Index(name = "idx_att_school_date", columnList = "school_id,attendance_date"),
                @Index(name = "idx_att_student", columnList = "student_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "class_label", nullable = false, length = 15)
    private String classLabel;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AttendanceSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AttendanceStatus status;

    @Column(name = "marked_by_teacher_id", nullable = false)
    private UUID markedByTeacherId;

    @Column(name = "marked_at", nullable = false)
    private LocalDateTime markedAt;
}
