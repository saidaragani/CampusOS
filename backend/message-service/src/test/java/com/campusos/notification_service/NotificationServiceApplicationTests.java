package com.campusos.notification_service;

import com.campusos.notification_service.client.AuthClient;
import com.campusos.notification_service.client.SchoolClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceApplicationTests {

	@MockitoBean
	private AuthClient authClient;

	@MockitoBean
	private SchoolClient schoolClient;

	@Test
	void contextLoads() {
	}

}
