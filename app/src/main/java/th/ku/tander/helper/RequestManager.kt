package th.ku.tander.helper

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.*

object RequestManager {
    @Volatile
    private var requestQueue: RequestQueue? = null

    init {
        println("Instantiate Request Manager(Singleton)")
    }

    fun start(context: Context) {
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(context)
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
}