package com.campusos.school_service.entity;

import com.campusos.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "school")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class School extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    /** Admission-number prefix, e.g. "GVS". */
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String village;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String state;

    @Column(length = 10)
    private String pincode;

    @Column(length = 20)
    private String phone;

    @Column(length = 190)
    private String email;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}
