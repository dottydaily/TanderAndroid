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
import th.ku.tander.R
import th.ku.tander.helper.LocationRequester
import th.ku.tander.helper.RequestManager
import th.ku.tander.ui.search.SearchActivity

class MapFragment : Fragment(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var currentLocation: LatLng? = null
    private lateinit var mapFragment: SupportMapFragment
    private var restaurants: JSONArray? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        println("On CreateView")

        // inflate an view
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // handle searchbar action
//        val searchBar = view.findViewById<EditText>(R.id.search_bar)
//        handleSearchBarBehavior()

        // implement google map fragment
        mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.retainInstance = true
        mapFragment.getMapAsync(this)

        // hide map fragment if don't have restaurant yet
        if (restaurants == null) {
            revealFragment(mapFragment, false)
        }

        currentLocation = LocationRequester.getLocation()

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        handleSearchBarBehavior()

        if (restaurants == null) {
            mMap = googleMap

            // request
            val url = "https://tander-webservice.an.r.appspot.com/restaurants/search/" +
                    "?lat=${currentLocation?.latitude}&lon=${currentLocation?.longitude}"
            println(url)
            val restaurantRequest = JsonArrayRequest(
                Request.Method.GET, url, null,
                Response.Listener { response ->
                    println("========== Got response! ==========")

                    // parse all restaurants into arraylist
//                    restaurants = JSONParser.fromJSONArraytoRestaurantArray(response.toString())

                    setDataOnMap(response)
                },
                Response.ErrorListener { error ->
                    println(error.message)
                }
            )
            restaurantRequest.setRetryPolicy(
                DefaultRetryPolicy(
                5000, 3, 1.0F
            )
            )

            RequestManager.add(restaurantRequest)
//            RequestManager.startRequest()
        } else {
            Handler().postDelayed( {
                setDataOnMap(restaurants!!)
            }, 3000)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapFragment.onSaveInstanceState(outState)
    }

//    override fun onResume() {
//        super.onResume()
//
//        println("On Resume")
//
//        if (mMap != null) {
//            println("Reloading map fragment ${restaurants?.length()}")
//            search_bar.visibility = View.VISIBLE
//        }
//    }

    override fun onPause() {
        super.onPause()

        println("========== PAUSE : MAP FRAGMENT ==========")
    }

    ///////////////////
    // helper method //
    ///////////////////

    private fun setDataOnMap(jsonArray: JSONArray) {
        // save the response result
        restaurants = jsonArray

        // showing map fragment and remove spinner
        revealFragment(mapFragment, true)
        loading_progessbar.visibility = View.INVISIBLE
        search_bar.visibility = View.VISIBLE

//        restaurants.forEach {
        for (i in 0 until jsonArray.length()) {
            val restaurant = jsonArray.getJSONObject(i)
            val position = restaurant.getJSONObject("position")
            val lat = position.getDouble("lat")
            val lon = position.getDouble("lon")
            val title = restaurant.getString("name")

            // Add a marker of each restaurant
            mMap?.addMarker(MarkerOptions().position(LatLng(lat, lon)).title(title))
        }

        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16.0f))
        Toast.makeText(context, "Found: ${restaurants?.length()} restaurants", Toast.LENGTH_SHORT).show()
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

    private fun hideKeyboard(context: Context, editText: EditText) {
        val imm = context as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    private fun handleSearchBarBehavior() {
        search_bar.setOnClickListener {
            println("Launching Search Activity")
            val intent = Intent(requireContext(), SearchActivity::class.java)
            intent.putExtra("EXTRA_LOCATION", currentLocation!!)

            startActivity(intent)
        }
    }
}
