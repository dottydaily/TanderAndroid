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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_search.*
import org.json.JSONArray
import th.ku.tander.R
import th.ku.tander.ui.view_class.SearchListLayout

class SearchActivity : AppCompatActivity() {

    private val searchViewModel by viewModels<SearchViewModel>()
    private val FILTER_ACTIVITY_REQUEST_CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        setSupportActionBar(findViewById(R.id.search_actionbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStart() {
        super.onStart()

        println("========== START: SEARCH PAGE ==========")

        handleSearchBarBehavior()

        // focus on search bar and show keyboard by default
        makeSearchBarFocus()
    }

    override fun onResume() {
        super.onResume()

        println("========== RESUME: SEARCH PAGE ==========")

        // update Hint on search bar
        customizeHint()
    }

    override fun onPause() {
        super.onPause()

        println("========== PAUSE: SEARCH PAGE ==========")
    }

    override fun onStop() {
        super.onStop()
        println("========== STOP: SEARCH PAGE ==========")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.filter_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.filter_button -> {
            val popupFilter = Intent(this, SearchFilterActivity::class.java)
            popupFilter.putExtra("category", searchViewModel.categorySpec)
            popupFilter.putExtra("startPrice", searchViewModel.startPrice)
            popupFilter.putExtra("fromSearch", true)
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
                searchViewModel.startPrice = data?.getIntExtra("startPrice", 0)
                searchViewModel.categorySpec = data?.getStringExtra("category")

                if (searchViewModel.keyword == null) {
                    Toast.makeText(this, "No search.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this,
                        "Searching: ${searchViewModel.keyword}", Toast.LENGTH_SHORT).show()
                    search()
                }
            }
        }
    }

    ///////////////////
    // Helper Method //
    ///////////////////

    private fun search() {
        // show loading spinner
        loading_spinner_searchpage.visibility = View.VISIBLE

        searchViewModel.currentLocation.observe(this, Observer {
            if (it != null) searchViewModel.requestRestaurantBySearch()
        })

        searchViewModel.restaurants.observe(this, Observer {
            if (it != null) updateRestaurantListView(it)
        })
    }

    private fun updateRestaurantListView(response: JSONArray) {
        // manage search text and hint
        search_bar_search_page.clearFocus()
        customizeHint()

        // clear old result
        search_result_layout.removeAllViews()

        for (i in 0 until response.length()) {
            val restaurant = response.getJSONObject(i)
            val title = restaurant.getString("name")
            val address = restaurant.getString("address")

            // create restaurant's row & divider
            val restaurantListView = SearchListLayout(
                this,
                restaurant.toString()
            )
//            val restaurantRow = createRestaurantRow(title, address)
            val divider = createDivider()

            search_result_layout.addView(restaurantListView)
            search_result_layout.addView(divider)
        }

        // hiding spinner again and show toast
        loading_spinner_searchpage.visibility = View.INVISIBLE
        Toast.makeText(this, "Found ${response.length()} restaurants.",
            Toast.LENGTH_SHORT).show()
    }

    private fun makeSearchBarFocus() {
        search_bar_search_page.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(search_bar_search_page, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun createDivider(): View {
        val divider = View(this)
        divider.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 1)
        divider.setBackgroundColor(Color.DKGRAY)

        return divider
    }

    private fun customizeHint() {
        search_bar_search_page.setText("")
        search_bar_search_page.hint = "Search"

        if (!searchViewModel.keyword.isNullOrBlank()) {
            search_bar_search_page.hint = searchViewModel.keyword
        }
        if (searchViewModel.startPrice != null && searchViewModel.startPrice != 0) {
            val hint = search_bar_search_page.hint
            search_bar_search_page.hint = "$hint [StartPrice: ${searchViewModel.startPrice}]"
        }
        if (searchViewModel.categorySpec != null) {
            val hint = search_bar_search_page.hint
            search_bar_search_page.hint = "$hint [Category: ${searchViewModel.categorySpec}]"
        }
    }

    private fun handleSearchBarBehavior() {
        search_bar_search_page.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchViewModel.keyword = search_bar_search_page.text.toString()
                search()
            }
            false
        }
    }
}