package th.ku.tander.ui.restaurant

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_restaurant.*
import org.json.JSONObject
import th.ku.tander.R
import th.ku.tander.model.Restaurant
import th.ku.tander.ui.lobby.CreateLobbyActivity

class RestaurantActivity : AppCompatActivity() {

    private val restaurantViewModel by viewModels<RestaurantViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant)

        supportActionBar?.title = "Restaurant's Detail"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val restaurantJson = intent.getStringExtra("restaurantJson")

        if (restaurantJson != null) {
            restaurantViewModel.restaurant.apply { value = Restaurant(JSONObject(restaurantJson)) }
        }
    }

    override fun onStart() {
        super.onStart()

        println("========== START: RESTAURANT ==========")

        restaurant_category_text_view.isSelected = true
    }

    override fun onResume() {
        super.onResume()

        println("========== RESUME: RESTAURANT ==========")

        handleCreateButton()

        restaurantViewModel.restaurant.observe(this, Observer {
            if (it != null) restaurantViewModel.fetchRestaurantImage()
        })

        restaurantViewModel.isDoneLoaded.observe(this, Observer { isDone ->
            if (isDone) updateUiData()
        })
    }

    override fun onPause() {
        super.onPause()

        println("========== PAUSE: RESTAURANT ==========")
    }

    override fun onStop() {
        super.onStop()

        println("========== STOP: RESTAURANT ==========")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        android.R.id.home -> {
            finish()
            false
        }
        else -> {
            false
        }
    }

    ///////////////////
    // Helper Method //
    ///////////////////

    private fun updateUiData() {
        val restaurant = restaurantViewModel.restaurant.value
        restaurant?.apply {
            restaurant_text_view.text = name
            restaurant_text_view.isSelected = true
            restaurant_price_text_view.text = "Price start at $startPrice.-"
            restaurant_address_text_view.text = address

            if (promotions.isEmpty()) {
                restaurant_promotion_layout.visibility = View.GONE
            }

            var category = categories.joinToString(", ",
                transform = { member: String -> member.capitalize() })
            restaurant_category_text_view.text = category
            restaurant_category_text_view.postDelayed({
                restaurant_category_text_view.isSelected = true
            }, 3000)

            supportActionBar?.title = name

            restaurant_loading_spinner.visibility = View.INVISIBLE
            restaurant_content_view.visibility = View.VISIBLE
        }

        restaurantViewModel.image.value.let { restaurant_image_view.setImageBitmap(it) }
    }

    private fun handleCreateButton() {
        restaurant_create_button.setOnClickListener {
            restaurantViewModel.checkIfAlreadyCreateLobby()

            restaurantViewModel.didCreateAnotherBefore.observe(
                this, Observer { didCreate ->
                    if (didCreate != null) {
                        if (didCreate) {  // don't create
                            Toast.makeText(this, "You already have another lobby!",
                                Toast.LENGTH_SHORT).show()
                        } else { // do create a new lobby
                            val intent = Intent(this, CreateLobbyActivity::class.java)

                            restaurantViewModel.restaurant.apply {
                                if (value != null) {
                                    intent.putExtra("restaurantJson", value!!.toJson().toString())
                                    startActivity(intent)
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}
