package th.ku.tander.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.*
import th.ku.tander.R
import th.ku.tander.helper.RequestManager

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val queue = RequestManager.getQueue()

        val url = "https://tander-webservice.herokuapp.com/restaurants"
        val restaurantRequest = JsonArrayRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                println(response.toString())
                Toast.makeText(context, response.length().toString(), Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                println(error.message)
            }
        )

        queue.add(restaurantRequest)
        queue.start()

        // Add a marker in Sydney and move the camera
        val kaset = LatLng(13.8476, 100.5696)
        mMap.addMarker(MarkerOptions().position(kaset).title("Here I am"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kaset, 16.0f))
    }

}
