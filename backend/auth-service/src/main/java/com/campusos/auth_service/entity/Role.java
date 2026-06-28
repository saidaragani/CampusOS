package com.campusos.auth_service.entity;

import com.campusos.auth_service.enums.RoleType;
import com.campusos.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,unique = true)
    private RoleType name;

    @Column(length = 255)
    private String description;

}
