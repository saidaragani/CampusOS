package com.campusos.fee_service.service;

import com.campusos.fee_service.dto.request.FeeStructureRequest;
import com.campusos.fee_service.dto.request.UpdateFeeStructureRequest;
import com.campusos.fee_service.dto.response.FeeStructureResponse;

import java.util.List;
import java.util.UUID;

public interface FeeStructureService {

    FeeStructureResponse create(UUID schoolId, FeeStructureRequest request);

    List<FeeStructureResponse> list(UUID schoolId);

    FeeStructureResponse update(UUID schoolId, UUID id, UpdateFeeStructureRequest request);
}
