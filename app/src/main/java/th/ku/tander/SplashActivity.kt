package th.ku.tander

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.Observer
import com.google.android.gms.maps.model.LatLng
import th.ku.tander.helper.LocationRequester
import th.ku.tander.helper.RequestManager

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // observer for enter main activity after got location
        val locationObserver = Observer<LatLng> { location ->
            if (location != null) {
                // start main activity
                startActivity(Intent(this, MainActivity::class.java))

                // close this activity
                finish()
            }
        }

        RequestManager.start(this)
        LocationRequester.start(this)
        LocationRequester.getLiveLocation().observe(this, locationObserver)
    }
}
