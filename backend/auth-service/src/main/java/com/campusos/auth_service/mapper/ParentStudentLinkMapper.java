package com.campusos.auth_service.mapper;

import com.campusos.auth_service.dto.request.ChildDto;
import com.campusos.auth_service.entity.ParentStudentLink;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ParentStudentLinkMapper {

    ParentStudentLinkMapper INSTANCE = Mappers.getMapper(ParentStudentLinkMapper.class);

    ChildDto toChildDto(ParentStudentLink parentStudentLink);
}
