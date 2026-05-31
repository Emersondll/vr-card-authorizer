package com.vr.miniauthorizer.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility class providing SHA-256 hashing operations for card password management.
 *
 * <p>All methods are static. This class cannot be instantiated.</p>
 *
 * <p>Algorithm: SHA-256 with Base64 URL-safe encoding.
 * The same input always produces the same output (deterministic).</p>
 *
 * <p>Thread Safety: All methods are stateless and thread-safe.</p>
 *
 * @author Emerson Lima
 * @version 1.0
 * @since 1.0.0
 */
public final class HashUtil {

    private static final String SHA_256 = "SHA-256";

    private HashUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Hashes the input string using SHA-256 and returns the Base64-encoded result.
     *
     * <p>Example:
     * <pre>{@code
     * String hash = HashUtil.hashString("myPassword");
     * // hash = "MEGfTPMhBPRFStXbZHAUT0jFoAWrsA35l/XHgqfPU3E="
     * }</pre>
     *
     * @param input the plain-text string to hash, must be non-null
     * @return Base64-encoded SHA-256 digest of the input, never {@code null}
     * @throws IllegalStateException if the SHA-256 algorithm is unavailable in the JVM
     */
    public static String hashString(final String input) {
        try {
            final MessageDigest digest = MessageDigest.getInstance(SHA_256);
            final byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(ExceptionMessages.ERROR_WHILE_HASHING_STRING, e);
        }
    }

    /**
     * Compares a plain-text input against a stored SHA-256 hash.
     *
     * <p>The input is hashed before comparison. Constant-time comparison is not
     * guaranteed; use this only for non-cryptographic PIN matching, not authentication tokens.</p>
     *
     * @param input      the plain-text string to verify, must be non-null
     * @param storedHash the previously computed Base64-encoded SHA-256 hash, must be non-null
     * @return {@code true} if the hash of {@code input} equals {@code storedHash};
     *         {@code false} otherwise
     * @throws IllegalStateException if SHA-256 is unavailable (propagated from {@link #hashString})
     */
    public static boolean compareHash(final String input, final String storedHash) {
        final String hashedInput = hashString(input);
        return hashedInput.equals(storedHash);
    }
}
