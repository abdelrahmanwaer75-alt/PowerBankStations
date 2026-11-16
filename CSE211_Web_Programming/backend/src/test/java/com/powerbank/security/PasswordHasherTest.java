package com.powerbank.security;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class PasswordHasherTest {
    private final PasswordHasher hasher = new PasswordHasher();

    @Test
    void hashesAndVerifiesPasswordsWithoutStoringPlainText() {
        String encoded = hasher.hash("correct horse battery staple");
        assertNotEquals("correct horse battery staple", encoded);
        assertTrue(hasher.matches("correct horse battery staple", encoded));
        assertFalse(hasher.matches("wrong password", encoded));
    }

    @Test
    void rejectsMalformedHashesAndNullValues() {
        assertFalse(hasher.matches("password", "not-a-valid-hash"));
        assertFalse(hasher.matches(null, hasher.hash("password")));
        assertFalse(hasher.matches("password", null));
    }

    @Test
    void verifiesSeededDemoHashes() {
        assertTrue(hasher.matches(
                "DemoCustomer123!",
                "pbkdf2_sha256$210000$j7K0/ch9dJgdGM1U6qOi7g==$qClytbDEqCkEeHVolJqzMZrbQY3Wb5nwambF/zqdYyU="));
        assertTrue(hasher.matches(
                "DemoAdmin123!",
                "pbkdf2_sha256$210000$gGux4HpvCkSxuNnhgXgz/g==$wXxkkPiNDkXIqTbSu5q8XyFiktmbXTGa6dNEKh/bikM="));
    }
}
