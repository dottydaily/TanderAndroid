package th.ku.tander

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import com.google.android.gms.maps.model.LatLng
import th.ku.tander.helper.KeyStoreManager
import th.ku.tander.helper.LocationRequester
import th.ku.tander.helper.RequestManager
import th.ku.tander.ui.login.LogInActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onResume() {
        super.onResume()

        KeyStoreManager.start(applicationContext)
        RequestManager.start(applicationContext)

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
        println("Checking login")

//        KeyStoreManager.clearAll()

        val token = KeyStoreManager.getData("TOKEN")

        if (token == null) {
            startActivity(Intent(this, LogInActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }

        finish()
    }
}
