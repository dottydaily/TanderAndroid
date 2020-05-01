package th.ku.tander.ui.profile

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.*
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonRequest
import org.json.JSONObject
import th.ku.tander.helper.KeyStoreManager
import th.ku.tander.helper.RequestManager

class ProfileViewModel : ViewModel() {

    val username = KeyStoreManager.getData("USER")
    val userJson = MutableLiveData<JSONObject>().apply { value = null }
    val profileImage = MutableLiveData<Bitmap>().apply { value = null }
    val isDoneLoaded = MutableLiveData<Boolean>().apply { value = false }

    fun fetchUserData() {
        val url = "https://tander-webservice.an.r.appspot.com/users/$username"

        RequestManager.getJsonObjectRequestWithToken(url,
            Response.Listener {
                userJson.value = it
                println(it.toString(2))
                fetchUserImage()
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
            }, 3000, 3, 2f
        )
    }

    // get images from server
    fun fetchUserImage() {
        var statusCode: Int? = null

        var url = "https://tander-webservice.an.r.appspot.com/images/profiles/$username"
        println(RequestManager.token)

        val imageRequest = object: ImageRequest(url,
            Response.Listener {
                profileImage.apply { value = it }
                isDoneLoaded.apply { value = true }
            }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565,
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

            override fun getHeaders(): MutableMap<String, String> {
                if (RequestManager.token.isNullOrBlank()) {
                    return super.getHeaders()
                } else {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer ${RequestManager.token!!}"
                    return headers
                }
            }
        }

        imageRequest.setRetryPolicy(
            DefaultRetryPolicy(3000, 3, 2f)
        )
        RequestManager.add(imageRequest)
    }
}