package com.vr.miniauthorizer.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import resources.fixtures.TestFixture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("HashUtil")
class HashUtilTest {

    @Test
    @DisplayName("should return correct SHA-256 Base64 hash for known password")
    void shouldReturnCorrectHashForKnownPassword() {
        String hash = HashUtil.hashString(TestFixture.CARD_PASSWORD);
        assertEquals(TestFixture.CARD_PASSWORD_HASH, hash);
    }

    @Test
    @DisplayName("should return true when comparing matching hash and input")
    void shouldReturnTrueWhenComparingMatchingHashAndInput() {
        String storedHash = HashUtil.hashString(TestFixture.CARD_PASSWORD_HASH);
        assertTrue(HashUtil.compareHash(TestFixture.CARD_PASSWORD_HASH, storedHash));
    }

    @Test
    @DisplayName("should return false when input does not match stored hash")
    void shouldReturnFalseWhenInputDoesNotMatchStoredHash() {
        String storedHash = HashUtil.hashString(TestFixture.CARD_PASSWORD_HASH);
        assertFalse(HashUtil.compareHash(TestFixture.CARD_PASSWORD, storedHash));
    }
}
