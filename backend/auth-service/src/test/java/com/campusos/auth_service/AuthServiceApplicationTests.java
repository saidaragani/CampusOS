package com.campusos.auth_service;

import com.campusos.auth_service.client.NotificationClient;
import com.campusos.auth_service.client.SchoolClient;
import com.campusos.auth_service.client.StudentClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Boots the full application context against in-memory H2 (profile "test"), with
 * the downstream Feign clients mocked. This is a wiring smoke test: it verifies
 * every bean, entity mapping, derived repository query, security config and the
 * startup DataInitializer all load correctly — no MySQL/Eureka required.
 */
@SpringBootTest
@ActiveProfiles("test")
class AuthServiceApplicationTests {

	@MockitoBean private StudentClient studentClient;
	@MockitoBean private SchoolClient schoolClient;
	@MockitoBean private NotificationClient notificationClient;

	@Test
	void contextLoads() {
	}

}
