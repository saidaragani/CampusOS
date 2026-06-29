package com.campusos.fee_service.mapper;

import com.campusos.fee_service.dto.response.FeeStructureResponse;
import com.campusos.fee_service.dto.response.StudentFeeResponse;
import com.campusos.fee_service.entity.FeeStructure;
import com.campusos.fee_service.entity.StudentFee;

public final class FeeMappers {

    private FeeMappers() {
    }

    public static FeeStructureResponse toStructureResponse(FeeStructure s) {
        return new FeeStructureResponse(s.getId(), s.getSchoolId(), s.getAcademicYear(), s.getClassLabel(),
                s.getSchoolFeeAmount(), s.getBusFeeAmount(), s.getDueDate());
    }

    public static StudentFeeResponse toStudentFeeResponse(StudentFee f) {
        return new StudentFeeResponse(f.getId(), f.getSchoolId(), f.getStudentId(), f.getClassLabel(),
                f.getAcademicYear(), f.getFeeType(), f.getAmount(), f.getDueDate(), f.getStatus(),
                f.getPaidOn(), f.getPaidAmount(), f.getPaymentNote());
    }
}
