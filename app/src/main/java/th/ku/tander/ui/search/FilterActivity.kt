package th.ku.tander.ui.search

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_filter.*
import th.ku.tander.R

class FilterActivity : AppCompatActivity() {

    private var startPrice: Int = 0
    private var category: String = "NONE"
    private var keyword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        this.keyword = intent.getStringExtra("keyword")
        supportActionBar?.title = "Filter by Detail"
        setSeekBarBehavior()
        setCategoryButton()
        setFilterButton()
    }

    fun setSeekBarBehavior() {
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

    fun setCategoryButton() {
        goto_category_list_button.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                goto_category_list_button.setBackgroundColor(Color.parseColor("#FFEFEFEF"))
            } else if (event.action == MotionEvent.ACTION_UP) {
                goto_category_list_button.setBackgroundColor(Color.WHITE)

                val categoryList = Intent(this, CategoryActivity::class.java)
                startActivityForResult(categoryList, 0)
            }
            true
        }
    }

    fun setFilterButton() {
        filter_button.setOnTouchListener { v, event ->
            val intent = Intent()
            intent.putExtra("startPrice", startPrice)
            intent.putExtra("category", category)

            setResult(Activity.RESULT_OK, intent)
            finish()

            print("Back with filter result.")
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        // handle back button on action bar
        // make it just finish this view to prevent app from creating a new SearchActivity
        android.R.id.home -> {
            finish()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }
}