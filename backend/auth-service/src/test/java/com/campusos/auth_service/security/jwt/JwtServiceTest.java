package com.campusos.auth_service.security.jwt;

import com.campusos.auth_service.entity.Role;
import com.campusos.auth_service.entity.User;
import com.campusos.auth_service.enums.RoleType;
import com.campusos.auth_service.security.User.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    // 64-char secret -> 64 bytes, well above the 32-byte minimum for HS256.
    private static final String SECRET =
            "4C6F6E675365637265744B6579466F7243616D7075734F53323032364A575454";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 3_600_000L);
    }

    private User user(UUID id, UUID schoolId, RoleType roleType) {
        return User.builder()
                .id(id)
                .email("user@campusos.com")
                .password("hash")
                .fullName("Test User")
                .role(Role.builder().id(UUID.randomUUID()).name(roleType).build())
                .schoolId(schoolId)
                .enabled(true)
                .build();
    }

    @Test
    void generateAndParse_roundTrip() {
        UUID id = UUID.randomUUID();
        UUID schoolId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(user(id, schoolId, RoleType.TEACHER));

        String token = jwtService.generateToken(principal);

        assertThat(jwtService.extractUsername(token)).isEqualTo("user@campusos.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(id);
        assertThat(jwtService.extractSchoolId(token)).isEqualTo(schoolId);
        assertThat(jwtService.extractDisplayName(token)).isEqualTo("Test User");
        assertThat(jwtService.isTokenValid(token, principal)).isTrue();
    }

    @Test
    void superAdminWithoutSchool_hasNullSchoolClaim() {
        UserPrincipal principal = new UserPrincipal(user(UUID.randomUUID(), null, RoleType.SUPER_ADMIN));

        String token = jwtService.generateToken(principal);

        assertThat(jwtService.extractSchoolId(token)).isNull();
        assertThat(jwtService.isTokenValid(token, principal)).isTrue();
    }
}
