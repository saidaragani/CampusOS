package com.campusos.school_service.dto.response;

import com.campusos.school_service.enums.Gender;

import java.time.LocalDate;
import java.util.UUID;

public record StudentResponse(
        UUID id,
        UUID schoolId,
        String admissionNo,
        String fullName,
        Gender gender,
        LocalDate dateOfBirth,
        String classLabel,
        String fatherName,
        String motherName,
        String guardianPhone,
        String address,
        String village,
        String photoUrl,
        Boolean hasBus,
        String busPickupPoint,
        Boolean active,
        LocalDate admissionDate
) {}
