package th.ku.tander.ui.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import th.ku.tander.R

class CategorySelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_select)

        supportActionBar?.title = "Select Restaurant Type"
        supportActionBar?.setHomeButtonEnabled(true)
    }
}
