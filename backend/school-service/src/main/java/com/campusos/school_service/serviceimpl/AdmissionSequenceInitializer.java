package com.campusos.school_service.serviceimpl;

import com.campusos.school_service.entity.AdmissionSequence;
import com.campusos.school_service.entity.AdmissionSequenceId;
import com.campusos.school_service.repository.AdmissionSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures the per-(school, year) admission sequence row exists before it is
 * locked for increment. Runs in its OWN transaction (REQUIRES_NEW) so that a
 * unique-constraint collision from a concurrent first admission is contained
 * here and never poisons the caller's transaction. Separate bean so the
 * REQUIRES_NEW proxy actually applies.
 */
@Component
@RequiredArgsConstructor
public class AdmissionSequenceInitializer {

    private final AdmissionSequenceRepository sequenceRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureExists(AdmissionSequenceId id) {
        if (sequenceRepository.findById(id).isPresent()) {
            return;
        }
        try {
            sequenceRepository.saveAndFlush(AdmissionSequence.builder().id(id).lastNumber(0).build());
        } catch (DataIntegrityViolationException concurrentCreateWon) {
            // Another admission created the row first — that's fine, it now exists.
        }
    }
}
