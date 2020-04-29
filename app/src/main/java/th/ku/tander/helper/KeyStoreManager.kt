package th.ku.tander.helper

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeyStoreManager {
    @Volatile private var context: Context? = null
    @Volatile private var sharedPreferences: SharedPreferences? = null

    fun start(context: Context) {
        if (this.context == null) {
            this.context = context
        }
        if (sharedPreferences == null) {
            sharedPreferences = EncryptedSharedPreferences.create(
                "TANDER", getAlias(), context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    fun saveData(name: String, data: String) {
        val editor = sharedPreferences?.edit()

        editor?.putString(name, data)
        editor?.commit()
    }

    fun getData(name: String): String? {
        return sharedPreferences?.getString(name, null)
    }

    fun clearAll() {
        val editor = sharedPreferences?.edit()
        editor?.clear()
        editor?.commit()
    }

    private fun getAlias(): String{
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        return masterKeyAlias
    }
}