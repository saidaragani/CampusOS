package com.campusos.auth_service.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a downstream service (e.g. student-service) cannot be reached.
 * Maps to HTTP 503 so callers know to retry rather than treating it as a
 * client error.
 */
public class ServiceUnavailableException extends ApiException {

    public ServiceUnavailableException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}
