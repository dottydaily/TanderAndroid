package th.ku.tander.helper

import android.content.Context
import com.android.volley.*
import com.android.volley.toolbox.*
import org.json.JSONArray
import org.json.JSONObject

object RequestManager {
    @Volatile private var requestQueue: RequestQueue? = null
    @Volatile var token: String? = null; private set

    init {
        println("Instantiate Request Manager(Singleton)")
    }

    fun start(context: Context) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context)
        }
        if (token.isNullOrBlank()) {
            token = KeyStoreManager.getData("TOKEN")

            println("RequestManager now has token $token")
        }
    }

    fun add(request: JsonArrayRequest) {
        requestQueue?.add(request)
    }

    fun add(request: JsonObjectRequest) {
        requestQueue?.add(request)
    }

    fun add(request: StringRequest) {
        requestQueue?.add(request)
    }

    fun add(request: ImageRequest) {
        requestQueue?.add(request)
    }

    fun verifyToken(name: String, token: String,
                    listener: Response.Listener<String>, errorListener: Response.ErrorListener) {

        val map = HashMap<String, String>()
        map["username"] = name
        map["token"] = token

        val body = JSONObject(map as Map<*, *>)
        val url = "https://tander-webservice.an.r.appspot.com/users/verify"

        postRequestWithBody(url, body,
            listener, errorListener, 3000, 3, 2f
        )
    }

    fun getJsonArrayRequestWithToken(url: String, listener: Response.Listener<JSONArray>,
                            errorListener: Response.ErrorListener,
                            retryTime: Int, retryCount: Int, retryBackoff: Float) {
        println(url)

        val jsonArrayRequest = object: JsonArrayRequest(Request.Method.GET, url, null,
            listener, errorListener
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                if (token.isNullOrBlank()) {
                    return super.getHeaders()
                } else {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer ${token!!}"
                    return headers
                }
            }
        }

        jsonArrayRequest.setRetryPolicy(
            DefaultRetryPolicy(retryTime, retryCount, retryBackoff)
        )

        requestQueue?.add(jsonArrayRequest)
    }

    fun postRequestWithBodyBySet(url: String, bodyName: String, setToBody: HashSet<String>,
                                 listener: Response.Listener<String>, errorListener: Response.ErrorListener,
                                 retryTime: Int, retryCount: Int, retryBackoff: Float) {
        println(url)

        val body = HashMap<String, Array<String>>()
        body[bodyName] = setToBody.toTypedArray()
        val bodyJson = JSONObject(body as Map<*, *>)

        postRequestWithBody(url, bodyJson, listener, errorListener, retryTime, retryCount, retryBackoff)
    }

    fun postRequestWithBody(url: String, body: JSONObject,
                                listener: Response.Listener<String>, errorListener: Response.ErrorListener,
                                retryTime: Int, retryCount: Int, retryBackoff: Float) {
        println(url)

        val stringRequest = object: StringRequest(Request.Method.POST, url, listener, errorListener) {
            override fun getHeaders(): MutableMap<String, String> {
                if (token != null) {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer $token"
                    return headers
                } else {
                    return super.getHeaders()
                }
            }

            override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {
                val statusCode = response?.statusCode
                statusCode.let { println(" >>>>>>>>>> RESPONSE STATUS CODE $it <<<<<<<<<<") }
                return super.parseNetworkResponse(response)
            }

            override fun parseNetworkError(volleyError: VolleyError?): VolleyError {
                val statusCode = volleyError?.networkResponse?.statusCode
                statusCode.let { println(" >>>>>>>>>> ERROR STATUS CODE $it <<<<<<<<<<") }
                return super.parseNetworkError(volleyError)
            }

            // custom request to make it can send json but get response with string
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
            override fun getBody(): ByteArray {
                return body.toString().toByteArray()
            }
        }

        stringRequest.setRetryPolicy(
            DefaultRetryPolicy(retryTime, retryCount, retryBackoff)
        )

        requestQueue?.add(stringRequest)
    }
}