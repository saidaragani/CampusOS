package com.campusos.calendar_service;

import com.campusos.calendar_service.client.NotificationClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class CalendarServiceApplicationTests {

	@MockitoBean
	private NotificationClient notificationClient;

	@Test
	void contextLoads() {
	}

}
