package com.campusos.auth_service.enums;

/**
 * Canonical set of role names that the application seeds and references in code.
 * The {@code roles} table can hold additional rows in the future without code
 * changes; this enum simply names the ones the system knows about today.
 */
public enum RoleType {
    SUPER_ADMIN,
    ADMIN,
    TEACHER,
    PARENT
}
