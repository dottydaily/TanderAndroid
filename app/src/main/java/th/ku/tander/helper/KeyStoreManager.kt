package th.ku.tander.helper

import androidx.security.crypto.MasterKeys
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeyStoreManager {
    @Volatile private var ks: KeyStore? = null
    @Volatile private var iv: ByteArray? = null

    fun start() {
        if (ks == null) {
            ks = KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
            }
        }
    }

    fun encrypt(data: String): ByteArray? {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        iv = cipher.iv

        return cipher.doFinal(data.toByteArray())
    }

    fun decrypt(encrypted: ByteArray): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

        val decoded = cipher.doFinal(encrypted)
        return String(decoded, Charsets.UTF_8)
    }

    private fun getSecretKey(): SecretKey {
        val aliasKeyStore = getAlias()

        if (ks == null) {
            start()
        }

        val secretKeyEntry = ks?.getEntry(aliasKeyStore, null) as KeyStore.SecretKeyEntry

        return secretKeyEntry.secretKey
    }

    private fun getAlias(): String{
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        return masterKeyAlias
    }
}