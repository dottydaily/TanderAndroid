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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.*
import org.json.JSONArray
import org.json.JSONObject
import th.ku.tander.R
import th.ku.tander.helper.LocationRequester
import th.ku.tander.helper.RequestManager
import th.ku.tander.model.Restaurant
import th.ku.tander.model.RestaurantInfoData
import th.ku.tander.ui.restaurant.RestaurantActivity
import th.ku.tander.ui.search.SearchActivity
import th.ku.tander.ui.view_class.RestaurantCardInfoGoogleMap

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
        mapFragment.retainInstance
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

        // observe location, then do fetch restaurant
        mapViewModel.currentLocation.observe(viewLifecycleOwner, Observer {
            if (it != null) mapViewModel.fetchRestaurants()
        })

        // observe if done loading, then set data on map
        mapViewModel.getRestaurantMap().observe(viewLifecycleOwner, Observer {
            if (it != null) setDataOnMap(it)
        })

        return view
    }

    // Google Map : Run when complete created map fragment completed
    override fun onMapReady(googleMap: GoogleMap) {
        println("========== MAP READY: MAP FRAGMENT ==========")
        mMap = googleMap
        mMap!!.setInfoWindowAdapter(RestaurantCardInfoGoogleMap(this.requireContext()))

        mMap?.setOnInfoWindowClickListener {
            Toast.makeText(this.requireContext(), "Long pressed for more details", Toast.LENGTH_SHORT).show()
        }

        mMap?.setOnInfoWindowLongClickListener {
            val restaurant = it.tag as RestaurantInfoData
            val intent = Intent(this.requireContext(), RestaurantActivity::class.java)
            intent.putExtra("restaurantJson",
                mapViewModel.getRestaurantById(restaurant.restaurantId).toString())
            this.requireContext().startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()

        println("========== START: MAP FRAGMENT ==========")
    }

    override fun onResume() {
        super.onResume()

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

        childFragmentManager.putFragment(outState, "mapFragment", mapFragment)
//        mapFragment.onSaveInstanceState(outState)
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
            val json = it.value
            val position = json.getJSONObject("position")
            val categories = json.getJSONArray("categories")
            val categoryList = arrayListOf<String>()
            for (i in 0 until categories.length()) {
                categoryList.add(categories.getString(i))
            }
            val restaurantInfo = RestaurantInfoData(
                restaurantId = json.getString("_id"),
                name = json.getString("name"),
                categories = categoryList,
                startPrice = json.getDouble("startPrice"),
                lat = position.getDouble("lat"),
                lon = position.getDouble("lon")
            )
            val markerOptions = MarkerOptions()

            restaurantInfo.apply {
                markerOptions.position(LatLng(lat, lon))
            }

            // Add a marker of each restaurant
            val m = mMap?.addMarker(markerOptions)
            m?.tag = restaurantInfo
            m?.showInfoWindow()
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
