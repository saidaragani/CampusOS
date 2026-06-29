package com.campusos.common_lib.contract;

import java.util.UUID;

/**
 * Shared inter-service contract describing a school's public header info.
 * Owned/produced by school-service; consumed by any service that needs to show
 * the school name or logo (e.g. the parent portal header).
 */
public record SchoolSummary(
        UUID schoolId,
        String name,
        String code,
        String logoUrl
) {}
