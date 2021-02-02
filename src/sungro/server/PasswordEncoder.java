package sungro.server;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public class PasswordEncoder {
    private static final int SALT_LEN = 16;
    private static final int HASH_LEN = 64;
    private static final int ITERATION = 8192;

    public static String encode(String passwordStr) {
        char[] password = passwordStr.toCharArray();

        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LEN];
        random.nextBytes(salt);

        byte[] hash = pbkdf2(password, salt, ITERATION, HASH_LEN);
        return ITERATION + ":" + toHex(salt) + ":" + toHex(hash);
    }

    public static boolean verify(String password, String encodedPassword) {

        String[] tokens = encodedPassword.split(":");

        int iteration = Integer.parseInt(tokens[0]);
        byte[] salt = fromHex(tokens[1]);
        byte[] hash = fromHex(tokens[2]);

        byte[] testHash = pbkdf2(password.toCharArray(), salt, iteration, hash.length);

        return slowEquals(hash, testHash);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iteration, int hash_len) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iteration, hash_len * 8);
        SecretKeyFactory skf;

        try {
            skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        try {
            return skf.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHex(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {
            int x = bytes[i] & 0xff;
            hex[i * 2] = toHex(x >>> 4);
            hex[i * 2 + 1] = toHex(x & 0xf);
        }

        return new String(hex);
    }

    private static char toHex(int x) {
        if (x < 10) {
            return (char) ('0' + x);
        } else {
            return (char) ('A' + x - 10);
        }
    }

    private static byte[] fromHex(String hexStr) {
        char[] hex = hexStr.toCharArray();
        byte[] bytes = new byte[hex.length / 2];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) ((fromHex(hex[i * 2]) << 4) | fromHex(hex[i * 2 + 1]));
        }

        return bytes;
    }

    private static int fromHex(char x) {
        if (x >= 'A') {
            return 10 + (x - 'A');
        } else {
            return x - '0';
        }
    }

    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;

        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }

        return diff == 0;
    }
}
