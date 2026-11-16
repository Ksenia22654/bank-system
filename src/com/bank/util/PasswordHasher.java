package com.bank.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Хеширование паролей алгоритмом PBKDF2. Формат хранения: итерации:соль:хеш. */
public final class PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 100_000;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] key = derive(password, salt, ITERATIONS);
        Base64.Encoder encoder = Base64.getEncoder();
        return ITERATIONS + ":" + encoder.encodeToString(salt) + ":" + encoder.encodeToString(key);
    }

    public static boolean matches(String password, String stored) {
        String[] parts = stored.split(":");
        if (parts.length != 3) {
            return false;
        }
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] expected = decoder.decode(parts[2]);
        byte[] actual = derive(password, decoder.decode(parts[1]), Integer.parseInt(parts[0]));
        return MessageDigest.isEqual(expected, actual);
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Алгоритм хеширования паролей недоступен", e);
        }
    }
}
