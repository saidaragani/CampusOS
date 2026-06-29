package com.campusos.academic_service.repository;

import com.campusos.academic_service.entity.BehaviourRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BehaviourRatingRepository extends JpaRepository<BehaviourRating, UUID> {

    Optional<BehaviourRating> findByStudentIdAndRatingMonth(UUID studentId, String ratingMonth);

    List<BehaviourRating> findBySchoolIdAndClassLabelAndRatingMonth(UUID schoolId, String classLabel, String ratingMonth);

    List<BehaviourRating> findByStudentIdOrderByRatingMonthDesc(UUID studentId);
}
