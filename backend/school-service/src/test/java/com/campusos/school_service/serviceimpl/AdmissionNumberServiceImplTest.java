package com.campusos.school_service.serviceimpl;

import com.campusos.school_service.entity.AdmissionSequence;
import com.campusos.school_service.entity.AdmissionSequenceId;
import com.campusos.school_service.repository.AdmissionSequenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdmissionNumberServiceImplTest {

    @Mock private AdmissionSequenceRepository sequenceRepository;
    @Mock private AdmissionSequenceInitializer sequenceInitializer;
    @InjectMocks private AdmissionNumberServiceImpl admissionNumberService;

    private final UUID schoolId = UUID.randomUUID();

    @Test
    void firstAdmission_startsAtOne() {
        // ensureExists seeds a lastNumber=0 row, which the lock then reads.
        AdmissionSequence seeded = AdmissionSequence.builder()
                .id(new AdmissionSequenceId(schoolId, "2025")).lastNumber(0).build();
        when(sequenceRepository.findByIdForUpdate(any())).thenReturn(Optional.of(seeded));
        when(sequenceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String admissionNo = admissionNumberService.generate(schoolId, "GVS", 2025);

        assertThat(admissionNo).isEqualTo("GVS-2025-0001");
    }

    @Test
    void subsequentAdmission_incrementsAndZeroPads() {
        AdmissionSequence existing = AdmissionSequence.builder()
                .id(new AdmissionSequenceId(schoolId, "2025"))
                .lastNumber(5)
                .build();
        when(sequenceRepository.findByIdForUpdate(any())).thenReturn(Optional.of(existing));
        when(sequenceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String admissionNo = admissionNumberService.generate(schoolId, "GVS", 2025);

        assertThat(admissionNo).isEqualTo("GVS-2025-0006");
        assertThat(existing.getLastNumber()).isEqualTo(6);
    }
}
