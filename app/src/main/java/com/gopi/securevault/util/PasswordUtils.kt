package com.gopi.securevault.util

import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64

object PasswordUtils {

    fun generateSalt(bytes: Int = 16): String {
        val salt = ByteArray(bytes)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    fun hashWithSalt(input: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(Base64.decode(salt, Base64.NO_WRAP))
        val hashed = md.digest(input.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hashed, Base64.NO_WRAP)
    }

    fun isPasswordValid(password: String): Boolean {
        if (password.length < 8) return false
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        return hasLetter && hasDigit && hasSpecial
    }
}
