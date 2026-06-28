package com.campusos.auth_service.entity;

import com.campusos.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "parent_student_links",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_school_admission",
                        columnNames = {"school_id", "admission_no"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentStudentLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_user_id", nullable = false)
    private User parentUser;

    @Column(nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private Long schoolId;

    @Column(name = "admission_no", nullable = false, length = 40)
    private String admissionNo;
}