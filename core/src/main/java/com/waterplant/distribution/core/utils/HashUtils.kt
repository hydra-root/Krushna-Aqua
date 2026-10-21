package com.waterplant.distribution.core.utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object HashUtils {
    
    /**
     * Generates a random cryptographic salt.
     */
    fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return Base64.getEncoder().encodeToString(saltBytes)
    }

    /**
     * Hashes the given PIN with the provided salt using SHA-256.
     */
    fun hashPin(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(Base64.getDecoder().decode(salt))
        val hashedBytes = md.digest(pin.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashedBytes)
    }

    /**
     * Verifies a plain text PIN against a stored hash and salt.
     */
    fun verifyPin(pin: String, salt: String, hash: String): Boolean {
        val computedHash = hashPin(pin, salt)
        return computedHash == hash
    }
}
