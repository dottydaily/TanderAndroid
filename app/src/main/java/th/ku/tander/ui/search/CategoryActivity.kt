package th.ku.tander.ui.search

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.setPadding
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import kotlinx.android.synthetic.main.activity_category.*
import th.ku.tander.R
import th.ku.tander.helper.RequestManager

class CategoryActivity : AppCompatActivity() {

    private var previousCategory: String? = null
    private var selectedButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        supportActionBar?.title = "Select Restaurant Type"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setClearButtonBehavior()
        setSelectButtonBehavior()
    }

    override fun onResume() {
        super.onResume()
        println("========== CATEGORY PAGE ==========")

        previousCategory = intent.getStringExtra("category")
        if (previousCategory != null) {
            previousCategory = previousCategory?.decapitalize()
        }
        println(previousCategory)

        requestCategoryList()
    }

    override fun onPause() {
        super.onPause()

        println("========== PAUSE : CATEGORY PAGE ==========")
    }

    override fun onStop() {
        super.onStop()

        loading_spinner_category.visibility = View.VISIBLE
        println("========== STOP : CATEGORY PAGE ==========")
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        android.R.id.home -> {
            val intent = Intent()
            intent.putExtra("category", previousCategory)
            setResult(Activity.RESULT_OK, intent)

            finish()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    ///////////////////
    // Helper Method //
    ///////////////////

    private fun requestCategoryList() {
        val url = "https://tander-webservice.an.r.appspot.com/restaurants/categoryList"
        println(url)

        val categoryListRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                for (i in 0 until response.length()) {
                    val category = response[i].toString()

                    // category button & divider
                    val button = createButton(category, Color.WHITE, Color.BLACK)
                    val divider = createDivider()

                    // if already selected category, set button selected
                    if (selectedButton == null &&
                        previousCategory != null && button.text == previousCategory) {

                        button.setBackgroundColor(getColor(R.color.colorPrimary))
                        button.setTextColor(Color.WHITE)

                        selectedButton = button
                        println("set button : ${selectedButton?.text}")
                    }

                    category_table.addView(button)
                    category_table.addView(divider)
                }

                // hiding spinner
                loading_spinner_category.visibility = View.INVISIBLE
            }, Response.ErrorListener { error ->
                println(error.message)
            })

        categoryListRequest.setRetryPolicy(
            DefaultRetryPolicy(
                3000, 3, 1.0F
            )
        )
        RequestManager.start(applicationContext)
        RequestManager.add(categoryListRequest)
    }

    private fun setClearButtonBehavior() {
        category_clear_button.setOnClickListener {
            if (selectedButton != null) {
                selectedButton?.setBackgroundColor(Color.WHITE)
                selectedButton?.setTextColor(Color.BLACK)

                selectedButton = null
            }
        }
    }

    private fun setSelectButtonBehavior() {
        category_select_button.setOnClickListener {
            val intent = Intent()
            if (selectedButton != null)
                intent.putExtra("category", selectedButton?.text.toString())
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun createButton(title: String, bgColor: Int, textColor: Int,
                               maxLine: Int = 1, textSize: Float = 16f): Button {
        val result = Button(this)
        result.text = "$title"
        result.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
        result.gravity = Gravity.CENTER_VERTICAL
        result.setBackgroundColor(bgColor)
        result.setTextColor(textColor)
        result.maxLines = maxLine
        result.textSize = textSize
        result.setPadding(30, 15, 30, 30)
        result.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 150
        )

        result.setOnClickListener {
            if (selectedButton != null) {
                // if press the same category, clear select
                if (result.text == selectedButton?.text) {
                    result.setBackgroundColor(bgColor)
                    result.setTextColor(textColor)

                    println("clear button : ${selectedButton?.text}")
                    selectedButton = null
                } else {    // swap select button
                    selectedButton?.setBackgroundColor(bgColor)
                    selectedButton?.setTextColor(textColor)

                    result.setBackgroundColor(getColor(R.color.colorPrimary))
                    result.setTextColor(Color.WHITE)

                    println("swap button : ${selectedButton?.text} to ${result.text}")
                    selectedButton = result
                }
            } else { // click for the first time
                result.setBackgroundColor(getColor(R.color.colorPrimary))
                result.setTextColor(Color.WHITE)

                selectedButton = result
                println("set button : ${selectedButton?.text}")
            }
        }

        return result
    }

    private fun createDivider(): View {
        val divider = View(this)
        divider.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 1)
        divider.setBackgroundColor(Color.DKGRAY)

        return divider
    }
}