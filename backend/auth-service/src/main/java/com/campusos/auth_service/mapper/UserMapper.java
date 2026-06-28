package com.campusos.auth_service.mapper;

import com.campusos.auth_service.dto.response.UserSummaryDto;
import com.campusos.auth_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserSummaryDto toUserSummaryDto(User user);
}
