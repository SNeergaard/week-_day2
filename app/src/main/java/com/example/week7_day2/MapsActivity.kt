package com.example.week7_day2

import android.Manifest
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
import android.content.pm.PackageManager
import android.graphics.drawable.ScaleDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.model.BitmapDescriptorFactory


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    val PERMISSION_INDEX_ID = 777
    private lateinit var mMap: GoogleMap
    private var locationManager : LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        //locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?;
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.setMinZoomPreference(18.0f)
        setUpMap()
        val sydney = LatLng(-34.0, 151.0)
        displayLocationOnMap(sydney, "somewhere")
    }

    private fun setUpMap() {
//        if (ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_INDEX_ID)
//            return
//        }
//        mMap.isMyLocationEnabled = true

    }

    private fun displayLocationOnMap(latLng: LatLng, title: String) {
        mMap.addMarker(MarkerOptions()
            .position(latLng)
            .title(title)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)))
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
        val alertDialog1: AlertDialog
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
        alertDialog1 = builder.create()
        alertDialog1.show()

    }

//    private val locationListener: LocationListener = object : LocationListener {
//        override fun onLocationChanged(location: Location) {
//            val curentLocation = LatLng(location.latitude, location.longitude)
//        }
//    }

    fun CurrentLocation(view: View) {

    }
}
