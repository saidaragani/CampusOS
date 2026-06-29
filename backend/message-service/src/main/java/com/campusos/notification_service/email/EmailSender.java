package com.campusos.notification_service.email;

/**
 * Sends an email. Default implementation logs (no SMTP creds needed). A real
 * JavaMailSender-backed implementation drops in behind this interface.
 */
public interface EmailSender {

    void send(String to, String subject, String body);
}
