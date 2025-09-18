package com.montreal.oauth.domain.service;

import com.montreal.oauth.domain.dto.response.ResetPasswordResult;
import com.montreal.oauth.domain.entity.PasswordResetToken;

import java.util.Optional;

public interface IPasswordResetService {

    /**
     * Generates a password reset token for the given login
     * @param login User's login (username or email)
     * @return Generated reset link with token
     */
    String generatePasswordResetToken(String login);

    /**
     * Validates a password reset token
     * @param token The token to validate
     * @return true if token is valid, false otherwise
     */
    boolean validatePasswordResetToken(String token);

    /**
     * Marks a password reset token as used
     * @param token The token to mark as used
     */
    void markTokenAsUsed(String token);

    /**
     * Finds a password reset token by its value
     * @param token The token value
     * @return Optional containing the token if found
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Cleans up expired and used tokens
     */
    void cleanupExpiredTokens();

    /**
     * Resets the user's password using a valid token (legacy method)
     * @param token The password reset token
     * @param newPassword The new password to set
     * @param confirmPassword The password confirmation
     * @return true if password was reset successfully, false otherwise
     */
    boolean resetPassword(String token, String newPassword, String confirmPassword);

    /**
     * Resets the user's password using a valid token and optionally performs auto-login
     * @param token The password reset token
     * @param newPassword The new password to set
     * @param confirmPassword The password confirmation
     * @return ResetPasswordResult containing success status, message, and optionally auth tokens
     */
    ResetPasswordResult resetPasswordWithTokens(String token, String newPassword, String confirmPassword);
}