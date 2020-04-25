package th.ku.tander.ui.login

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ScrollView
import kotlinx.android.synthetic.main.activity_sign_up.*
import th.ku.tander.R

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        window.statusBarColor = Color.WHITE
    }

    override fun onStart() {
        super.onStart()

//        handleDatePickerBehavior()
        handleBackToLoginButtonBehavior()
    }

    override fun onResume() {
        super.onResume()

        println("========== SIGNUP PAGE ==========")
    }

    override fun onPause() {
        super.onPause()

        println("========== PAUSE: SIGNUP PAGE ==========")
    }

    override fun onStop() {
        super.onStop()

        println("========== STOP: SIGNUP PAGE ==========")
    }

    ///////////////////
    // Helper method //
    ///////////////////

    private fun handleBackToLoginButtonBehavior() {
        backto_login_button.setOnClickListener {
            finish()
        }
    }
}
