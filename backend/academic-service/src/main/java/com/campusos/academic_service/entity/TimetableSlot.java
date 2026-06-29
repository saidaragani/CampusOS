package com.campusos.academic_service.entity;

import com.campusos.academic_service.enums.Weekday;
import com.campusos.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
        name = "timetable_slot",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_tt_class_day_period",
                columnNames = {"school_id", "class_label", "day_of_week", "period_no"}),
        indexes = @Index(name = "idx_tt_teacher", columnList = "teacher_id")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableSlot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "class_label", nullable = false, length = 15)
    private String classLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 3)
    private Weekday dayOfWeek;

    @Column(name = "period_no", nullable = false)
    private int periodNo;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "teacher_id")
    private UUID teacherId;
}
