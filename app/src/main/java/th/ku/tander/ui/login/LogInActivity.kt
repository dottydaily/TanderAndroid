package th.ku.tander.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_log_in.*
import org.json.JSONObject
import th.ku.tander.MainActivity
import th.ku.tander.R
import th.ku.tander.helper.KeyStoreManager
import th.ku.tander.helper.RequestManager

class LogInActivity : AppCompatActivity() {

    private val SIGNUP_REQUEST_CODE = 0
    private var username: String? = null
    private var password: String? = null
    private var responseStatusCode: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        window.statusBarColor = Color.WHITE
    }

    override fun onStart() {
        super.onStart()

        handleLogInButtonBehavior()
        handleSignUpButtonBehavior()
        handlePasswordEditTextBehavior()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGNUP_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val newUsername = data?.getStringExtra("username")
                val newPassword = data?.getStringExtra("password")

                // update username&password edittext
                if (newUsername != null && newPassword != null) {
                    username_edit_text.setText(newUsername)
                    password_edit_text.setText(newPassword)

                    login()
                }
            }
        }
    }

    ///////////////////
    // Helper method //
    ///////////////////

    private fun handleLogInButtonBehavior() {
        login_button.setOnClickListener {
            login()
        }
    }

    private fun handleSignUpButtonBehavior() {
        signup_button.setOnClickListener {
            val signup = Intent(this, SignUpActivity::class.java)
            startActivityForResult(signup, SIGNUP_REQUEST_CODE)
        }
    }

    private fun handlePasswordEditTextBehavior() {
        password_edit_text.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login()
            } else if (actionId == EditorInfo.IME_ACTION_NEXT) {
                hideKeyboard(this, v)
            }
            false
        }
    }

    private fun login() {
        hideKeyboard(this, login_button)

        val url = "https://tander-webservice.an.r.appspot.com/users/login"

        username = username_edit_text.text.toString()
        password = password_edit_text.text.toString()
        println("$username, ${password}")

        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            Toast.makeText(this, "Please enter both Username and Password.",
                Toast.LENGTH_SHORT).show()
            println("Missing credentials info")
        } else {
            loading_spinner_login.visibility = View.VISIBLE

            val credentials = HashMap<String, String>()
            credentials["password"] = password!!
            credentials["username"] = username!!
            val requestBody = JSONObject(credentials as Map<*,*>)
            println(requestBody.toString())

            val loginRequest = JsonObjectRequest(Request.Method.POST, url, requestBody,
                Response.Listener { response ->
                    val token = response["accessToken"] as String
                    val user = credentials["username"]!!
                    println("$user : $token")

                    // save to sharedPreferences
                    KeyStoreManager.saveData("TOKEN", token)
                    KeyStoreManager.saveData("USER", user)

                    val resultToken = KeyStoreManager.getData("TOKEN")
                    val resultUser = KeyStoreManager.getData("USER")
                    resultToken.let { println("resultToken: $it") }
                    resultUser.let { println("resultUser: $it") }

                    // go to main page
                    val mainPage = Intent(this, MainActivity::class.java)
                    startActivity(mainPage)
//                    loading_spinner_login.visibility = View.INVISIBLE
                    finish()
                },
                Response.ErrorListener { error ->
                    loading_spinner_login.visibility = View.INVISIBLE

                    responseStatusCode = error.networkResponse.statusCode
                    println("STATUS CODE : ${responseStatusCode}")
                    if (responseStatusCode == 404) {
                        println("User not found.")
                        Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show()
                    } else if (responseStatusCode == 403){
                        Toast.makeText(this, "Wrong password.", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            loginRequest.setRetryPolicy(
                DefaultRetryPolicy(3000, 3, 2f)
            )

            RequestManager.add(loginRequest)
        }
    }

    private fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
