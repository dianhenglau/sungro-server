package sungro.server;

import java.security.SecureRandom;

public class RandomStrGenerator {
    public static String generate(int length, String charset) {
        SecureRandom random = new SecureRandom();
        StringBuilder result = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            result.append(charset.charAt(random.nextInt(charset.length())));
        }

        return result.toString();
    }

    public static String generateSessionId() {
        return generate(16, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
    }

    public static String generateSku() {
        return "S" + generate(6, "0123456789");
    }
}
