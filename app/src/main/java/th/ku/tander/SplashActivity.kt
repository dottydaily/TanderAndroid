package th.ku.tander

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import com.google.android.gms.maps.model.LatLng
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

        var sp = getSharedPreferences("TANDER", Context.MODE_PRIVATE)
        val token = sp.getString("TOKEN", null)

        if (token == null) {
            startActivity(Intent(this, LogInActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }

        finish()
    }
}
