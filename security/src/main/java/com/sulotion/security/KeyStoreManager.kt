// original: KeyStoreManager
package com.sulotion.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.nio.charset.StandardCharsets
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeyStoreManager {

    private const val PROVIDER = "AndroidKeyStore"
    private const val SIGNING_ALIAS = "device_signing_key"
    private const val LOCAL_AES_ALIAS = "device_local_aes_key"

    private const val EC_CURVE = "secp256r1"
    private const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
    private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val AES_KEY_SIZE_BITS = 256
    private const val GCM_TAG_SIZE_BITS = 128

    private const val LOCAL_PAYLOAD_VERSION = "ks1"
    private const val PAYLOAD_SEPARATOR = ":"

    data class SignedRequest(
        val method: String,
        val path: String,
        val body: String,
        val timestampMillis: Long,
        val nonce: String,
        val canonical: String,
        val signatureBase64: String,
        val publicKeyBase64: String,
    )

    data class TimeBoundApiHeaders(
        val apiKeyToken: String,
        val packageName: String,
        val deviceId: String,
        val requestId: String,
        val timestampMillis: Long,
    ) {
        // Tự convert từ timestampMillis sang String
        val timestamp: String
            get() = timestampMillis.toString()
    }

    private fun getKeyStore(): KeyStore {
        return KeyStore.getInstance(PROVIDER).apply {
            load(null)
        }
    }

    /**
     * Backward-compatible entry point for the current sample code.
     * Creates both the local AES key and the EC signing key if they do not exist.
     */
    fun createIfNeeded() {
        createSigningKeyIfNeeded()
        createLocalAesKeyIfNeeded()
    }

    fun createSigningKeyIfNeeded() {
        val keyStore = getKeyStore()
        if (keyStore.containsAlias(SIGNING_ALIAS)) {
            return
        }

        val generator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            PROVIDER,
        )
        val spec = KeyGenParameterSpec.Builder(
            SIGNING_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY,
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec(EC_CURVE))
            .setDigests(KeyProperties.DIGEST_SHA256)
            .build()

        generator.initialize(spec)
        generator.generateKeyPair()
    }

    fun createLocalAesKeyIfNeeded() {
        val keyStore = getKeyStore()
        if (keyStore.containsAlias(LOCAL_AES_ALIAS)) {
            return
        }

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            PROVIDER,
        )
        val spec = KeyGenParameterSpec.Builder(
            LOCAL_AES_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(AES_KEY_SIZE_BITS)
            .setRandomizedEncryptionRequired(true)
            .build()

        generator.init(spec)
        generator.generateKey()
    }

    fun getPrivateKey(): PrivateKey {
        createSigningKeyIfNeeded()
        val keyStore = getKeyStore()
        return keyStore.getKey(SIGNING_ALIAS, null) as PrivateKey
    }

    fun getPublicKey(): PublicKey {
        createSigningKeyIfNeeded()
        val keyStore = getKeyStore()
        return keyStore.getCertificate(SIGNING_ALIAS).publicKey
    }

    fun getPublicKeyBase64(): String {
        return encodeBase64(getPublicKey().encoded)
    }

    fun getPublicKeyPem(): String {
        return buildString {
            appendLine("-----BEGIN PUBLIC KEY-----")
            getPublicKeyBase64().chunked(64).forEach(::appendLine)
            append("-----END PUBLIC KEY-----")
        }
    }

    fun encryptLocal(plainText: String): String {
        createLocalAesKeyIfNeeded()

        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getLocalAesKey())

        val cipherText = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
        return listOf(
            LOCAL_PAYLOAD_VERSION,
            encodeBase64(cipher.iv),
            encodeBase64(cipherText),
        ).joinToString(PAYLOAD_SEPARATOR)
    }

    fun decryptLocal(payload: String): String {
        val parts = payload.split(PAYLOAD_SEPARATOR)
        require(parts.size == 3 && parts[0] == LOCAL_PAYLOAD_VERSION) {
            "Encrypted payload format is invalid"
        }

        createLocalAesKeyIfNeeded()

        val iv = decodeBase64(parts[1])
        val cipherText = decodeBase64(parts[2])
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getLocalAesKey(),
            GCMParameterSpec(GCM_TAG_SIZE_BITS, iv),
        )

        val plainBytes = cipher.doFinal(cipherText)
        return String(plainBytes, StandardCharsets.UTF_8)
    }

    fun createTimeBoundApiHeaders(
        encryptedApiKeyPayload: String,
        packageName: String,
        deviceId: String,
        requestId: String = java.util.UUID.randomUUID().toString(),
        timestampMillis: Long = NativeSecurity.getCurrentTimeMillis(),
    ): TimeBoundApiHeaders {
        require(packageName.isNotBlank()) {
            "Package name must not be blank"
        }
        require(deviceId.isNotBlank()) {
            "Device id must not be blank"
        }
        require(requestId.isNotBlank()) {
            "Request id must not be blank"
        }

        val apiKey = decryptLocal(encryptedApiKeyPayload)
        Log.e("kh45", "decryptLocal: $apiKey")
        return TimeBoundApiHeaders(
            apiKeyToken = NativeSecurity.createTimeBoundApiKeyToken(
                apiKey,
                packageName,
                deviceId,
                requestId,
                timestampMillis,
            ),
            packageName = packageName,
            deviceId = deviceId,
            requestId = requestId,
            timestampMillis = timestampMillis,
        )
    }

    fun createHeader(
        apiKey: String,
        packageName: String,
        deviceId: String,
        requestId: String = java.util.UUID.randomUUID().toString(),
        timestampMillis: Long = NativeSecurity.getCurrentTimeMillis(),
        apiKeyEncryption: Boolean = true
    ): TimeBoundApiHeaders {
        require(packageName.isNotBlank()) {
            "Package name must not be blank"
        }
        require(deviceId.isNotBlank()) {
            "Device id must not be blank"
        }
        require(requestId.isNotBlank()) {
            "Request id must not be blank"
        }

        val token = if (apiKeyEncryption) {
            NativeSecurity.createTimeBoundApiKeyToken(
                apiKey,
                packageName,
                deviceId,
                requestId,
                timestampMillis,
            )
        } else {
            apiKey
        }
        return TimeBoundApiHeaders(
            apiKeyToken = token,
            packageName = packageName,
            deviceId = deviceId,
            requestId = requestId,
            timestampMillis = timestampMillis,
        )
    }

    fun signRequest(
        method: String,
        path: String,
        body: String,
        timestampMillis: Long = System.currentTimeMillis(),
        nonce: String = java.util.UUID.randomUUID().toString(),
    ): SignedRequest {
        val canonical = canonicalRequest(method, path, body, timestampMillis, nonce)
        return SignedRequest(
            method = method.uppercase(),
            path = path,
            body = body,
            timestampMillis = timestampMillis,
            nonce = nonce,
            canonical = canonical,
            signatureBase64 = sign(canonical),
            publicKeyBase64 = getPublicKeyBase64(),
        )
    }

    fun verifyRequestSignature(
        canonical: String,
        signatureBase64: String,
        publicKey: PublicKey = getPublicKey(),
    ): Boolean {
        val verifier = Signature.getInstance(SIGNATURE_ALGORITHM)
        verifier.initVerify(publicKey)
        verifier.update(canonical.toByteArray(StandardCharsets.UTF_8))
        return verifier.verify(decodeBase64(signatureBase64))
    }

    fun canonicalRequest(
        method: String,
        path: String,
        body: String,
        timestampMillis: Long,
        nonce: String,
    ): String {
        return listOf(
            method.uppercase(),
            path,
            timestampMillis.toString(),
            nonce,
            sha256Base64(body.toByteArray(StandardCharsets.UTF_8)),
        ).joinToString("\n")
    }

    fun deleteKeys() {
        val keyStore = getKeyStore()
        if (keyStore.containsAlias(SIGNING_ALIAS)) {
            keyStore.deleteEntry(SIGNING_ALIAS)
        }
        if (keyStore.containsAlias(LOCAL_AES_ALIAS)) {
            keyStore.deleteEntry(LOCAL_AES_ALIAS)
        }
    }

    private fun getLocalAesKey(): SecretKey {
        val keyStore = getKeyStore()
        return keyStore.getKey(LOCAL_AES_ALIAS, null) as SecretKey
    }

    private fun sign(canonical: String): String {
        val signer = Signature.getInstance(SIGNATURE_ALGORITHM)
        signer.initSign(getPrivateKey())
        signer.update(canonical.toByteArray(StandardCharsets.UTF_8))
        return encodeBase64(signer.sign())
    }

    private fun sha256Base64(bytes: ByteArray): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256").digest(bytes)
        return encodeBase64(digest)
    }

    private fun encodeBase64(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun decodeBase64(value: String): ByteArray {
        return Base64.decode(value, Base64.NO_WRAP)
    }
}
