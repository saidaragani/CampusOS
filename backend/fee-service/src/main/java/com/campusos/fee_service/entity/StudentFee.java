package com.campusos.fee_service.entity;

import com.campusos.common_lib.entity.BaseEntity;
import com.campusos.fee_service.enums.FeeStatus;
import com.campusos.fee_service.enums.FeeType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "student_fee",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_sf_student_year_type",
                columnNames = {"student_id", "academic_year", "fee_type"}),
        indexes = {
                @Index(name = "idx_sf_school_status", columnList = "school_id,status"),
                @Index(name = "idx_sf_student", columnList = "student_id"),
                @Index(name = "idx_sf_due", columnList = "status,due_date")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentFee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    /** Denormalized class label at generation time, to filter fees by class. */
    @Column(name = "class_label", length = 15)
    private String classLabel;

    @Column(name = "academic_year", nullable = false, length = 9)
    private String academicYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false, length = 10)
    private FeeType feeType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 10)
    private FeeStatus status = FeeStatus.PENDING;

    @Column(name = "paid_on")
    private LocalDate paidOn;

    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "payment_note", length = 255)
    private String paymentNote;

    @Column(name = "updated_by_user_id")
    private UUID updatedByUserId;
}
