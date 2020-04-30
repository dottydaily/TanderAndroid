package th.ku.tander.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import th.ku.tander.helper.LocationRequester
import th.ku.tander.helper.RequestManager
import java.net.URLEncoder

class SearchViewModel: ViewModel() {
    var startPrice: Int? = null
    var categorySpec: String? = null
    var keyword: String? = null
    var currentLocation = MutableLiveData<LatLng>().apply { value = null }; private set
    var restaurants = MutableLiveData<JSONArray>().apply { value = null }; private set

    init {
        currentLocation.apply { value = LocationRequester.getLocation() }
    }

    fun requestRestaurantBySearch() {
        clearData()

        val encodedKeyword = URLEncoder.encode(keyword, "utf-8")
        // request
        var url = "https://tander-webservice.an.r.appspot.com/restaurants/search/${encodedKeyword}" +
                "?lat=${LocationRequester.getLocation()?.latitude}" +
                "&lon=${LocationRequester.getLocation()?.longitude}"
        if (startPrice != null) {
            url += "&startPrice=${startPrice}"
        }
        if (categorySpec != null) {
            val encodedCategory = URLEncoder.encode(categorySpec, "utf-8")
            url += "&categorySpec=${encodedCategory}"
        }

        RequestManager.getJsonArrayRequestWithToken( url,
            Response.Listener { response ->
                println("========== Got response! ==========")

                restaurants.apply { value = response }
            },
            Response.ErrorListener { error ->
                println(error.message)
            }, 3000, 3, 2f
        )
    }

    private fun clearData() {
        restaurants.apply { value = null }
    }
}