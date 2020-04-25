package th.ku.tander.ui.login

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_log_in.*
import th.ku.tander.R

class LogInActivity : AppCompatActivity() {

    private val SIGNUP_REQUEST_CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        window.statusBarColor = Color.WHITE
        handleSignUpBehavior()
    }

    override fun onResume() {
        super.onResume()

        println("========== LOGIN PAGE ==========")
    }

    override fun onPause() {
        super.onPause()

        println("========== PAUSE: LOGIN PAGE ==========")
    }

    override fun onStop() {
        super.onStop()

        println("========== STOP: LOGIN PAGE ==========")
    }

    ///////////////////
    // Helper method //
    ///////////////////

    private fun handleSignUpBehavior() {
        signup_button.setOnClickListener {
            val signup = Intent(this, SignUpActivity::class.java)
            startActivityForResult(signup, SIGNUP_REQUEST_CODE)
        }
    }
}
