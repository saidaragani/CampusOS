package com.campusos.academic_service.service;

import com.campusos.academic_service.dto.request.SubmitRatingRequest;
import com.campusos.academic_service.dto.response.RatingResponse;

import java.util.List;
import java.util.UUID;

public interface RatingService {

    RatingResponse submitRating(UUID teacherUserId, SubmitRatingRequest request);

    List<RatingResponse> getClassRatings(UUID teacherUserId, String month);

    List<RatingResponse> getStudentRatings(UUID requesterUserId, String role, UUID schoolId, UUID studentId);
}
