package com.campusos.fee_service.serviceimpl;

import com.campusos.common_lib.contract.RosterStudent;
import com.campusos.fee_service.client.AuthClient;
import com.campusos.fee_service.client.NotificationClient;
import com.campusos.fee_service.client.SchoolClient;
import com.campusos.fee_service.dto.request.GenerateFeesRequest;
import com.campusos.fee_service.entity.FeeStructure;
import com.campusos.fee_service.entity.StudentFee;
import com.campusos.fee_service.enums.FeeType;
import com.campusos.fee_service.repository.FeeStructureRepository;
import com.campusos.fee_service.repository.StudentFeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentFeeServiceImplTest {

    @Mock private StudentFeeRepository studentFeeRepository;
    @Mock private FeeStructureRepository feeStructureRepository;
    @Mock private SchoolClient schoolClient;
    @Mock private AuthClient authClient;
    @Mock private NotificationClient notificationClient;

    @InjectMocks private StudentFeeServiceImpl studentFeeService;

    private final UUID schoolId = UUID.randomUUID();

    @Test
    void generate_createsSchoolFeeForAll_andBusFeeForBusStudents() {
        UUID busStudent = UUID.randomUUID();
        UUID walkStudent = UUID.randomUUID();

        FeeStructure structure = FeeStructure.builder()
                .schoolId(schoolId).academicYear("2025-26").classLabel("6-A")
                .schoolFeeAmount(new BigDecimal("5000")).busFeeAmount(new BigDecimal("1200"))
                .build();
        when(feeStructureRepository.findBySchoolIdAndAcademicYearAndClassLabel(schoolId, "2025-26", "6-A"))
                .thenReturn(Optional.of(structure));
        when(schoolClient.getRoster(schoolId, "6-A")).thenReturn(List.of(
                new RosterStudent(busStudent, schoolId, "GVS-2025-0001", "Asha", "6-A", "111", true),
                new RosterStudent(walkStudent, schoolId, "GVS-2025-0002", "Bharat", "6-A", "222", false)));
        when(studentFeeRepository.existsByStudentIdAndAcademicYearAndFeeType(any(), eq("2025-26"), any()))
                .thenReturn(false);
        when(studentFeeRepository.save(any(StudentFee.class))).thenAnswer(i -> i.getArgument(0));

        int created = studentFeeService.generate(schoolId, new GenerateFeesRequest("2025-26", "6-A"));

        // 2 school fees + 1 bus fee (only the bus student)
        assertThat(created).isEqualTo(3);
        verify(studentFeeRepository, times(3)).save(any(StudentFee.class));
    }
}
