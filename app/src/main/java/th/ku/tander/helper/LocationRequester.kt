package th.ku.tander.helper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng

object LocationRequester {
    private lateinit var mFuseLocationClient: FusedLocationProviderClient
    private var currentLocation: MutableLiveData<LatLng> = MutableLiveData()
    private lateinit var context: Context

    init {
        println("Instantiate Location Manager(Singleton)")
    }

    fun start(context: Context) {
        this.context = context
        // location requesting
        mFuseLocationClient = LocationServices.getFusedLocationProviderClient(context)
        getCurrentLocation()
    }

    fun getLiveLocation(): LiveData<LatLng> {
        return currentLocation
    }

    fun getLocation(): LatLng? {
        return currentLocation.value
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
                Toast.makeText(context, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            }
        } else { // request permission from user
            requestNeededPermissions()
        }

        println("DONE GET CURRENT LOCATION")
    }

    // check if this device has already grant permission or not
    private fun hasPermissionGranted(): Boolean {
        val isCoarseLocationGranted = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val isFineLocationGranted = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        return isCoarseLocationGranted && isFineLocationGranted
    }

    // trying to grant permission
    private fun requestNeededPermissions() {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
            2)
    }

    // check if this device has already enable location or not
    private fun isLocationEnabled(): Boolean {
        val locationManager= context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

//    // handle after permissions had granted
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == 2) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                val toast = Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT)
//                toast.show()
//            }
//        }
//    }

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
        mFuseLocationClient = LocationServices.getFusedLocationProviderClient(context)
        mFuseLocationClient.requestLocationUpdates(
            requestOption, handleData, Looper.myLooper()).continueWith {
        }

        println("DONE REQUESTING NEW LOCATION DATA")
    }
}