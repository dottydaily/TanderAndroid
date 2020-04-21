package th.ku.tander.ui.search

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        search_bar_search_page.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                requestRestaurantBySearch(search_bar_search_page.text.toString())
                search_result_table.removeAllViews()
                true
            }
            false
        }
    }

    fun requestRestaurantBySearch(keyword: String) {
        val queue = RequestManager.getQueue()
        val encodedKeyword = URLEncoder.encode(keyword, "utf-8")
        // request
        val url = "https://tander-webservice.an.r.appspot.com/restaurants/search/${encodedKeyword}" +
                "?radius=2000&lat=${LocationRequester.getLocation()?.latitude}" +
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

                    val addingTextView = createTextView(title, Color.WHITE, Color.BLACK)
                    addingTextView.setPadding(30, 30, 15, 30)
                    addingTextView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, 150
                    )

                    val addressTextView = createTextView("-  $address", Color.WHITE, Color.GRAY, textSize = 14f)
                    addressTextView.ellipsize = TextUtils.TruncateAt.END
                    addressTextView.setPadding(15, 30, 30, 30)
                    addressTextView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    val linearRow = LinearLayout(this)
                    linearRow.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    linearRow.gravity = Gravity.CENTER_VERTICAL
                    linearRow.addView(addingTextView)
                    linearRow.addView(addressTextView)

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
