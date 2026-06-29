package com.campusos.school_service.service;

import com.campusos.common_lib.contract.SchoolSummary;
import com.campusos.school_service.dto.request.CreateSchoolAdminRequest;
import com.campusos.school_service.dto.request.CreateSchoolRequest;
import com.campusos.school_service.dto.request.UpdateSchoolRequest;
import com.campusos.school_service.dto.response.SchoolAdminResponse;
import com.campusos.school_service.dto.response.SchoolOverview;
import com.campusos.school_service.dto.response.SchoolReport;
import com.campusos.school_service.dto.response.SchoolResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SchoolService {

    SchoolResponse createSchool(CreateSchoolRequest request);

    List<SchoolResponse> listSchools();

    SchoolResponse getSchool(UUID id);

    SchoolSummary getSchoolSummary(UUID id);

    SchoolResponse updateSchool(UUID id, UpdateSchoolRequest request);

    SchoolResponse updateLogo(UUID id, String logoUrl);

    SchoolAdminResponse createSchoolAdmin(UUID schoolId, CreateSchoolAdminRequest request);

    SchoolAdminResponse getSchoolAdmin(UUID schoolId);

    List<SchoolOverview> overview();

    /** Cross-school report: counts + attendance-% + fee-collection-% per school. */
    List<SchoolReport> reports(LocalDate from, LocalDate to);
}
