package utils;

import io.qameta.allure.Allure;

import java.security.SecureRandom;
import java.util.Random;

public class TestDataGenerator {
    private static final String VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    //private static final String VALID_CHARS = "ABCDEF0123456789";
    private static final String INVALID_CHARS = "abcdefghijklmnopqrstuvwxyz!@#$%^&*()_-+=[]{}|;:,.<>?/";
    private static final int VALID_TOKEN_LENGTH = 32;
    private static final Random random = new SecureRandom();

    public static String generateValidToken() {
        return Allure.step("Generate a valid token (32 characters, letters A-Z and numbers 0-9 can be used)", () -> {
            StringBuilder token = new StringBuilder(VALID_TOKEN_LENGTH);

            for (int i = 0; i < VALID_TOKEN_LENGTH; i++) {
                token.append(VALID_CHARS.charAt(random.nextInt(VALID_CHARS.length())));
            }

            return token.toString();
        });
    }

    public static String generateInvalidLengthToken() {
        return Allure.step("Generate token with incorrect length", () -> {
            int length = random.nextBoolean() ? VALID_TOKEN_LENGTH - 1 : VALID_TOKEN_LENGTH + 1;
            StringBuilder token = new StringBuilder(length);

            for (int i = 0; i < length; i++) {
                token.append(VALID_CHARS.charAt(random.nextInt(VALID_CHARS.length())));
            }

            return token.toString();
        });
    }

    public static String generateInvalidCharsToken() {
        return Allure.step("Generate token with invalid characters", () -> {
            StringBuilder token = new StringBuilder(VALID_TOKEN_LENGTH);
            for (int i = 0; i < VALID_TOKEN_LENGTH; i++) {
                token.append(INVALID_CHARS.charAt(random.nextInt(INVALID_CHARS.length())));
            }
            return token.toString();
        });
    }
}
