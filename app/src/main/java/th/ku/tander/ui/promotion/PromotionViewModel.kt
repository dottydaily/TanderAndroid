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
import th.ku.tander.helper.RequestManager
import th.ku.tander.model.Restaurant

class PromotionViewModel(private val context: Context) : ViewModel() {

    private var token: String?
    private var loadImageCount = MutableLiveData<Int>().apply { value = 0 }
    private var promotionDetailArray = MutableLiveData<JSONArray>().apply { value = JSONArray() }
    private var imageMap: HashMap<String, Bitmap> = HashMap()
    private var rstaurantNameMap: HashMap<String, String> = HashMap()
    private var restaurantIds: HashSet<String> = HashSet()

    init {
        val sp = context.getSharedPreferences("TANDER", Context.MODE_PRIVATE)
        token = sp.getString("TOKEN", null)
    }

    fun getPromotionDetail(): LiveData<JSONArray> = promotionDetailArray
    fun getImageMap(): HashMap<String, Bitmap> = imageMap
    fun getLoadImageCount(): LiveData<Int> = loadImageCount
    fun getRestaurantNameById(id: String): String? = rstaurantNameMap?.get(id)

    fun isDoneLoading(): Boolean{
        println("$loadImageCount == ${promotionDetailArray.value!!.length()} ?")
        return loadImageCount.value == promotionDetailArray.value!!.length()
    }

    // fetch promotion detail
    fun fetchPromotion() {
        val url = "https://tander-webservice.an.r.appspot.com/promotions"
        println(url)

        val promotionRequest = object: JsonArrayRequest(Request.Method.GET, url, null,
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
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                if (token.isNullOrBlank()) {
                    return super.getHeaders()
                } else {
                    val headers = HashMap<String, String>()
                    headers.put("Authorization", "Bearer ${token!!}")
                    return headers
                }
            }
        }

        promotionRequest.setRetryPolicy(
            DefaultRetryPolicy (3000, 3, 2f))

        RequestManager.add(promotionRequest)
    }

    // get all restaurants detail
    private fun fetchRestaurant() {
        val url = "https://tander-webservice.an.r.appspot.com/restaurants/id/getByIds"
        println(url)

        val body = HashMap<String, Array<String>>()
        body["restaurantIds"] = restaurantIds.toTypedArray()
        val bodyJson = JSONObject(body as Map<*, *>)

        val restaurantsRequest = object: StringRequest(Request.Method.POST, url,
            Response.Listener { response ->
                val jsonResponse = JSONArray(response)

                for (i in 0 until jsonResponse.length()) {
                    val restaurant = jsonResponse.getJSONObject(i)
                    val id = restaurant.getString("_id")
                    val name = restaurant.getString("name")

                    rstaurantNameMap[id] = name
                }

                println("DONE FETCHING RESTAURANT : ${jsonResponse.length()} restaurants")
                println("Starting get image data...")
                fetchPromotionImage()
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val sp = context.getSharedPreferences("TANDER", Context.MODE_PRIVATE)
                val token = sp.getString("TOKEN", null)

                if (token != null) {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer ${token}"
                    return headers
                } else {
                    return super.getHeaders()
                }
            }

            // custom request to make it can send json but get response with string
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
            override fun getBody(): ByteArray {
                return bodyJson.toString().toByteArray()
            }
        }

        restaurantsRequest.setRetryPolicy(
            DefaultRetryPolicy(3000, 3, 2f)
        )

        RequestManager.add(restaurantsRequest)
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
                    loadImageCount.value = loadImageCount.value!!+1

                    println("$loadImageCount/${promotionDetailArray.value!!.length()} : Got image of $promotionId")
                    println(it)
                    println("Now have ${imageMap.size} of ${promotionDetailArray.value!!.length()}")
                    imageMap.put(promotionId, it)
                }, 0, 0, ImageView.ScaleType.FIT_CENTER, Bitmap.Config.RGB_565,
                Response.ErrorListener {
                    loadImageCount.value = loadImageCount.value!!+1

                    println("$loadImageCount/${promotionDetailArray.value!!.length()} : Didn't get image of $promotionId")
                    println("Now have ${imageMap.size} of ${promotionDetailArray.value!!.length()}")
                }
            ) {
                override fun parseNetworkResponse(response: NetworkResponse?): Response<Bitmap> {
                    statusCode = response?.statusCode
                    println("RESPONSE : STATUS CODE: $statusCode of $promotionId")
                    return super.parseNetworkResponse(response)
                }
                override fun parseNetworkError(volleyError: VolleyError?): VolleyError {
                    statusCode = volleyError?.networkResponse?.statusCode
                    println("ERROR : STATUS CODE: $statusCode of $promotionId")
                    return super.parseNetworkError(volleyError)
                }
            }

            imageRequest.setRetryPolicy(
                DefaultRetryPolicy(3000, 3, 2f)
            )
            RequestManager.add(imageRequest)
        }
    }
}