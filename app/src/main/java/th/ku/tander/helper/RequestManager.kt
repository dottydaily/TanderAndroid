package th.ku.tander.helper

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.Volley

object RequestManager {
    private lateinit var requestQueue: RequestQueue

    init {
        println("Instantiate Request Manager(Singleton)")
    }

    fun start(context: Context) {
        requestQueue = Volley.newRequestQueue(context)
    }

    fun getQueue(): RequestQueue {
        return requestQueue
    }
}