package net.ticherhaz.pokdexclone.utils

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.security.KeyStore
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class QuickSave private constructor(context: Context) {

    /**
     * Secure way to store at Android, because using AndroidKeyStore
     * This encrypt only works on Android
     */
    companion object {
        private const val SHARED_PREFS_NAME = "resitmudah.quicksave"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "SecureStorageKey_v2" // Updated alias for new version
        private const val VALUE_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val TAG_LENGTH = 128 // GCM tag length in bits
        private const val HMAC_ALGORITHM = "HmacSHA256" // Lighter than SHA512
        private const val VERSION = 1 // Data format version

        // Hardcoded HMAC key parts (Base64 encoded) for Flutter compatibility
        private const val PART1 =
            "anythingPassword321" // Replace with your actual part1
        private const val PART2 =
            "passwordAnything123" // Replace with your actual part2

        // Combine the parts to reconstruct the HMAC key
        private val HMAC_KEY: ByteArray by lazy {
            val part1Bytes = Base64.decode(PART1, Base64.NO_WRAP)
            val part2Bytes = Base64.decode(PART2, Base64.NO_WRAP)
            part1Bytes + part2Bytes
        }

        @Volatile
        private var instance: QuickSave? = null

        fun initialize(context: Context) {
            synchronized(this) {
                if (instance == null) {
                    instance = QuickSave(context.applicationContext)
                }
            }
        }

        fun getInstance(): QuickSave {
            return instance ?: throw kotlin.IllegalStateException(
                "QuickSave must be initialized. Call QuickSave.initialize(context) first."
            )
        }
    }

    val preferences: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    // Initialize KeyStore and generate or load the AES key for values
    private val valueSecretKey: SecretKey by lazy {
        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)

            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator =
                    KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
                val keySpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGenerator.init(keySpec)
                keyGenerator.generateKey()
            }
            keyStore.getKey(KEY_ALIAS, null) as? SecretKey
                ?: throw kotlin.Exception("Failed to retrieve or generate key for alias: $KEY_ALIAS")
        } catch (e: Exception) {
            throw kotlin.Exception("Unable to initialize KeyStore or key", e)
        }
    }

    // Obfuscate key names using HMAC-SHA256
    fun obfuscateKey(key: String): String {
        return try {
            val mac = Mac.getInstance(HMAC_ALGORITHM)
            val secretKeySpec = SecretKeySpec(HMAC_KEY, HMAC_ALGORITHM)
            mac.init(secretKeySpec)
            val hash = mac.doFinal(key.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(hash, Base64.URL_SAFE or Base64.NO_WRAP)
        } catch (_: Exception) {
            ""
        }
    }

    // Compress data using GZIP
    private fun compress(data: String): ByteArray {
        return try {
            val bos = ByteArrayOutputStream()
            GZIPOutputStream(bos).use { it.write(data.toByteArray(Charsets.UTF_8)) }
            bos.toByteArray()
        } catch (e: Exception) {
            ByteArray(0)
        }
    }

    // Decompress data using GZIP
    private fun decompress(data: ByteArray): String {
        return try {
            val bis = data.inputStream()
            val gis = GZIPInputStream(bis)
            val decompressed = gis.readBytes()
            String(decompressed, Charsets.UTF_8)
        } catch (e: Exception) {
            ""
        }
    }

    // Encrypt values before storing
    fun encryptValue(value: String): String {
        return try {
            val compressed = compress(value)
            val cipher = Cipher.getInstance(VALUE_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, valueSecretKey)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(compressed)
            val versioned = byteArrayOf(VERSION.toByte()) + iv + encrypted
            Base64.encodeToString(versioned, Base64.DEFAULT)
        } catch (e: Exception) {
            ""
        }
    }

    // Decrypt values after retrieving
    fun decryptValue(encrypted: String): String {
        return try {
            val combined = Base64.decode(encrypted, Base64.DEFAULT)
            if (combined.size < 13) { // 1 byte version + 12 byte IV
                ""
            }
            val version = combined[0].toInt()
            if (version != VERSION) {
                ""
            }
            val iv = combined.copyOfRange(1, 13) // GCM uses 12-byte IV
            val encryptedData = combined.copyOfRange(13, combined.size)
            val cipher = Cipher.getInstance(VALUE_TRANSFORMATION)
            val spec = GCMParameterSpec(TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, valueSecretKey, spec)
            val decrypted = cipher.doFinal(encryptedData)
            decompress(decrypted)
        } catch (e: Exception) {
            ""
        }
    }

    // Save a value securely (async)
    suspend fun save(key: String, value: Any) = withContext(Dispatchers.IO) {
        try {
            val stringValue = when (value) {
                is Int, is Boolean, is Float, is Long, is String -> value.toString()
                else -> throw kotlin.IllegalArgumentException("Unsupported type")
            }
            val obfuscatedKey = obfuscateKey(key)
            preferences.edit {
                putString(obfuscatedKey, encryptValue(stringValue))
            }
        } catch (e: Exception) {
            throw e
        }
    }

    // Retrieve a value securely (async)
    suspend inline fun <reified T> get(key: String, defaultValue: T): T =
        withContext(Dispatchers.IO) {
            try {
                val obfuscatedKey = obfuscateKey(key)
                val encrypted = preferences.getString(obfuscatedKey, null)
                    ?: return@withContext defaultValue
                val decrypted = decryptValue(encrypted)
                when (T::class) {
                    Int::class -> decrypted.toIntOrNull() as? T ?: defaultValue
                    Boolean::class -> decrypted.toBoolean() as T
                    Float::class -> decrypted.toFloatOrNull() as? T ?: defaultValue
                    Long::class -> decrypted.toLongOrNull() as? T ?: defaultValue
                    String::class -> decrypted as T
                    else -> throw kotlin.IllegalArgumentException("Unsupported type")
                }
            } catch (_: Exception) {
                defaultValue
            }
        }

    // Save an object securely (async)
    suspend fun <T> saveObject(key: String, `object`: T) = withContext(Dispatchers.IO) {
        try {
            val objectString = Gson().toJson(`object`)
            save(key, objectString)
        } catch (e: Exception) {
            throw e
        }
    }

    // Retrieve an object securely (async)
    suspend inline fun <reified T> getObject(key: String): T? = withContext(Dispatchers.IO) {
        try {
            val obfuscatedKey = obfuscateKey(key)
            val encrypted = preferences.getString(obfuscatedKey, null)
                ?: return@withContext null
            val objectString = decryptValue(encrypted)
            val type = object : TypeToken<T>() {}.type
            Gson().fromJson(objectString, type)
        } catch (_: Exception) {
            null
        }
    }

    // Save a list of objects securely (async)
    suspend fun <T> saveObjectsList(key: String, objectList: List<T>) =
        withContext(Dispatchers.IO) {
            try {
                val objectString = Gson().toJson(objectList)
                save(key, objectString)
            } catch (e: Exception) {
                throw e
            }
        }

    // Retrieve a list of objects securely (async)
    suspend inline fun <reified T> getObjectsList(key: String): List<T>? =
        withContext(Dispatchers.IO) {
            try {
                val obfuscatedKey = obfuscateKey(key)
                val encrypted = preferences.getString(obfuscatedKey, null)
                    ?: return@withContext null
                val objectString = decryptValue(encrypted)
                Gson().fromJson(objectString, object : TypeToken<List<T>>() {}.type)
            } catch (_: Exception) {
                null
            }
        }

    // Clear all stored data (async)
    suspend fun clearSession() = withContext(Dispatchers.IO) {
        preferences.edit { clear() }
    }

    // Delete a specific value (async)
    suspend fun deleteValue(key: String): Boolean = withContext(Dispatchers.IO) {
        val obfuscatedKey = obfuscateKey(key)
        if (preferences.contains(obfuscatedKey)) {
            preferences.edit { remove(obfuscatedKey) }
            true
        } else {
            false
        }
    }

    // Check if a key exists (async)
    suspend fun isKeyExists(key: String): Boolean = withContext(Dispatchers.IO) {
        val obfuscatedKey = obfuscateKey(key)
        preferences.contains(obfuscatedKey)
    }
}