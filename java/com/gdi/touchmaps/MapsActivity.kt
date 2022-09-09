package com.gdi.touchmaps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.gdi.touchmaps.databinding.ActivityMapsBinding
import com.gdi.touchmaps.model.MyLocation
import com.gdi.touchmaps.utils.Common
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.firebase.database.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, ValueEventListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var trackingUserLocation:DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerEventRealtime()
    }

    private fun registerEventRealtime() {
        trackingUserLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION)
            .child(Common.loggedUser!!.uid!!)

        trackingUserLocation.addValueEventListener(this)
    }

    override fun onResume() {
        super.onResume()
        trackingUserLocation.addValueEventListener(this)
    }

    override fun onStop() {
        super.onStop()
        trackingUserLocation.removeEventListener(this)
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.my_uber_style))
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        if (snapshot.value != null){
            val location = snapshot.getValue(MyLocation::class.java)

            val userMarker = LatLng(location!!.latitude,location.longitude)
            mMap.addMarker(MarkerOptions().position(userMarker).title(Common.loggedUser!!.email)
                .snippet(Common.getDataFormatted(Common.convertTimeStampToDate(location.time))))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userMarker,16F))
        }
    }

    override fun onCancelled(error: DatabaseError) {
        TODO("Not yet implemented")
    }
}