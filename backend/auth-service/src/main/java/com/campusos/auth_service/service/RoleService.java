package com.campusos.auth_service.service;

import com.campusos.auth_service.entity.Role;
import com.campusos.auth_service.enums.RoleType;

public interface RoleService {

    Role getByName(RoleType name);
}
