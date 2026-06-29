package com.campusos.fee_service.repository;

import com.campusos.fee_service.entity.StudentFee;
import com.campusos.fee_service.enums.FeeStatus;
import com.campusos.fee_service.enums.FeeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentFeeRepository extends JpaRepository<StudentFee, UUID> {

    @Query("select sf from StudentFee sf where sf.schoolId = :schoolId "
            + "and (:classLabel is null or sf.classLabel = :classLabel) "
            + "and (:status is null or sf.status = :status) "
            + "and (:feeType is null or sf.feeType = :feeType)")
    Page<StudentFee> search(@Param("schoolId") UUID schoolId,
                            @Param("classLabel") String classLabel,
                            @Param("status") FeeStatus status,
                            @Param("feeType") FeeType feeType,
                            Pageable pageable);

    boolean existsByStudentIdAndAcademicYearAndFeeType(UUID studentId, String academicYear, FeeType feeType);

    List<StudentFee> findByStudentIdOrderByAcademicYearDesc(UUID studentId);

    Optional<StudentFee> findByIdAndSchoolId(UUID id, UUID schoolId);

    /** Rows: [status(FeeStatus), count(Long), totalAmount(BigDecimal)]. */
    @Query("select sf.status, count(sf), coalesce(sum(sf.amount), 0) from StudentFee sf "
            + "where sf.schoolId = :schoolId group by sf.status")
    List<Object[]> summarize(@Param("schoolId") UUID schoolId);

    List<StudentFee> findByStatusAndDueDateLessThanEqual(FeeStatus status, LocalDate date);
}
