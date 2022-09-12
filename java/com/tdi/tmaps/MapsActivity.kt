package com.tdi.tmaps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.tdi.tmaps.databinding.ActivityMapsBinding
import com.tdi.tmaps.model.MyLocation
import com.tdi.tmaps.utils.Common
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.firebase.database.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, ValueEventListener,
    OnMapsSdkInitializedCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var trackingUserLocation:DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerEventRealtime()
//        mMap.setOnMarkerClickListener { p0 ->
//            if (!ttsTracking(p0)) {
//                val alertDialogStart = AlertDialog.Builder(this@MapsActivity)
//                alertDialogStart.setIcon(R.drawable.ic_baseline_explore_24)
//                alertDialogStart.setTitle("Track user")
//                alertDialogStart.setMessage("Start tracking current user ?")
//                alertDialogStart.setNegativeButton("Close") { DialogInterface, _ -> DialogInterface.dismiss() }
//                alertDialogStart.setPositiveButton("Start") { _, _ -> ttsTracking(p0) }
//                alertDialogStart.show()
//            } else {
//                val alertDialogStop = AlertDialog.Builder(this@MapsActivity)
//                alertDialogStop.setIcon(R.drawable.ic_baseline_explore_24)
//                alertDialogStop.setTitle("Track user")
//                alertDialogStop.setMessage("Stop tracking current user ?")
//                alertDialogStop.setNegativeButton("Close") { DialogInterface, _ -> DialogInterface.dismiss() }
//                alertDialogStop.setPositiveButton("Stop") { _, _ -> ttsTracking(p0) }
//                alertDialogStop.show()
//            }
//            true
//        }
    }

//    private fun openDialog() {
//        if (!ttsTracking(p0)) {
//            val alertDialogStart = AlertDialog.Builder(this)
//            alertDialogStart.setIcon(R.drawable.ic_baseline_explore_24)
//            alertDialogStart.setTitle("Track user")
//            alertDialogStart.setMessage("Start tracking current user ?")
//            alertDialogStart.setNegativeButton("Close") { DialogInterface, _ -> DialogInterface.dismiss() }
//            alertDialogStart.setPositiveButton("Start") { _, _ -> ttsTracking(p0) }
//            alertDialogStart.show()
//        }else{
//            val alertDialogStop = AlertDialog.Builder(this)
//            alertDialogStop.setIcon(R.drawable.ic_baseline_explore_24)
//            alertDialogStop.setTitle("Track user")
//            alertDialogStop.setMessage("Stop tracking current user ?")
//            alertDialogStop.setNegativeButton("Close") { DialogInterface, _ -> DialogInterface.dismiss() }
//            alertDialogStop.setPositiveButton("Stop") { _, _ -> ttsTracking(p0) }
//            alertDialogStop.show()
//        }
//    }
//
//    private fun ttsTracking(p0: Marker):Boolean {
//
//        p0.snippet =
//            return true
//    }


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
                .snippet(Common.getDataFormatted(Common.convertTimeStampToDate(location.time))+"Speed:"+ location.speed))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userMarker,16F))
        }
    }

    override fun onCancelled(error: DatabaseError) {
        TODO("Not yet implemented")
    }

    override fun onMapsSdkInitialized(p0: MapsInitializer.Renderer) {
        when (p0) {
            MapsInitializer.Renderer.LATEST -> Log.d("MapsDemo", "The latest version of the renderer is used.")
            MapsInitializer.Renderer.LEGACY -> Log.d("MapsDemo", "The legacy version of the renderer is used.")
        }
    }
}