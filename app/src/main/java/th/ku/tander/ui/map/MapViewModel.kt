package th.ku.tander.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject
import th.ku.tander.helper.RequestManager

class MapViewModel: ViewModel() {

    var currentLocation = MutableLiveData<LatLng>().apply { value = null }

    var restaurantJsonString: String? = null; private set
    var totalRestaurant = 0; private set
    private val restaurantMap = MutableLiveData<HashMap<String, JSONObject>>().apply { value = null }

    fun getRestaurantById(id: String): JSONObject? = restaurantMap.value?.get(id)
    fun getRestaurantMap(): LiveData<HashMap<String, JSONObject>> = restaurantMap

    fun fetchRestaurants() {
        // request
        val url = "https://tander-webservice.an.r.appspot.com/restaurants/search/" +
                "?lat=${currentLocation.value?.latitude}&lon=${currentLocation.value?.longitude}"

        RequestManager.getJsonArrayRequestWithToken(url,
            Response.Listener { response ->
                println("========== Got response! ==========")

                applyRestaurantJson(response)
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
            }, 3000, 3, 2f)
    }

    fun applyRestaurantJson(response: JSONArray) {
        restaurantJsonString = response.toString()
        totalRestaurant = response.length()

        val saving = HashMap<String, JSONObject>()

        for (i in 0 until response.length()) {
            val restaurant = response.getJSONObject(i)
            val id = restaurant.getString("_id")

            saving[id] = restaurant
        }

        restaurantMap.value = saving
    }
}