package th.ku.tander.helper

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.google.android.gms.maps.model.LatLng
import th.ku.tander.model.Restaurant
import java.io.StringReader

object JSONParser {
    private var klaxon: Klaxon = Klaxon()

    init {
        println("Starting JSONParser")
    }

    fun fromJSONtoRestaurant(jsonString: String): Restaurant {
        val restaurant = klaxon.parse<Restaurant>(jsonString) as Restaurant
        val jsonObject = klaxon.parseJsonObject(StringReader(jsonString))
        val position = jsonObject["position"] as JsonObject
        val latitude = position["lat"] as Double
        val longitude = position["lon"] as Double
        restaurant.position = LatLng(latitude, longitude)

        return restaurant
    }

    fun fromJSONArraytoRestaurantArray(jsonString: String): ArrayList<Restaurant> {
        val jsonArray = klaxon.parseJsonArray(StringReader(jsonString))
        val restaurants = ArrayList<Restaurant>()

        for (member in jsonArray) {
            val jsonObject = member as JsonObject
            restaurants.add(fromJSONtoRestaurant(jsonObject.toJsonString()))
        }

        return restaurants
    }
}