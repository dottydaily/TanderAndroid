package th.ku.tander.ui.map

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.*
import org.json.JSONArray
import org.json.JSONObject
import th.ku.tander.R
import th.ku.tander.helper.LocationRequester
import th.ku.tander.helper.RequestManager
import th.ku.tander.ui.search.SearchActivity

class MapFragment : Fragment(), OnMapReadyCallback {

    private val mapViewModel by viewModels<MapViewModel>()
    private var mMap: GoogleMap? = null
    private lateinit var mapFragment: SupportMapFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        println("========== CREATE: MAP FRAGMENT =========")

        // inflate an view
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // implement google map fragment
        mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.retainInstance = true
        mapFragment.getMapAsync(this)

        if (savedInstanceState != null) {
            println("========== RESTORE STATE: MAP FRAGMENT ==========")
            savedInstanceState.let {
                val lat = it.getDouble("lat")
                val lon = it.getDouble("lon")

                mapViewModel.currentLocation.value = LatLng(lat, lon)

                val jsonString = it.getString("restaurants")
                mapViewModel.applyRestaurantJson(JSONArray(jsonString))
            }

        } else {
            LocationRequester.getLiveLocation().observe(viewLifecycleOwner, Observer {
                if (it != null) mapViewModel.currentLocation.value = it
                else LocationRequester.getLocation()
            })
        }

        // hide map fragment if don't have restaurant yet
        if (mapViewModel.getRestaurantMap().value == null) {
            revealFragment(mapFragment, false)
        }

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        println("========== MAP READY: MAP FRAGMENT ==========")
        mMap = googleMap
    }

    override fun onStart() {
        super.onStart()

        println("========== START: MAP FRAGMENT ==========")
    }

    override fun onResume() {
        super.onResume()

        // check location, then do fetch restaurant
        mapViewModel.currentLocation.observe(viewLifecycleOwner, Observer {
            if (it != null) mapViewModel.fetchRestaurants()
        })

        // check if done loading, then set data on map
        mapViewModel.getRestaurantMap().observe(viewLifecycleOwner, Observer {
            if (it != null) setDataOnMap(it)
        })

        println("========== RESUME: MAP FRAGMENT ==========")
        handleSearchBarBehavior()
    }

    override fun onPause() {
        super.onPause()

        println("========== PAUSE : MAP FRAGMENT ==========")
    }

    override fun onStop() {
        super.onStop()

        println("========== STOP : MAP FRAGMENT ==========")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        println("========= ON SAVE INSTANCE : MAP FRAGMENT ==========")

        outState.run {
            putDouble("lat", mapViewModel.currentLocation.value!!.latitude)
            putDouble("lon", mapViewModel.currentLocation.value!!.longitude)
            putString("restaurants", mapViewModel.restaurantJsonString)
        }
        mapFragment.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    ///////////////////
    // helper method //
    ///////////////////

    private fun setDataOnMap(restaurantMap: HashMap<String, JSONObject>) {

        // showing map fragment and remove spinner
        revealFragment(mapFragment, true)
        loading_spinner_map.visibility = View.INVISIBLE
        search_bar.visibility = View.VISIBLE

        restaurantMap.map {
            val restaurant = it.value

            val position = restaurant.getJSONObject("position")
            val lat = position.getDouble("lat")
            val lon = position.getDouble("lon")
            val title = restaurant.getString("name")

            // Add a marker of each restaurant
            mMap?.addMarker(MarkerOptions().position(LatLng(lat, lon)).title(title))
        }

        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(mapViewModel.currentLocation.value, 16.0f))
        Toast.makeText(context,
            "Found: ${mapViewModel.totalRestaurant} restaurants",
            Toast.LENGTH_SHORT).show()
    }

    // show/hide mapfragment
    private fun revealFragment(fragment: SupportMapFragment, isShow: Boolean) {
        val fragmentTransaction = childFragmentManager.beginTransaction()

        if (isShow) {
            fragmentTransaction.show(fragment)
        } else {
            fragmentTransaction.hide(fragment)
        }

        fragmentTransaction.commit()
    }

    private fun handleSearchBarBehavior() {
        this.search_bar.setOnClickListener {
            println("Launching Search Activity")
            val intent = Intent(requireContext(), SearchActivity::class.java)

            startActivity(intent)
        }
    }
}
