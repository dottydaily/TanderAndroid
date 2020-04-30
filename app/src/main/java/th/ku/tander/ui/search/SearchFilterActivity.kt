package th.ku.tander.ui.search

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_search_filter.*
import kotlinx.android.synthetic.main.activity_search_filter.view.*
import th.ku.tander.R

class SearchFilterActivity : AppCompatActivity() {
    private val CATEGORY_ACTIVITY_REQUEST_CODE = 0
    private var startPrice: Int = 0
    private var category: String? = null
    private var keyword: String? = null
    private var comeFromSearch: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_filter)

        this.keyword = intent.getStringExtra("keyword")
        supportActionBar?.title = "Filter by Detail"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setSeekBarBehavior()
        setCategoryButton()
        setFilterButton()
    }

    override fun onStart() {
        super.onStart()

        println("========== START : FILTER PAGE ==========")
    }

    override fun onResume() {
        super.onResume()
        println("========= FILTER PAGE =========")

        println("Come from SearchActivity: ${comeFromSearch}")

        if (comeFromSearch) { // setup by using value from SearchActivity
            category = intent.getStringExtra("category")
            startPrice = intent.getIntExtra("startPrice", 0)

            println(category)
            println(startPrice)
        }

        filter_seekbar.progress = startPrice
        start_price_textview.text = "Start Price ${startPrice} Baht."

        if (category == null) {
            goto_category_list_button.text = "Select category"
        } else {
            goto_category_list_button.text = category
        }
    }

    override fun onPause() {
        super.onPause()

        println("========== PAUSE : FILTER PAGE ==========")
    }

    override fun onStop() {
        super.onStop()
        println("========== STOP : FILTER PAGE ==========")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        // handle back button on action bar
        // make it just finish this view to prevent app from creating a new SearchActivity
        android.R.id.home -> {
            comeFromSearch = true
            finish()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        category = data?.getStringExtra("category")

        if (requestCode == CATEGORY_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (category != null) {
                    category = category?.capitalize()
                }

                println(category)
            }
        }
    }

    ///////////////////
    // Helper Method //
    ///////////////////

    private fun setSeekBarBehavior() {
        filter_seekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            // update text status "Start Price _____ Baht.
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                startPrice = progress
                start_price_textview.text = "Start Price ${startPrice} Baht."
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // do nothing
            }
        })
    }

    private fun setCategoryButton() {
        goto_category_list_button.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                goto_category_list_button.setBackgroundColor(Color.parseColor("#FFEFEFEF"))
            } else if (event.action == MotionEvent.ACTION_UP) {
                goto_category_list_button.setBackgroundColor(Color.WHITE)

                val categoryList = Intent(this, CategoryActivity::class.java)
                categoryList.putExtra("category", category)
                comeFromSearch = false
                println("GO TO CATEGORY: ${comeFromSearch}")
                startActivityForResult(categoryList, CATEGORY_ACTIVITY_REQUEST_CODE)
            }
            true
        }
    }

    private fun setFilterButton() {
        filter_button.setOnClickListener {
            val intent = Intent()
            intent.putExtra("startPrice", startPrice)
            intent.putExtra("category", category)
            comeFromSearch = true

            setResult(Activity.RESULT_OK, intent)
            finish()

            println("Back with filter result.")
        }
    }
}
