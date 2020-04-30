package th.ku.tander.ui.restaurant

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.ImageRequest
import th.ku.tander.helper.RequestManager
import th.ku.tander.model.Restaurant

class RestaurantViewModel: ViewModel() {
    var restaurant = MutableLiveData<Restaurant>().apply { value = null }
    var image = MutableLiveData<Bitmap>().apply { value = null }; private set

    // get images from server
    fun fetchRestaurantImage() {
        var statusCode: Int? = null

        var url = "https:/tander-webservice.an.r.appspot.com/images/restaurants/"
        restaurant.value?.apply { url += restaurantId }

        val imageRequest = object: ImageRequest(url,
            Response.Listener {
                image.apply { value = it }
            }, 0, 0, ImageView.ScaleType.FIT_CENTER, Bitmap.Config.RGB_565,
            Response.ErrorListener { error ->
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
}