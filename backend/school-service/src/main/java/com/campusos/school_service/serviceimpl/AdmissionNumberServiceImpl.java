package com.campusos.school_service.serviceimpl;

import com.campusos.school_service.entity.AdmissionSequence;
import com.campusos.school_service.entity.AdmissionSequenceId;
import com.campusos.school_service.repository.AdmissionSequenceRepository;
import com.campusos.school_service.service.AdmissionNumberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Generates per-school admission numbers like {@code GVS-2025-0001}. The running
 * counter lives in {@code admission_sequence}, locked for update so concurrent
 * admissions in the same school+year increment without collisions.
 */
@Service
@RequiredArgsConstructor
public class AdmissionNumberServiceImpl implements AdmissionNumberService {

    private final AdmissionSequenceRepository sequenceRepository;
    private final AdmissionSequenceInitializer sequenceInitializer;

    @Override
    @Transactional
    public String generate(UUID schoolId, String schoolCode, int year) {
        AdmissionSequenceId id = new AdmissionSequenceId(schoolId, String.valueOf(year));

        // Guarantee the row exists (in its own tx) so the lock below always has a
        // row to lock — avoids the cold-start race on the first admission of a year.
        sequenceInitializer.ensureExists(id);

        AdmissionSequence sequence = sequenceRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalStateException("Admission sequence row missing after initialization."));

        int next = sequence.getLastNumber() + 1;
        sequence.setLastNumber(next);
        sequenceRepository.save(sequence);

        return String.format("%s-%d-%04d", schoolCode, year, next);
    }
}
