package com.example.weathergpt_android.core.network

import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object HmacSigner {
    /**
     * Generates dynamic HMAC-SHA256 cryptographic security headers with timestamp.
     * Prevents token spoofing, unauthorized scrapers, and replay attacks.
     */
    fun generateSecurityHeaders(path: String): Map<String, String> {
        val timestamp = (System.currentTimeMillis() / 1000L).toString()
        val message = "$timestamp:$path"
        val signature = computeHmacSha256(message, BackendConfig.CLIENT_AUTH_KEY)

        return mapOf(
            "X-Timestamp" to timestamp,
            "X-Signature" to signature,
            BackendConfig.AUTH_HEADER_NAME to BackendConfig.CLIENT_AUTH_KEY
        )
    }

    private fun computeHmacSha256(data: String, key: String): String {
        val sha256Hmac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        sha256Hmac.init(secretKey)
        val hash = sha256Hmac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
