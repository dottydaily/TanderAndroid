package th.ku.tander.ui.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import th.ku.tander.R
import th.ku.tander.helper.RequestManager

class CategoryActivity : AppCompatActivity() {

    private var categoryList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        supportActionBar?.title = "Select Restaurant Type"
        supportActionBar?.setHomeButtonEnabled(true)

        requestCategoryList()

        setClearButtonBehavior()
        setSelectButtonBehavior()
    }

    fun requestCategoryList() {
        val queue = RequestManager.getQueue()
        val url = "https://tander-webservice.an.r.appspot.com/restaurants/categoryList"
        print(url)

        val categoryListRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                print(response)
            }, Response.ErrorListener { error ->
                println(error.message)
            })

        categoryListRequest.setRetryPolicy(
            DefaultRetryPolicy(
                3000, 3, 1.0F
            )
        )
        queue.add(categoryListRequest)
        queue.start()
    }

    fun setClearButtonBehavior() {

    }

    fun setSelectButtonBehavior() {

    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }
}
