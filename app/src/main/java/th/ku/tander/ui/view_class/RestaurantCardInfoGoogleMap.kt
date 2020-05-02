package th.ku.tander.ui.view_class

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.restaurant_card_map.view.*
import th.ku.tander.R
import th.ku.tander.model.RestaurantInfoData

class RestaurantCardInfoGoogleMap(val context: Context): GoogleMap.InfoWindowAdapter {

    private lateinit var nameTextView: TextView
    private lateinit var categoriesTextView: TextView
    private lateinit var priceTextView: TextView

    override fun getInfoContents(p0: Marker?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.restaurant_card_map, null)

        val restaurant = p0?.tag as RestaurantInfoData

        nameTextView = view.restaurant_card_name_text_view
        categoriesTextView = view.restaurant_card_categories_text_view
        priceTextView = view.restaurant_card_price_text_view

        nameTextView.text = restaurant.name
        categoriesTextView.text = restaurant.categoryString
        priceTextView.text = String.format(
            "Start at %.0f Baht.", restaurant.startPrice)

        return view
    }

    override fun getInfoWindow(p0: Marker?): View? = null
}