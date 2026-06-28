package com.campusos.common_lib.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND("User not found"),
    INVALID_CREDENTIALS("Invalid username or password"),
    EMAIL_ALREADY_EXISTS("Email already exists"),
    ACCESS_DENIED("Access denied"),
    INTERNAL_SERVER_ERROR("Something went wrong");

    private final String message;
}