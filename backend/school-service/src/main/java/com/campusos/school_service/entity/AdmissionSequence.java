package com.campusos.school_service.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * One row per (school, academic year). Locked on read during admission so the
 * per-school running number increments without gaps or collisions.
 */
@Entity
@Table(name = "admission_sequence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmissionSequence {

    @EmbeddedId
    private AdmissionSequenceId id;

    @Column(name = "last_number", nullable = false)
    private int lastNumber;
}
