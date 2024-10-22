package th.ku.tander

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import com.android.volley.Response
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_splash.*
import th.ku.tander.helper.KeyStoreManager
import th.ku.tander.helper.LocationRequester
import th.ku.tander.helper.RequestManager
import th.ku.tander.helper.SocketManager
import th.ku.tander.ui.login.LogInActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.statusBarColor = Color.WHITE
    }

    override fun onResume() {
        super.onResume()

        KeyStoreManager.start(applicationContext)
        RequestManager.start(applicationContext)
        SocketManager.start()
        splash_loading_text_view.text = "Getting location..."

        print("========== Requesting Location Permissions ==========")
        LocationRequester.start(this)

        // observer for enter main activity after got location
        val locationObserver = Observer<LatLng> { location ->
            if (location != null) {
                println("========== ${location} ==========")

                checkLogin()
            }
        }
        LocationRequester.getLiveLocation().observe(this, locationObserver)
    }

    // when grant permission, do request location
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        LocationRequester.start(this)
    }

    ///////////////////
    // Helper Method //
    ///////////////////

    fun checkLogin() {
//        KeyStoreManager.clearAll()

        val name = KeyStoreManager.getData("USER")
        val token = KeyStoreManager.getData("TOKEN")

        if (name != null && token != null) {
            splash_loading_text_view.text = "Logging in..."
            RequestManager.verifyToken(name, token,
                Response.Listener {
                    val isVerify = it.toBoolean()
                    if (isVerify) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        startActivity(Intent(this, LogInActivity::class.java))
                        finish()
                    }
                }, Response.ErrorListener { error ->
                    error.printStackTrace()
                }
            )
        } else {
            startActivity(Intent(this, LogInActivity::class.java))
            finish()
        }
    }
}
