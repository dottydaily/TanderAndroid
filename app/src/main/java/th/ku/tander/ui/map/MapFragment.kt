package th.ku.tander.ui.map

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.*
import th.ku.tander.R
import th.ku.tander.helper.JSONParser
import th.ku.tander.helper.LocationRequester
import th.ku.tander.helper.RequestManager

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var currentLocation: LatLng? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // implement google map fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // hide map fragment at first
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.hide(mapFragment)
        fragmentTransaction.commit()

        val searchBar = view.findViewById<EditText>(R.id.search_bar)
        searchBar.setOnClickListener {
            val self = it as EditText
            self.hint = self.hint.toString() + "5"
        }

        currentLocation = LocationRequester.getLocation()

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val queue = RequestManager.getQueue()

        // request
        val url = "https://tander-webservice.herokuapp.com/restaurants/search/" +
                "?radius=2000&lat=${currentLocation?.latitude}&lon=${currentLocation?.longitude}"
        println(url)
        val restaurantRequest = JsonArrayRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                println("========== Got response! ==========")

                // parse all restaurants into arraylist
                val restaurants = JSONParser.fromJSONArraytoRestaurantArray(response.toString())

                restaurants.forEach {
                    // Add a marker of each restaurant
                    mMap.addMarker(MarkerOptions().position(it.position).title(it.name))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16.0f))
                }

                // showing map fragment and remove spinner
                val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
                val fragmentTransaction = childFragmentManager.beginTransaction()
                fragmentTransaction.show(mapFragment)
                fragmentTransaction.commit()

                loadingSpinner.visibility = View.GONE
                search_bar.visibility = View.VISIBLE
                Toast.makeText(context, "Found: ${response.length()} restaurants", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                println(error.message)
            }
        )
        restaurantRequest.setRetryPolicy(DefaultRetryPolicy(
            5000, 3, 1.0F
        ))

        queue.add(restaurantRequest)
        queue.start()
    }

    ///////////////////
    // helper method //
    ///////////////////

    fun hideKeyboard(context: Context, editText: EditText) {
        val imm = context as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }
}
