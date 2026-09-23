package com.example.data.local

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecurityUtils {

    private const val ITERATIONS = 12000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16

    /**
     * Generates a cryptographically strong random salt.
     */
    fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return salt
    }

    /**
     * Hashes a password using PBKDF2WithHmacSHA256 with salt.
     * Falls back to SHA-256 with salt if PBKDF2 algorithm is unavailable.
     */
    fun hashPassword(password: String, salt: ByteArray): String {
        return try {
            val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
            val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val hash = skf.generateSecret(spec).encoded
            bytesToHex(hash)
        } catch (e: Exception) {
            // Robust fallback using SHA-256 with salt
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(salt)
            val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
            bytesToHex(hash)
        }
    }

    /**
     * Verifies that the entered password matches the stored salted hash.
     */
    fun verifyPassword(password: String, salt: ByteArray, storedHash: String): Boolean {
        val calculatedHash = hashPassword(password, salt)
        return calculatedHash.equals(storedHash, ignoreCase = true)
    }

    /**
     * Validates email address format.
     */
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.isNotBlank() && email.matches(emailRegex)
    }

    /**
     * Validates minimum password criteria.
     */
    fun isValidPassword(password: String): Pair<Boolean, String> {
        if (password.length < 6) {
            return Pair(false, "Password must be at least 6 characters long")
        }
        return Pair(true, "")
    }

    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        val hexArray = "0123456789abcdef".toCharArray()
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = hexArray[v ushr 4]
            hexChars[i * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
