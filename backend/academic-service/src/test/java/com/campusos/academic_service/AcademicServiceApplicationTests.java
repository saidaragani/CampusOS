package com.campusos.academic_service;

import com.campusos.academic_service.client.AuthClient;
import com.campusos.academic_service.client.NotificationClient;
import com.campusos.academic_service.client.SchoolClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Boots the full context against in-memory H2 (profile "test") with the Feign
 * clients mocked. Smoke-tests beans, entity mappings, derived queries and the
 * security config — no MySQL / Eureka / sibling services required.
 */
@SpringBootTest
@ActiveProfiles("test")
class AcademicServiceApplicationTests {

	@MockitoBean
	private SchoolClient schoolClient;

	@MockitoBean
	private AuthClient authClient;

	@MockitoBean
	private NotificationClient notificationClient;

	@Test
	void contextLoads() {
	}

}
