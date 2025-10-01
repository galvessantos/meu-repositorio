package com.montreal.oauth.domain.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes específicos para o enum ValidationResult do UserTokenService
 */
class UserTokenServiceValidationResultTest {

    @Test
    void validationResult_HasCorrectValues() {
        // Given & When
        UserTokenService.ValidationResult[] values = UserTokenService.ValidationResult.values();

        // Then
        assertEquals(3, values.length);
        assertEquals(UserTokenService.ValidationResult.OK, values[0]);
        assertEquals(UserTokenService.ValidationResult.INVALID, values[1]);
        assertEquals(UserTokenService.ValidationResult.EXPIRED, values[2]);
    }

    @Test
    void validationResult_ValueOfWorks() {
        // Given & When & Then
        assertEquals(UserTokenService.ValidationResult.OK, 
                    UserTokenService.ValidationResult.valueOf("OK"));
        assertEquals(UserTokenService.ValidationResult.INVALID, 
                    UserTokenService.ValidationResult.valueOf("INVALID"));
        assertEquals(UserTokenService.ValidationResult.EXPIRED, 
                    UserTokenService.ValidationResult.valueOf("EXPIRED"));
    }

    @Test
    void validationResult_ToStringWorks() {
        // Given & When & Then
        assertEquals("OK", UserTokenService.ValidationResult.OK.toString());
        assertEquals("INVALID", UserTokenService.ValidationResult.INVALID.toString());
        assertEquals("EXPIRED", UserTokenService.ValidationResult.EXPIRED.toString());
    }

    @Test
    void validationResult_EqualsAndHashCodeWork() {
        // Given
        UserTokenService.ValidationResult ok1 = UserTokenService.ValidationResult.OK;
        UserTokenService.ValidationResult ok2 = UserTokenService.ValidationResult.OK;
        UserTokenService.ValidationResult invalid = UserTokenService.ValidationResult.INVALID;

        // When & Then
        assertEquals(ok1, ok2);
        assertNotEquals(ok1, invalid);
        assertEquals(ok1.hashCode(), ok2.hashCode());
        assertNotEquals(ok1.hashCode(), invalid.hashCode());
    }

    @Test
    void validationResult_InvalidValueOf_ThrowsException() {
        // Given & When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            UserTokenService.ValidationResult.valueOf("NONEXISTENT");
        });
    }
}