package com.campusos.academic_service.exception;

import org.springframework.http.HttpStatus;

/** Authenticated but not allowed to touch this resource (e.g. not the parent's child). */
public class ForbiddenException extends ApiException {

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
