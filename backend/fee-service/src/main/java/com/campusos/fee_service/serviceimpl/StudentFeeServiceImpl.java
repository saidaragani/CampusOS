package com.campusos.fee_service.serviceimpl;

import com.campusos.common_lib.contract.ChildLink;
import com.campusos.common_lib.contract.RosterStudent;
import com.campusos.common_lib.contract.SchoolFeeStats;
import com.campusos.fee_service.client.AuthClient;
import com.campusos.fee_service.client.NotificationClient;
import com.campusos.fee_service.client.SchoolClient;
import com.campusos.fee_service.client.dto.FeeNotification;
import com.campusos.fee_service.dto.request.GenerateFeesRequest;
import com.campusos.fee_service.dto.request.MarkPaidRequest;
import com.campusos.fee_service.dto.response.FeeSummaryResponse;
import com.campusos.fee_service.dto.response.StudentFeeResponse;
import com.campusos.fee_service.entity.FeeStructure;
import com.campusos.fee_service.entity.StudentFee;
import com.campusos.fee_service.enums.FeeStatus;
import com.campusos.fee_service.enums.FeeType;
import com.campusos.fee_service.exception.BadRequestException;
import com.campusos.fee_service.exception.ForbiddenException;
import com.campusos.fee_service.exception.ResourceNotFoundException;
import com.campusos.fee_service.exception.ServiceUnavailableException;
import com.campusos.fee_service.mapper.FeeMappers;
import com.campusos.fee_service.repository.FeeStructureRepository;
import com.campusos.fee_service.repository.StudentFeeRepository;
import com.campusos.fee_service.service.StudentFeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentFeeServiceImpl implements StudentFeeService {

    private final StudentFeeRepository studentFeeRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final SchoolClient schoolClient;
    private final AuthClient authClient;
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public int generate(UUID schoolId, GenerateFeesRequest request) {
        String classLabel = request.classLabel().trim();
        FeeStructure structure = feeStructureRepository
                .findBySchoolIdAndAcademicYearAndClassLabel(schoolId, request.academicYear(), classLabel)
                .or(() -> feeStructureRepository.findBySchoolIdAndAcademicYearAndClassLabelIsNull(schoolId, request.academicYear()))
                .orElseThrow(() -> new BadRequestException(
                        "No fee structure defined for " + request.academicYear() + " / " + classLabel + "."));

        List<RosterStudent> roster = roster(schoolId, classLabel);
        int created = 0;
        for (RosterStudent student : roster) {
            if (!studentFeeRepository.existsByStudentIdAndAcademicYearAndFeeType(
                    student.studentId(), request.academicYear(), FeeType.SCHOOL)) {
                studentFeeRepository.save(buildFee(schoolId, student, classLabel, request.academicYear(),
                        FeeType.SCHOOL, structure.getSchoolFeeAmount(), structure.getDueDate()));
                created++;
            }
            if (student.hasBus() && structure.getBusFeeAmount().signum() > 0
                    && !studentFeeRepository.existsByStudentIdAndAcademicYearAndFeeType(
                            student.studentId(), request.academicYear(), FeeType.BUS)) {
                studentFeeRepository.save(buildFee(schoolId, student, classLabel, request.academicYear(),
                        FeeType.BUS, structure.getBusFeeAmount(), structure.getDueDate()));
                created++;
            }
        }
        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentFeeResponse> list(UUID schoolId, String classLabel, FeeStatus status, FeeType feeType, Pageable pageable) {
        String label = (classLabel != null && !classLabel.isBlank()) ? classLabel.trim() : null;
        return studentFeeRepository.search(schoolId, label, status, feeType, pageable)
                .map(FeeMappers::toStudentFeeResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public FeeSummaryResponse summary(UUID schoolId) {
        long paidCount = 0;
        long pendingCount = 0;
        BigDecimal paidTotal = BigDecimal.ZERO;
        BigDecimal pendingTotal = BigDecimal.ZERO;
        for (Object[] row : studentFeeRepository.summarize(schoolId)) {
            FeeStatus status = (FeeStatus) row[0];
            long count = ((Number) row[1]).longValue();
            BigDecimal total = (BigDecimal) row[2];
            if (status == FeeStatus.PAID) {
                paidCount = count;
                paidTotal = total;
            } else {
                pendingCount = count;
                pendingTotal = total;
            }
        }
        return new FeeSummaryResponse(paidCount, paidTotal, pendingCount, pendingTotal);
    }

    @Override
    @Transactional
    public StudentFeeResponse markPaid(UUID schoolId, UUID userId, UUID feeId, MarkPaidRequest request) {
        StudentFee fee = require(schoolId, feeId);
        fee.setStatus(FeeStatus.PAID);
        fee.setPaidOn(LocalDate.now());
        fee.setPaidAmount(request != null && request.paidAmount() != null ? request.paidAmount() : fee.getAmount());
        if (request != null) {
            fee.setPaymentNote(request.paymentNote());
        }
        fee.setUpdatedByUserId(userId);
        StudentFee saved = studentFeeRepository.save(fee);

        notify(saved, "RECEIPT");
        return FeeMappers.toStudentFeeResponse(saved);
    }

    @Override
    @Transactional
    public StudentFeeResponse markPending(UUID schoolId, UUID userId, UUID feeId) {
        StudentFee fee = require(schoolId, feeId);
        fee.setStatus(FeeStatus.PENDING);
        fee.setPaidOn(null);
        fee.setPaidAmount(null);
        fee.setUpdatedByUserId(userId);
        return FeeMappers.toStudentFeeResponse(studentFeeRepository.save(fee));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentFeeResponse> studentFees(UUID parentUserId, UUID studentId) {
        requireOwnedChild(parentUserId, studentId);
        return studentFeeRepository.findByStudentIdOrderByAcademicYearDesc(studentId).stream()
                .map(FeeMappers::toStudentFeeResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolFeeStats getSchoolStats(UUID schoolId) {
        long paidCount = 0;
        long pendingCount = 0;
        BigDecimal paidTotal = BigDecimal.ZERO;
        BigDecimal pendingTotal = BigDecimal.ZERO;
        for (Object[] row : studentFeeRepository.summarize(schoolId)) {
            FeeStatus status = (FeeStatus) row[0];
            long count = ((Number) row[1]).longValue();
            BigDecimal total = (BigDecimal) row[2];
            if (status == FeeStatus.PAID) {
                paidCount = count;
                paidTotal = total;
            } else {
                pendingCount = count;
                pendingTotal = total;
            }
        }
        BigDecimal billed = paidTotal.add(pendingTotal);
        double pct = billed.signum() == 0 ? 0.0 : paidTotal.doubleValue() * 100.0 / billed.doubleValue();
        return new SchoolFeeStats(schoolId, paidTotal, pendingTotal, paidCount, pendingCount, pct);
    }

    // ---------------- helpers ----------------

    private StudentFee buildFee(UUID schoolId, RosterStudent student, String classLabel, String year,
                                FeeType type, BigDecimal amount, LocalDate dueDate) {
        return StudentFee.builder()
                .schoolId(schoolId)
                .studentId(student.studentId())
                .classLabel(classLabel)
                .academicYear(year)
                .feeType(type)
                .amount(amount != null ? amount : BigDecimal.ZERO)
                .dueDate(dueDate)
                .status(FeeStatus.PENDING)
                .build();
    }

    private StudentFee require(UUID schoolId, UUID feeId) {
        return studentFeeRepository.findByIdAndSchoolId(feeId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee record not found."));
    }

    private List<RosterStudent> roster(UUID schoolId, String classLabel) {
        try {
            return schoolClient.getRoster(schoolId, classLabel);
        } catch (Exception ex) {
            log.warn("school-service unavailable while loading roster for {}: {}", classLabel, ex.getMessage());
            throw new ServiceUnavailableException("School service is unavailable. Please try again later.");
        }
    }

    private void requireOwnedChild(UUID parentUserId, UUID studentId) {
        List<ChildLink> children;
        try {
            children = authClient.getChildren(parentUserId);
        } catch (Exception ex) {
            throw new ServiceUnavailableException("Auth service is unavailable. Please try again later.");
        }
        boolean owns = children.stream().anyMatch(c -> c.studentId().equals(studentId));
        if (!owns) {
            throw new ForbiddenException("This student is not linked to your account.");
        }
    }

    private void notify(StudentFee fee, String kind) {
        try {
            notificationClient.notifyFees(List.of(new FeeNotification(
                    fee.getStudentId(), fee.getSchoolId(), fee.getFeeType().name(), kind, fee.getAmount())));
        } catch (Exception ex) {
            log.warn("Failed to publish fee {} notification: {}", kind, ex.getMessage());
        }
    }
}
