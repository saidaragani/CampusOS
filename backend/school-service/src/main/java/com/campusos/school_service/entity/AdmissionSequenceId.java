package com.campusos.school_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AdmissionSequenceId implements Serializable {

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "academic_year", nullable = false, length = 9)
    private String academicYear;
}
