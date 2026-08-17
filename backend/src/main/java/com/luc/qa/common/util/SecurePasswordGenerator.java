package com.luc.qa.common.util;

import java.security.SecureRandom;

public final class SecurePasswordGenerator {

    private static final String ALPHANUMERIC =
        "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private SecurePasswordGenerator() {}

    public static String generate(int length) {
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return password.toString();
    }
}
