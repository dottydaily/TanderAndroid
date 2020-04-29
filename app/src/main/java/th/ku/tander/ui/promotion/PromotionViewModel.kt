package th.ku.tander.ui.promotion

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.*
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import org.json.JSONArray
import org.json.JSONObject
import th.ku.tander.helper.KeyStoreManager
import th.ku.tander.helper.RequestManager
import th.ku.tander.model.Restaurant

class PromotionViewModel(private val context: Context) : ViewModel() {

    private var status = MutableLiveData<Boolean>().apply { value = false }
    private var loadImageCount = 0
    private var promotionDetailArray = MutableLiveData<JSONArray>().apply { value = JSONArray() }
    private var imageMap: HashMap<String, Bitmap> = HashMap()
    private var restaurantNameMap: HashMap<String, String> = HashMap()
    private var restaurantIds: HashSet<String> = HashSet()

    fun getPromotionDetail(): LiveData<JSONArray> = promotionDetailArray
    fun getImageMap(): HashMap<String, Bitmap> = imageMap
    fun getStatus(): LiveData<Boolean> = status
    fun getRestaurantNameById(id: String): String? = restaurantNameMap[id]

    fun isDoneLoading(): Boolean{
        return loadImageCount == promotionDetailArray.value!!.length()
    }

    // fetch promotion detail
    fun fetchPromotion() {
        clearData()

        val url = "https://tander-webservice.an.r.appspot.com/promotions"
        println(url)

        RequestManager.getJsonArrayRequestWithToken(url,
            Response.Listener { response ->
                for (i in 0 until response.length()) {
                    val promotion = response.getJSONObject(i)
                    val restaurants = promotion.getJSONArray("restaurantApply")

                    for (j in 0 until restaurants.length()) {
                        restaurantIds.add(restaurants.getString(j))
                    }
                }

                promotionDetailArray.value = response

                println("DONE FETCHING PROMOTION : ${response.length()} promotions")
                println("Starting get restaurant data...")
                fetchRestaurant()
            },
            Response.ErrorListener { error ->
            }, 3000, 3, 2f
        )
    }

    // get all restaurants detail
    private fun fetchRestaurant() {
        val url = "https://tander-webservice.an.r.appspot.com/restaurants/id/getByIds"
        println(url)

        RequestManager.postRequestWithBodyBySet(url, "restaurantIds", restaurantIds,
            Response.Listener { response ->
                val jsonResponse = JSONArray(response)

                for (i in 0 until jsonResponse.length()) {
                    val restaurant = jsonResponse.getJSONObject(i)
                    val id = restaurant.getString("_id")
                    val name = restaurant.getString("name")

                    restaurantNameMap[id] = name
                }

                println("DONE FETCHING RESTAURANT : ${jsonResponse.length()} restaurants")
                println("Starting get image data...")
                fetchPromotionImage()
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
            }, 3000, 3, 2f
        )
    }

    // get images from server
    private fun fetchPromotionImage() {
        for (i in 0 until promotionDetailArray.value!!.length()) {
            val promotion = promotionDetailArray.value!!.getJSONObject(i)
            val promotionId = promotion.getString("_id")

            var statusCode: Int? = null

            val url = "https:/tander-webservice.an.r.appspot.com/images/promotions/$promotionId"
            val imageRequest = object: ImageRequest(url,
                Response.Listener {
                    println("$loadImageCount/${promotionDetailArray.value!!.length()} : Got image of $promotionId")
                    imageMap.put(promotionId, it)
                    println("Now have ${imageMap.size} of ${promotionDetailArray.value!!.length()}")
                    status.value = isDoneLoading()
                }, 0, 0, ImageView.ScaleType.FIT_CENTER, Bitmap.Config.RGB_565,
                Response.ErrorListener {
                    println("$loadImageCount/${promotionDetailArray.value!!.length()} : Didn't get image of $promotionId")
                    println("Now have ${imageMap.size} of ${promotionDetailArray.value!!.length()}")
                    status.value = isDoneLoading()
                }
            ) {
                override fun parseNetworkResponse(response: NetworkResponse?): Response<Bitmap> {
                    statusCode = response?.statusCode
                    loadImageCount++
                    println("$loadImageCount -> RESPONSE : STATUS CODE: $statusCode of $promotionId")
                    return super.parseNetworkResponse(response)
                }
                override fun parseNetworkError(volleyError: VolleyError?): VolleyError {
                    statusCode = volleyError?.networkResponse?.statusCode
                    loadImageCount++
                    println("$loadImageCount -> ERROR : STATUS CODE: $statusCode of $promotionId")
                    return super.parseNetworkError(volleyError)
                }
            }

            imageRequest.setRetryPolicy(
                DefaultRetryPolicy(3000, 3, 2f)
            )
            RequestManager.add(imageRequest)
        }
    }

    private fun clearData() {
        status.value = false
        loadImageCount = 0
        promotionDetailArray.value = JSONArray()
        imageMap = HashMap()
        restaurantNameMap = HashMap()
        restaurantIds = HashSet()
    }
}