package com.campusos.academic_service.service;

import com.campusos.academic_service.dto.request.ApplyLeaveRequest;
import com.campusos.academic_service.dto.request.DecideLeaveRequest;
import com.campusos.academic_service.dto.response.LeaveResponse;

import java.util.List;
import java.util.UUID;

public interface LeaveService {

    LeaveResponse applyLeave(UUID parentUserId, ApplyLeaveRequest request);

    List<LeaveResponse> getStudentLeaves(UUID parentUserId, UUID studentId);

    List<LeaveResponse> getPendingForTeacher(UUID teacherUserId);

    LeaveResponse decideLeave(UUID teacherUserId, UUID leaveId, DecideLeaveRequest request);
}
