package com.campusos.auth_service.serviceimpl;

import com.campusos.auth_service.entity.Role;
import com.campusos.auth_service.enums.RoleType;
import com.campusos.auth_service.exception.ResourceNotFoundException;
import com.campusos.auth_service.repository.RoleRepository;
import com.campusos.auth_service.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public Role getByName(RoleType name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role " + name + " is not configured. Ensure roles are seeded on startup."));
    }
}
