package th.ku.tander.ui.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import kotlinx.android.synthetic.main.activity_search.*
import th.ku.tander.R
import th.ku.tander.helper.LocationRequester
import th.ku.tander.helper.RequestManager
import java.net.URLEncoder

class SearchActivity : AppCompatActivity() {

    private val FILTER_ACTIVITY_REQUEST_CODE = 0
    private var keyword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        search_bar_search_page.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                keyword = search_bar_search_page.text.toString()
                requestRestaurantBySearch(keyword!!)
                search_result_table.removeAllViews()
                true
            }
            false
        }

        // focus on searchbar and show keyboard by default
        makeSearchBarFocus()

//        supportActionBar?.title = "Search"
        setSupportActionBar(findViewById(R.id.search_actionbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.filter_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.filter_button -> {
            val popupFilter = Intent(this, FilterActivity::class.java)
            startActivityForResult(popupFilter, FILTER_ACTIVITY_REQUEST_CODE)
            true
        }

        android.R.id.home -> {
            finish()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        search_bar_search_page.clearFocus()

        if (requestCode == FILTER_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (keyword.isNullOrBlank()) {
                    Toast.makeText(this, "No search.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Searching: ${keyword}", Toast.LENGTH_SHORT).show()
                    requestRestaurantBySearch(keyword!!)
                }
            }
        }
    }

    fun requestRestaurantBySearch(keyword: String) {
        val queue = RequestManager.getQueue()
        val encodedKeyword = URLEncoder.encode(keyword, "utf-8")
        // request
        val url = "https://tander-webservice.an.r.appspot.com/restaurants/search/${encodedKeyword}" +
                "?lat=${LocationRequester.getLocation()?.latitude}" +
                "&lon=${LocationRequester.getLocation()?.longitude}"
        println(url)
        val restaurantRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                println("========== Got response! ==========")

                // manage search text and hint
                search_bar_search_page.clearFocus()
                search_bar_search_page.setText("")

                for (i in 0 until response.length()) {
                    val restaurant = response.getJSONObject(i)
                    val title = restaurant.getString("name")
                    val address = restaurant.getString("address")

                    // restaurant's name
                    val addingTextView = createTextView(title, Color.WHITE, Color.BLACK)
                    addingTextView.setPadding(30, 30, 15, 30)
                    addingTextView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, 150
                    )

                    // restaurant's address
                    val addressTextView = createTextView("-  $address", Color.WHITE, Color.GRAY, textSize = 14f)
                    addressTextView.ellipsize = TextUtils.TruncateAt.END
                    addressTextView.setPadding(15, 30, 30, 30)
                    addressTextView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // group both
                    val linearRow = LinearLayout(this)
                    linearRow.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    linearRow.gravity = Gravity.CENTER_VERTICAL
                    linearRow.addView(addingTextView)
                    linearRow.addView(addressTextView)

                    // handle action
                    linearRow.setOnClickListener {
                        val name = linearRow.getChildAt(0) as TextView
                        Toast.makeText(this, name.text, Toast.LENGTH_SHORT).show()
                    }

                    // divider
                    val divider = View(this)
                    divider.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 1)
                    divider.setBackgroundColor(Color.DKGRAY)

                    search_result_table.addView(linearRow)
                    search_result_table.addView(divider)
                }

                // hiding spinner again and show toast
                loading_spinner_searchpage.visibility = View.INVISIBLE
                Toast.makeText(this, "Found ${response.length()} restaurants.",
                    Toast.LENGTH_LONG).show()
            },
            Response.ErrorListener { error ->
                println(error.message)
            }
        )
        restaurantRequest.setRetryPolicy(
            DefaultRetryPolicy(
                5000, 3, 1.0F
            )
        )

        queue.add(restaurantRequest)
        queue.start()

        // show loading spinner
        loading_spinner_searchpage.visibility = View.VISIBLE
    }

    fun makeSearchBarFocus() {
        search_bar_search_page.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(search_bar_search_page, InputMethodManager.SHOW_IMPLICIT)
    }

    fun createTextView(title: String, bgColor: Int, textColor: Int,
                       maxLine: Int = 1, textSize: Float = 16f): TextView{
        val result = TextView(this)
        result.text = "$title"
        result.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
        result.gravity = Gravity.CENTER_VERTICAL
        result.setBackgroundColor(bgColor)
        result.setTextColor(textColor)
        result.maxLines = maxLine
        result.textSize = textSize
        result.setPadding(30)

        return result
    }
}