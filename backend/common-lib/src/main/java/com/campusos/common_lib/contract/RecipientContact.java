package com.campusos.common_lib.contract;

/**
 * An email recipient (parent or teacher), resolved by auth/school for the
 * messaging service.
 */
public record RecipientContact(
        String email,
        String fullName
) {}
