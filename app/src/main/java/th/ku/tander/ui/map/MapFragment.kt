package th.ku.tander.ui.map

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.google.android.gms.dynamic.SupportFragmentWrapper
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.*
import th.ku.tander.R
import th.ku.tander.helper.JSONParser
import th.ku.tander.helper.LocationRequester
import th.ku.tander.helper.RequestManager
import th.ku.tander.model.Restaurant
import th.ku.tander.ui.search.SearchActivity

class MapFragment : Fragment(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var currentLocation: LatLng? = null
    private lateinit var mapFragment: SupportMapFragment
    private var restaurants: ArrayList<Restaurant> = ArrayList()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        println("On CreateView")

        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val searchBar = view.findViewById<EditText>(R.id.search_bar)
        searchBar.setOnClickListener {
            println("Launching Search Activity")
            val intent = Intent(requireContext(), SearchActivity::class.java)
            intent.putExtra("EXTRA_LOCATION", currentLocation!!)

            startActivity(intent)
            val activity = requireContext() as Activity
//            activity.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right)
        }

        // implement google map fragment
        mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.retainInstance = true
        mapFragment.getMapAsync(this)

        // hide map fragment if don't have restaurant yet
        if (restaurants.isEmpty()) {
            val fragmentTransaction = childFragmentManager.beginTransaction()
            fragmentTransaction.hide(mapFragment)
            fragmentTransaction.commit()
        }

        currentLocation = LocationRequester.getLocation()

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (restaurants.isEmpty()) {
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
                    restaurants = JSONParser.fromJSONArraytoRestaurantArray(response.toString())

                    setDataOnMap()
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
        } else {
            Handler().postDelayed( {
                setDataOnMap()
            }, 3000)
        }
    }

    override fun onPause() {
        super.onPause()

        println("On Pause")

        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.hide(mapFragment)
        fragmentTransaction.commit()
        loadingSpinner.visibility = View.VISIBLE
        search_bar.visibility = View.INVISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        mapFragment.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()

        println("On Resume")

        if (mMap != null) {
            println("Reloading map fragment ${restaurants.size}")
        }
    }

    ///////////////////
    // helper method //
    ///////////////////

    fun setDataOnMap() {
        restaurants.forEach {
            // Add a marker of each restaurant
            mMap?.addMarker(MarkerOptions().position(it.position).title(it.name))
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16.0f))
        }

        // showing map fragment and remove spinner
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.show(mapFragment)
        fragmentTransaction.commit()

        loadingSpinner.visibility = View.INVISIBLE
        search_bar.visibility = View.VISIBLE

        Toast.makeText(context, "Found: ${restaurants.size} restaurants", Toast.LENGTH_SHORT).show()
    }
    fun hideKeyboard(context: Context, editText: EditText) {
        val imm = context as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }
}
