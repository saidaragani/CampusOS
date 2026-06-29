package com.campusos.school_service;

import com.campusos.school_service.client.AuthClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Boots the full context against in-memory H2 (profile "test") with the auth
 * Feign client mocked. Smoke-tests every bean, entity mapping, derived query,
 * the @Lock admission-sequence query and the security config — no MySQL/Eureka.
 */
@SpringBootTest
@ActiveProfiles("test")
class SchoolServiceApplicationTests {

	@MockitoBean
	private AuthClient authClient;

	@Test
	void contextLoads() {
	}

}
