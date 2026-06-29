package com.campusos.auth_service.mapper;

import com.campusos.auth_service.dto.response.UserSummaryDto;
import com.campusos.auth_service.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserSummaryDto toUserSummaryDto(User user) {
        if (user == null) {
            return null;
        }
        String roleName = (user.getRole() != null && user.getRole().getName() != null)
                ? user.getRole().getName().name()
                : null;
        return new UserSummaryDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                roleName,
                user.getSchoolId()
        );
    }
}
