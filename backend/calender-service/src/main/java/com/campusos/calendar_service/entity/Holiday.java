package com.campusos.calendar_service.entity;

import com.campusos.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "holiday", indexes = @Index(name = "idx_holiday_school_date", columnList = "school_id,from_date"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Holiday extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(length = 500)
    private String description;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;
}
