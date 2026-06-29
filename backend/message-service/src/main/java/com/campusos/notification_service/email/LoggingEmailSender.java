package com.campusos.notification_service.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Default email sender: logs the message. Swap for an SMTP implementation
 * (JavaMailSender) without touching callers. Throwing here marks the
 * notification FAILED in the log.
 */
@Slf4j
@Component
public class LoggingEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String body) {
        log.info("[EMAIL] to={} subject=\"{}\" body=\"{}\"", to, subject, body);
    }
}
