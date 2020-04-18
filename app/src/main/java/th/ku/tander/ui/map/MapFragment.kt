package th.ku.tander.ui.map

import android.Manifest
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
import th.ku.tander.helper.RequestManager

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mFuseLocationClient: FusedLocationProviderClient
    private var currentLocation: MutableLiveData<LatLng> = MutableLiveData()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mFuseLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

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

        getCurrentLocation()

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val queue = RequestManager.getQueue()

        // observer for waiting location data
        val locationObserver = Observer<LatLng> { location ->
            if (location != null) {
                // request
                val url = "https://tander-webservice.herokuapp.com/restaurants/search/" +
                        "?radius=2000&lat=${location.latitude}&lon=${location.longitude}"
                println(url)
                val restaurantRequest = JsonArrayRequest(Request.Method.GET, url, null,
                    Response.Listener { response ->
                        println("Got response!")

                        // parse all restaurants into arraylist
                        val restaurants = JSONParser.fromJSONArraytoRestaurantArray(response.toString())
                        restaurants.forEach {
                            // Add a marker of each restaurant
                            mMap.addMarker(MarkerOptions().position(it.position).title(it.name))
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation.value, 16.0f))
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

                queue.add(restaurantRequest)
                queue.start()
            }
        }

        currentLocation.observe(viewLifecycleOwner, locationObserver)
    }

    ///////////////////
    // helper method //
    ///////////////////

    // get current location
    private fun getCurrentLocation() {
        // check permission
        if (hasPermissionGranted()) {
            println("PERMISSION GRANTED.")
            // check location setting
            if (isLocationEnabled()) {
                println("LOCATION ENABLED.")
                mFuseLocationClient.lastLocation.addOnCompleteListener { task ->
                    var location = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else { // set current location of
                        println("GETTING LOCATION DATA: ${location.latitude}, ${location.longitude}")
                        currentLocation.value = LatLng(location.latitude, location.longitude)
//                        currentLocation = LatLng(location.latitude, location.longitude)
                    }
                }
            } else { // let user enable it
                Toast.makeText(requireContext(), "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else { // request permission from user
            requestNeededPermissions()
        }

        println("DONE GET CURRENT LOCATION")
    }

    // check if this device has already grant permission or not
    private fun hasPermissionGranted(): Boolean {
        val isCoarseLocationGranted = ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val isFineLocationGranted = ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        return isCoarseLocationGranted && isFineLocationGranted
    }

    // trying to grant permission
    private fun requestNeededPermissions() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
            2
        )
    }

    // check if this device has already enable location or not
    private fun isLocationEnabled(): Boolean {
        val locationManager= requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    // handle after permissions had granted
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val toast = Toast.makeText(context, "Permission granted!", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }

    // request location data if our location record is null
    fun requestNewLocationData() {
        var requestOption = LocationRequest()
        requestOption.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        requestOption.interval = 0
        requestOption.fastestInterval = 0

        // callback for handling data after get it
        val handleData = object: LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                val lastLocation = result!!.lastLocation
                currentLocation.value = LatLng(lastLocation.latitude, lastLocation.longitude)
//                currentLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
            }
        }

        // get location service again (in case that we change its setting)
        mFuseLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        mFuseLocationClient.requestLocationUpdates(
            requestOption, handleData, Looper.myLooper()).continueWith {
        }

        println("DONE REQUESTING NEW LOCATION DATA")
    }

    fun hideKeyboard(context: Context, editText: EditText) {
        val imm = context as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }
}
