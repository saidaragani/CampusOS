package com.campusos.fee_service.service;

import com.campusos.fee_service.dto.request.GenerateFeesRequest;
import com.campusos.fee_service.dto.request.MarkPaidRequest;
import com.campusos.fee_service.dto.response.FeeSummaryResponse;
import com.campusos.fee_service.dto.response.StudentFeeResponse;
import com.campusos.fee_service.enums.FeeStatus;
import com.campusos.fee_service.enums.FeeType;
import com.campusos.common_lib.contract.SchoolFeeStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StudentFeeService {

    /** Generates SCHOOL (+ BUS for bus students) fee rows for a class+year. Returns how many were created. */
    int generate(UUID schoolId, GenerateFeesRequest request);

    Page<StudentFeeResponse> list(UUID schoolId, String classLabel, FeeStatus status, FeeType feeType, Pageable pageable);

    FeeSummaryResponse summary(UUID schoolId);

    StudentFeeResponse markPaid(UUID schoolId, UUID userId, UUID feeId, MarkPaidRequest request);

    StudentFeeResponse markPending(UUID schoolId, UUID userId, UUID feeId);

    java.util.List<StudentFeeResponse> studentFees(UUID parentUserId, UUID studentId);

    /** Internal: aggregate fee collection for a school (cross-school reports). */
    SchoolFeeStats getSchoolStats(UUID schoolId);
}
