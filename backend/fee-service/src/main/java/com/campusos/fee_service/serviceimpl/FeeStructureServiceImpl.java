package com.campusos.fee_service.serviceimpl;

import com.campusos.fee_service.dto.request.FeeStructureRequest;
import com.campusos.fee_service.dto.request.UpdateFeeStructureRequest;
import com.campusos.fee_service.dto.response.FeeStructureResponse;
import com.campusos.fee_service.entity.FeeStructure;
import com.campusos.fee_service.exception.DuplicateResourceException;
import com.campusos.fee_service.exception.ResourceNotFoundException;
import com.campusos.fee_service.mapper.FeeMappers;
import com.campusos.fee_service.repository.FeeStructureRepository;
import com.campusos.fee_service.service.FeeStructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeeStructureServiceImpl implements FeeStructureService {

    private final FeeStructureRepository feeStructureRepository;

    @Override
    @Transactional
    public FeeStructureResponse create(UUID schoolId, FeeStructureRequest request) {
        String classLabel = (request.classLabel() != null && !request.classLabel().isBlank())
                ? request.classLabel().trim() : null;

        Optional<FeeStructure> existing = (classLabel == null)
                ? feeStructureRepository.findBySchoolIdAndAcademicYearAndClassLabelIsNull(schoolId, request.academicYear())
                : feeStructureRepository.findBySchoolIdAndAcademicYearAndClassLabel(schoolId, request.academicYear(), classLabel);
        if (existing.isPresent()) {
            throw new DuplicateResourceException("A fee structure already exists for that year/class.");
        }

        FeeStructure structure = FeeStructure.builder()
                .schoolId(schoolId)
                .academicYear(request.academicYear())
                .classLabel(classLabel)
                .schoolFeeAmount(orZero(request.schoolFeeAmount()))
                .busFeeAmount(orZero(request.busFeeAmount()))
                .dueDate(request.dueDate())
                .build();
        return FeeMappers.toStructureResponse(feeStructureRepository.save(structure));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeeStructureResponse> list(UUID schoolId) {
        return feeStructureRepository.findBySchoolId(schoolId).stream()
                .map(FeeMappers::toStructureResponse).toList();
    }

    @Override
    @Transactional
    public FeeStructureResponse update(UUID schoolId, UUID id, UpdateFeeStructureRequest request) {
        FeeStructure structure = feeStructureRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found."));
        if (request.schoolFeeAmount() != null) structure.setSchoolFeeAmount(request.schoolFeeAmount());
        if (request.busFeeAmount() != null) structure.setBusFeeAmount(request.busFeeAmount());
        if (request.dueDate() != null) structure.setDueDate(request.dueDate());
        return FeeMappers.toStructureResponse(feeStructureRepository.save(structure));
    }

    private BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
