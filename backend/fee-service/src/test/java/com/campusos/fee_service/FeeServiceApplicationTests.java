package com.campusos.fee_service;

import com.campusos.fee_service.client.AuthClient;
import com.campusos.fee_service.client.NotificationClient;
import com.campusos.fee_service.client.SchoolClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class FeeServiceApplicationTests {

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
