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
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.json.JSONObject
import th.ku.tander.R
import th.ku.tander.helper.RequestManager
import kotlin.collections.HashMap

class SignUpActivity : AppCompatActivity() {

    private var firstName: String? = null
    private var lastName: String? = null
    private var birthDate: String? = null
    private var email: String? = null
    private var telephone: String? = null
    private var username: String? = null
    private var password: String? = null
    private var searchStatusCode: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        window.statusBarColor = Color.WHITE
    }

    override fun onStart() {
        super.onStart()

        println("========== START: SIGNUP PAGE ==========")

        handleSignUpButtonBehavior()
        handleBackToLoginButtonBehavior()
        handleUsernameSignUpEditTextBehavior()
        handlePasswordSignUpEditTextBehavior()
    }

    override fun onResume() {
        super.onResume()

        println("========== SIGNUP PAGE ==========")
//        autoFillForDebug()
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

    private fun validateData(): Boolean {
        firstName = firstname_edit_text.text.toString()
        lastName = lastname_edit_text.text.toString()
        val day = birthdate_datepicker.dayOfMonth
        val month = birthdate_datepicker.month
        val year = birthdate_datepicker.year
        birthDate = String.format("%04d-%02d-%02dT00:00:00.000Z", year, month+1, day)
        println("$day, $month, $year")
        email = email_edit_text.text.toString()
        telephone = phone_edit_text.text.toString()
        username = username_signup_edit_text.text.toString()
        password = password_signup_edit_text.text.toString()

        return !(firstName.isNullOrBlank() || lastName.isNullOrBlank() ||
                birthDate.isNullOrBlank() || email.isNullOrBlank() ||
                telephone.isNullOrBlank() || searchStatusCode != 404 ||
                username.isNullOrBlank() || password.isNullOrBlank())
    }

    private fun handleSignUpButtonBehavior() {
        signup_signuppage_button.setOnClickListener {
            if (validateData()) {
                loading_spinner_signup.visibility = View.VISIBLE

                val url = "https://tander-webservice.an.r.appspot.com/users"
                println(url)

                val userDetail = HashMap<String, String>()
                userDetail["firstname"] = firstName!!
                userDetail["lastname"] = lastName!!
                userDetail["birthdate"] = birthDate!!
                userDetail["email"] = email!!
                userDetail["telephone"] = telephone!!
                userDetail["username"] = username!!
                userDetail["password"] = password!!
                userDetail["role"] = "user"
                val userDetailJson = JSONObject(userDetail as Map<*,*>)
                println(userDetailJson)

                val signUpRequest = object: StringRequest(Request.Method.POST, url,
                    Response.Listener { response ->
                        loading_spinner_signup.visibility = View.INVISIBLE

                        println(response)
                        Toast.makeText(this, "Sign up complete..", Toast.LENGTH_SHORT).show()

                        val intent = Intent()
                        intent.putExtra("username", username)
                        intent.putExtra("password", password)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    },
                    Response.ErrorListener { error ->
                        loading_spinner_signup.visibility = View.INVISIBLE

                        println(error.message)
                        Toast.makeText(this, "Server error occurred. Please try again.",
                            Toast.LENGTH_SHORT).show()
                    }) {

                    // custom request to make it can send json but get response with string
                    override fun getBodyContentType(): String {
                        return "application/json; charset=utf-8"
                    }
                    override fun getBody(): ByteArray {
                        return userDetailJson.toString().toByteArray()
                    }
                }

                RequestManager.add(signUpRequest)
            } else {
                Toast.makeText(this, "Please enter all valid information.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleBackToLoginButtonBehavior() {
        backto_login_button.setOnClickListener {
            finish()
        }
    }

    private fun handleUsernameSignUpEditTextBehavior() {
        username_signup_edit_text.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                val searchUsername = username_signup_edit_text.text.toString()

                if (!searchUsername.isNullOrBlank()) {
                    val url = "https://tander-webservice.an.r.appspot.com/users/available/$searchUsername"
                    println(url)
                    val searchUserRequest = object: StringRequest(Request.Method.GET, url,
                        Response.Listener { response ->
                            println("STATUS CODE: $searchStatusCode")

                            username_signup_title.text = "ALREADY HAVE THIS ACCOUNT - USERNAME"
                            username_signup_title.setTextColor(Color.RED)
                            username_signup_edit_text.setTextColor(Color.RED)
                            Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                        },
                        Response.ErrorListener { _ ->
                            if (searchStatusCode == 404) {
                                println("STATUS CODE: $searchStatusCode")

                                val color = Color.parseColor("#0b8500")
                                username_signup_title.text = "USERNAME"
                                username_signup_title.setTextColor(color)
                                username_signup_edit_text.setTextColor(color)
                            }
                        }
                    ) {
                        override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {
                            searchStatusCode = response?.statusCode
                            return super.parseNetworkResponse(response)
                        }

                        override fun parseNetworkError(volleyError: VolleyError?): VolleyError {
                            searchStatusCode = volleyError?.networkResponse?.statusCode
                            return super.parseNetworkError(volleyError)
                        }
                    }

                    searchUserRequest.setRetryPolicy(
                        DefaultRetryPolicy(3000, 3, 2f)
                    )

                    RequestManager.add(searchUserRequest)
                }
            }
            false
        }
    }

    private fun handlePasswordSignUpEditTextBehavior() {
        password_signup_edit_text.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                hideKeyboard(this, v)
            }
            false
        }
    }

    private fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun autoFillForDebug() {
        firstname_edit_text.setText("Pornpat")
        lastname_edit_text.setText("Santibuppakul")
        email_edit_text.setText("dottypurkt@gmail.com")
        phone_edit_text.setText("0891085695")
        username_signup_edit_text.setText("dottydaily")
        password_signup_edit_text.setText("dailydotty")
    }
}
