package th.ku.tander.ui.restaurant

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.ImageRequest
import th.ku.tander.R
import th.ku.tander.helper.KeyStoreManager
import th.ku.tander.helper.RequestManager
import th.ku.tander.model.Restaurant

class RestaurantViewModel: ViewModel() {
    var restaurant = MutableLiveData<Restaurant>().apply { value = null }
    var image = MutableLiveData<Bitmap>().apply { value = null }; private set
    var isDoneLoaded = MutableLiveData<Boolean>().apply { value = false }; private set
    var didCreateAnotherBefore = MutableLiveData<Boolean>().apply { value = null }
    private var username: String = KeyStoreManager.getData("USER")!!

    // get images from server
    fun fetchRestaurantImage() {
        var statusCode: Int? = null

        var url = "https:/tander-webservice.an.r.appspot.com/images/restaurants/"
        restaurant.value?.apply { url += restaurantId }

        val imageRequest = object: ImageRequest(url,
            Response.Listener {
                image.apply { value = it }
                isDoneLoaded.apply { value = true }
            }, 0, 0, ImageView.ScaleType.FIT_CENTER, Bitmap.Config.RGB_565,
            Response.ErrorListener { error ->
                isDoneLoaded.apply { value = true }
                error.printStackTrace()
            }
        ) {
            override fun parseNetworkResponse(response: NetworkResponse?): Response<Bitmap> {
                statusCode = response?.statusCode
                println("RESPONSE : STATUS CODE: $statusCode")
                return super.parseNetworkResponse(response)
            }
            override fun parseNetworkError(volleyError: VolleyError?): VolleyError {
                statusCode = volleyError?.networkResponse?.statusCode
                println("ERROR : STATUS CODE: $statusCode")
                return super.parseNetworkError(volleyError)
            }
        }

        imageRequest.setRetryPolicy(
            DefaultRetryPolicy(3000, 3, 2f)
        )
        RequestManager.add(imageRequest)
    }

    fun checkIfAlreadyCreateLobby() {
        // clear check status
        didCreateAnotherBefore.apply { value = null }

        val url = "https://tander-webservice.an.r.appspot.com/lobbies/users/$username"
        println("Checking if already create another lobby...")

        RequestManager.getJsonArrayRequestWithToken(url,
            Response.Listener { response ->
                didCreateAnotherBefore.apply { value = (response.length() != 0) }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
            }, 3000, 3, 2f
        )
    }
}