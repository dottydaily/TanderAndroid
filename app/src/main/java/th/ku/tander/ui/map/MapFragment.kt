package th.ku.tander.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.beust.klaxon.Klaxon
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.*
import th.ku.tander.R
import th.ku.tander.helper.JSONParser
import th.ku.tander.helper.RequestManager
import th.ku.tander.model.Restaurant

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

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

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val queue = RequestManager.getQueue()

        // request
        val url = "https://tander-webservice.herokuapp.com/restaurants/search/shabu?radius=2000&lat=13.9888&lon=100.6178"
        val restaurantRequest = JsonArrayRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                // parse all restaurants into arraylist
                val restaurants = JSONParser.fromJSONArraytoRestaurantArray(response.toString())
                restaurants.forEach {
                    // Add a marker of each restaurant
                    val here = LatLng(13.9888, 100.6178)
                    mMap.addMarker(MarkerOptions().position(it.position).title(it.name))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here, 16.0f))
                }

                // showing map fragment and remove spinner
                val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
                val fragmentTransaction = childFragmentManager.beginTransaction()
                fragmentTransaction.show(mapFragment)
                fragmentTransaction.commit()

                loadingSpinner.visibility = View.GONE
                Toast.makeText(context, "Found: ${response.length()} restaurants", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                println(error.message)
            }
        )

        queue.add(restaurantRequest)
        queue.start()
    }

}
