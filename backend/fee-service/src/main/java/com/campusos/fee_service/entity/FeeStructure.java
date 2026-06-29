package com.campusos.fee_service.entity;

import com.campusos.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "fee_structure",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_fs_school_year_class",
                columnNames = {"school_id", "academic_year", "class_label"}),
        indexes = @Index(name = "idx_fs_school", columnList = "school_id")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeStructure extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "academic_year", nullable = false, length = 9)
    private String academicYear;

    /** NULL = applies to all classes. */
    @Column(name = "class_label", length = 15)
    private String classLabel;

    @Builder.Default
    @Column(name = "school_fee_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal schoolFeeAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "bus_fee_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal busFeeAmount = BigDecimal.ZERO;

    @Column(name = "due_date")
    private LocalDate dueDate;
}
