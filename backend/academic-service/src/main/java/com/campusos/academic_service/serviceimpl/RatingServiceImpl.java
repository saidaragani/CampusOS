package com.campusos.academic_service.serviceimpl;

import com.campusos.academic_service.dto.request.SubmitRatingRequest;
import com.campusos.academic_service.dto.response.RatingResponse;
import com.campusos.academic_service.entity.BehaviourRating;
import com.campusos.academic_service.exception.BadRequestException;
import com.campusos.academic_service.mapper.AcademicMappers;
import com.campusos.academic_service.repository.BehaviourRatingRepository;
import com.campusos.academic_service.service.RatingService;
import com.campusos.academic_service.support.AcademicAccessResolver;
import com.campusos.common_lib.contract.RosterStudent;
import com.campusos.common_lib.contract.TeacherClassView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final BehaviourRatingRepository ratingRepository;
    private final AcademicAccessResolver access;

    @Override
    @Transactional
    public RatingResponse submitRating(UUID teacherUserId, SubmitRatingRequest request) {
        TeacherClassView tc = access.requireTeacherClass(teacherUserId);

        boolean inClass = access.roster(tc.schoolId(), tc.classLabel()).stream()
                .anyMatch(s -> s.studentId().equals(request.studentId()));
        if (!inClass) {
            throw new BadRequestException("Student is not in your class.");
        }

        BehaviourRating rating = ratingRepository
                .findByStudentIdAndRatingMonth(request.studentId(), request.month())
                .orElseGet(BehaviourRating::new);

        rating.setSchoolId(tc.schoolId());
        rating.setStudentId(request.studentId());
        rating.setClassLabel(tc.classLabel());
        rating.setRatingMonth(request.month());
        rating.setBehaviourScore(request.score());
        rating.setRemarks(request.remarks());
        rating.setRatedByTeacherId(tc.teacherId());

        return AcademicMappers.toRatingResponse(ratingRepository.save(rating));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponse> getClassRatings(UUID teacherUserId, String month) {
        TeacherClassView tc = access.requireTeacherClass(teacherUserId);
        return ratingRepository.findBySchoolIdAndClassLabelAndRatingMonth(tc.schoolId(), tc.classLabel(), month)
                .stream().map(AcademicMappers::toRatingResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponse> getStudentRatings(UUID requesterUserId, String role, UUID schoolId, UUID studentId) {
        if ("PARENT".equals(role)) {
            access.requireOwnedChild(requesterUserId, studentId);
        }
        List<BehaviourRating> ratings = ratingRepository.findByStudentIdOrderByRatingMonthDesc(studentId);
        if ("ADMIN".equals(role)) {
            ratings = ratings.stream().filter(r -> r.getSchoolId().equals(schoolId)).toList();
        }
        return ratings.stream().map(AcademicMappers::toRatingResponse).toList();
    }
}
