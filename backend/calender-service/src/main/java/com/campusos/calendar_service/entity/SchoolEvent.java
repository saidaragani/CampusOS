package com.campusos.calendar_service.entity;

import com.campusos.calendar_service.enums.EventType;
import com.campusos.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "school_event", indexes = @Index(name = "idx_event_school_date", columnList = "school_id,event_date"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "event_type", nullable = false, length = 15)
    private EventType eventType = EventType.OTHER;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;
}
