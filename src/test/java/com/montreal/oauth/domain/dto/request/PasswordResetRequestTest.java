package com.montreal.oauth.domain.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidPasswordResetRequest() {
        PasswordResetRequest request = PasswordResetRequest.builder()
                .token("valid-token-123")
                .newPassword("Test@123")
                .confirmPassword("Test@123")
                .build();

        Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Should not have validation violations");
    }

    @Test
    void testNullToken() {
        PasswordResetRequest request = PasswordResetRequest.builder()
                .token(null)
                .newPassword("Test@123")
                .confirmPassword("Test@123")
                .build();

        Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("token")));
    }

    @Test
    void testEmptyToken() {
        PasswordResetRequest request = PasswordResetRequest.builder()
                .token("")
                .newPassword("Test@123")
                .confirmPassword("Test@123")
                .build();

        Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("token")));
    }

    @Test
    void testNullPassword() {
        PasswordResetRequest request = PasswordResetRequest.builder()
                .token("valid-token-123")
                .newPassword(null)
                .confirmPassword("Test@123")
                .build();

        Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("newPassword")));
    }

    @Test
    void testEmptyPassword() {
        PasswordResetRequest request = PasswordResetRequest.builder()
                .token("valid-token-123")
                .newPassword("")
                .confirmPassword("")
                .build();

        Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("newPassword")));
    }

    @Test
    void testPasswordTooShort() {
        PasswordResetRequest request = PasswordResetRequest.builder()
                .token("valid-token-123")
                .newPassword("Ab@")
                .confirmPassword("Ab@")
                .build();

        Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("newPassword")));
    }

    @Test
    void testPasswordTooLong() {
        PasswordResetRequest request = PasswordResetRequest.builder()
                .token("valid-token-123")
                .newPassword("Ab@123456")
                .confirmPassword("Ab@123456")
                .build();

        Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("newPassword")));
    }

    @Test
    void testValidPasswords() {
        String[] validPasswords = {
                "Test@12",
                "Abc#45",
                "Xyz_78",
                "Pass@1",
                "User#9",
                "Ab@1"
        };

        for (String password : validPasswords) {
            PasswordResetRequest request = PasswordResetRequest.builder()
                    .token("valid-token-123")
                    .newPassword(password)
                    .confirmPassword(password)
                    .build();

            Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty(), "Password '" + password + "' should be valid");
        }
    }

    @Test
    void testInvalidPasswords() {
        String[] invalidPasswords = {
                "Ab@",
                "Ab@123456",
        };

        for (String password : invalidPasswords) {
            PasswordResetRequest request = PasswordResetRequest.builder()
                    .token("valid-token-123")
                    .newPassword(password)
                    .confirmPassword(password)
                    .build();

            Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);
            assertFalse(violations.isEmpty(), "Password '" + password + "' should be invalid");
        }
    }

    @Test
    void testNullConfirmPassword() {
        PasswordResetRequest request = PasswordResetRequest.builder()
                .token("valid-token-123")
                .newPassword("Test@123")
                .confirmPassword(null)
                .build();

        Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("confirmPassword")));
    }

    @Test
    void testEmptyConfirmPassword() {
        PasswordResetRequest request = PasswordResetRequest.builder()
                .token("valid-token-123")
                .newPassword("Test@123")
                .confirmPassword("")
                .build();

        Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("confirmPassword")));
    }
}