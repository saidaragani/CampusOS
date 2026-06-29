package com.campusos.auth_service.dto.response;

import com.campusos.common_lib.contract.SchoolSummary;

import java.util.List;

/**
 * Everything the parent portal needs to render its header and dashboard in one
 * call: the logged-in user, the school header (name + logo), and the parent's
 * children with their class/section/class-teacher.
 *
 * <p>{@code school} and the per-child class context are enriched best-effort;
 * they may be null while school-service / student-service are unavailable.
 */
public record ParentContextResponse(
        UserSummaryDto user,
        SchoolSummary school,
        List<ChildView> children
) {}
