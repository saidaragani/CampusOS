package com.campusos.common_lib.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {

    String message() default
            "Password must contain 8-16 characters, one uppercase, one lowercase, one number and special character.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
