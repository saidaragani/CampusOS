package com.campusos.school_service.dto.request;

import com.campusos.school_service.enums.Gender;

import java.time.LocalDate;

public record UpdateStudentRequest(
        String fullName,
        Gender gender,
        LocalDate dateOfBirth,
        String classLabel,
        String fatherName,
        String motherName,
        String guardianPhone,
        String address,
        String village,
        Boolean hasBus,
        String busPickupPoint,
        Boolean active
) {}
