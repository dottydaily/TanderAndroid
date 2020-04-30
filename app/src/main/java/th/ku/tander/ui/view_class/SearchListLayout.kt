package th.ku.tander.ui.view_class

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.search_list_view.view.*
import org.json.JSONObject
import th.ku.tander.R
import th.ku.tander.model.Restaurant
import th.ku.tander.ui.restaurant.RestaurantActivity

class SearchListLayout: FrameLayout {

    private var restaurant: Restaurant

    // view
    private val restaurantNameTextView: TextView
    private val restaurantAddressTextView: TextView
    private val button: Button

    constructor(context: Context, restaurantJsonString: String)
            : super(context, null, 0) {
        LayoutInflater.from(context).inflate(R.layout.search_list_view, this)
        restaurant = Restaurant(JSONObject(restaurantJsonString))

        restaurantNameTextView = this.search_list_restaurant_name
        restaurantAddressTextView = this.search_list_restaurant_address
        button = this.search_list_button

        updateDataUi()
    }

    private fun updateDataUi() {
        restaurantNameTextView.text = restaurant.name
        restaurantAddressTextView.text = restaurant.address

        restaurantAddressTextView.postDelayed({
            restaurantAddressTextView.isSelected = true
        }, 3000)

        button.setOnClickListener {
            val intent = Intent(this.context, RestaurantActivity::class.java)
            intent.putExtra("restaurantJson", restaurant.toJson().toString())
            this.context.startActivity(intent)
        }
    }
}