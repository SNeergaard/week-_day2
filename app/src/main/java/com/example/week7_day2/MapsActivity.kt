package com.example.week7_day2

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import android.widget.Toast
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ScaleDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.tasks.OnSuccessListener
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsManager.IPermissionManager {
    private lateinit var mMap: GoogleMap
    private lateinit var manager: PermissionsManager
    private lateinit var locationProvider: FusedLocationProviderClient
    private lateinit var currentRoute: DirectionsRoute
    private lateinit var navigationMapRoute: NavigationMapRoute
    lateinit var geofencingClient: GeofencingClient

    private val TAG = "geo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        manager = PermissionsManager(this, this)
        manager.checkForPermissions()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        manager.permissionResult(requestCode, permissions, grantResults)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.setMinZoomPreference(18.0f)
        createGeofence()
        val sydney = LatLng(-34.0, 151.0)
        displayLocationOnMap(sydney, "somewhere")
    }

    private fun displayLocationOnMap(latLng: LatLng, title: String) {
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    fun getLocationByAddress(address: String): LatLng {
        val geocoder = Geocoder(this)
        val addressResult = geocoder.getFromLocationName(address, 1)[0]
        return LatLng(addressResult.latitude, addressResult.longitude)
    }

    fun FindAddress(view: View?) {
        val addressEntered = etAddress.text.toString()
        val retrivedLatLng: LatLng
        if (!addressEntered.isEmpty()) {
            retrivedLatLng = getLocationByAddress(addressEntered)
            displayLocationOnMap(retrivedLatLng, addressEntered)
        }
    }

    fun ChooseMapType(view: View) {
        val mapTypeDialog: AlertDialog
        val types = arrayOf(" Normal ", " Sattellite ", " Hybrid ", " Terrain ")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Map Type")
        builder.setSingleChoiceItems(types, -1,
            DialogInterface.OnClickListener { dialog, item ->
                when (item) {
                    0 ->
                        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                    1 ->
                        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    2 ->
                        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                    3 ->
                        mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                }
                dialog.dismiss()
            })
        mapTypeDialog = builder.create()
        mapTypeDialog.show()

    }

    fun GetDirections(view: View) {
        val dialog = Dialog(view.context)
        dialog.setContentView(R.layout.get_location_dialog)
        dialog.setTitle("Get Directions")

        val etCustom = dialog.findViewById(R.id.etDestination) as TextView

        val btnCustom = dialog.findViewById(R.id.btnDestination) as Button
        btnCustom.setOnClickListener {
            val destination = etCustom.text.toString()
            getRoute(destination)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun getLocationRequest(numOfRequest: Int): LocationRequest {
        val request = LocationRequest()
        request.setMaxWaitTime(5000)
        request.setInterval(3000)
        request.setNumUpdates(numOfRequest)
        return request
    }

    override fun onPermissionResult(isGranted: Boolean) {
        Toast.makeText(
            this,
            if (isGranted) "Permission Granted" else "Permission Denied",
            Toast.LENGTH_LONG
        ).show()
        if (isGranted) {
            locationProvider = FusedLocationProviderClient(this)
            getLastKnowLocation()
            getLocationUpdates(100)
        }
    }

    private fun getLastKnowLocation() {
        locationProvider.getLastLocation()
            .addOnSuccessListener(OnSuccessListener<Location> { location ->
                //LatLng returnLatLng = new LatLng((0,0);
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude
                    displayLocationOnMap(LatLng(lat, lng), "Last Known Location")

                } else {
                    Log.d("TAG", "onSuccess: LOCATION IS NULL")
                    getLocationUpdates(1)
                    getLastKnowLocation()
                }
            })
    }

    private fun getLocationUpdates(numOfUpdates: Int) {
        val locationRequest = getLocationRequest(numOfUpdates)
        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        val settingsClient = SettingsClient(this)
        settingsClient.checkLocationSettings(settingsRequest)
        locationProvider.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.getLocations().get(0)
                val lat = location.latitude
                val lbg = location.longitude
                Log.d("TAG", "onLocationResult")
            }

        }, Looper.myLooper())

    }

    private fun getRoute(destination: String) {
        var latLng = getLocationByAddress(destination)
        var uri = Uri.parse("google.navigation:q=" + latLng.latitude + "," + latLng.longitude)
        var intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }

    private fun createGeofence(){
        val latitude = 33.949680
        val longitude = -84.412160
        val radius = 100f
        val duration : Long = 60000
var geofence = Geofence.Builder()
    .setRequestId(TAG)
    .setCircularRegion( latitude, longitude, radius)
    .setExpirationDuration( duration )
    .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER)
    .build();
    }
}
