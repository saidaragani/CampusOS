package com.campusos.school_service.repository;

import com.campusos.school_service.entity.AdmissionSequence;
import com.campusos.school_service.entity.AdmissionSequenceId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdmissionSequenceRepository extends JpaRepository<AdmissionSequence, AdmissionSequenceId> {

    /**
     * Pessimistic write lock so two concurrent admissions in the same school+year
     * can't read the same last_number and collide.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AdmissionSequence a where a.id = :id")
    Optional<AdmissionSequence> findByIdForUpdate(@Param("id") AdmissionSequenceId id);
}
