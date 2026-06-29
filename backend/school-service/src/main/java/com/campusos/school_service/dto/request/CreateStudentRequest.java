package com.campusos.school_service.dto.request;

import com.campusos.school_service.enums.Gender;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateStudentRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        Gender gender,
        LocalDate dateOfBirth,

        @NotBlank(message = "Class label is required")
        String classLabel,

        String fatherName,
        String motherName,
        String guardianPhone,
        String address,
        String village,
        Boolean hasBus,
        String busPickupPoint,
        LocalDate admissionDate
) {}
