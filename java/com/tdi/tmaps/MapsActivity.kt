package com.tdi.tmaps


import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.tdi.tmaps.databinding.ActivityMapsBinding
import com.tdi.tmaps.model.MyLocation
import com.tdi.tmaps.utils.Common
import com.google.firebase.database.*
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, ValueEventListener, TextToSpeech.OnInitListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var trackingUserLocation:DatabaseReference
    private  var tts: TextToSpeech ?=null
    private lateinit var preferences: SharedPreferences

    companion object{
        const val REQUEST_LOCATION_PERMISSION = 0
    }


    override fun onDataChange(snapshot: DataSnapshot) {
        if (snapshot.value != null){
            val location = snapshot.getValue(MyLocation::class.java)
            val userMarker = LatLng(location!!.latitude,location.longitude)
//            val angle = 130.0; // rotation angle
//            val x = sin(-angle * Math.PI / 180) * 0.5 + 0.5
//            val y = -(cos(-angle * Math.PI / 180) * 0.5 - 0.5)
//          add beside snippet      .infoWindowAnchor(x.toFloat(),y.toFloat())
            if (Common.trackingUser == null){
                if (location.speed*3.6 >= 5) {
                    mMap.addMarker(MarkerOptions().position(userMarker).title(Common.loggedUser!!.email).icon(bitmapDescriptorFromVector(this, R.drawable.ic_motorbike_icon)).snippet(Common.getDataFormatted(Common.convertTimeStampToDate(location.time)) + ",Speed: " + location.speed * 3.6 + " km/h"))
                }else {
                    mMap.addMarker(MarkerOptions().position(userMarker).title(Common.loggedUser!!.email).icon(bitmapDescriptorFromVector(this, R.drawable.ic_male)).snippet(Common.getDataFormatted(Common.convertTimeStampToDate(location.time)) + ",Speed: " + location.speed * 3.6 + " km/h"))
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userMarker,16F))
            } else {
                if (location.speed*3.6 >= 5) {
                    mMap.addMarker(MarkerOptions().position(userMarker).title(Common.trackingUser!!.email).icon(bitmapDescriptorFromVector(this, R.drawable.ic_motorbike_icon))
                            .snippet(Common.getDataFormatted(Common.convertTimeStampToDate(location.time)) + ",Speed: " + location.speed*3.6+" km/h"))
                    if (tts != null)
                        tts!!.speak("Friend speed: " + location.speed*3.6 +" km/h", TextToSpeech.QUEUE_FLUSH, null, "")

                } else {
                    mMap.addMarker(MarkerOptions().position(userMarker).title(Common.trackingUser!!.email).icon(bitmapDescriptorFromVector(this, R.drawable.ic_male)).snippet(Common.getDataFormatted(Common.convertTimeStampToDate(location.time)) + ",Speed: " + location.speed*3.6+" km/h"))
                }

                if (location.speed*3.6 >= 50 && tts != null) tts!!.speak("Speeding to: " + location.speed*3.6 +" km/h", TextToSpeech.QUEUE_FLUSH, null, "")
//            mMap.addMarker(MarkerOptions().position(userMarker).title(Common.trackingUser!!.email).icon(null)
//                .snippet(Common.getDataFormatted(Common.convertTimeStampToDate(location.time))+",Speed: "+location.speed))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userMarker, 16F))
            }
        }
    }

    override fun onCancelled(error: DatabaseError) {
        Toast.makeText(this,error.message,Toast.LENGTH_SHORT).show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerEventRealtime()
        preferences = getSharedPreferences("rideMode", MODE_PRIVATE)

        if (preferences.getBoolean("ttsMode",true)){
            tts = TextToSpeech(this, this)
        }else {
            tts = null
            Toast.makeText(this,"Ride mode deactivated",Toast.LENGTH_SHORT).show()
        }
    }



    private fun registerEventRealtime() {
        trackingUserLocation = if (Common.trackingUser == null){
            FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION)
                .child(Common.loggedUser!!.uid!!)
        }else{
            FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION)
                .child(Common.trackingUser!!.uid!!)
        }
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

    override fun onDestroy() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
        mMap.isMyLocationEnabled = true
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.my_uber_style))

    }



    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = tts!!.setLanguage(Locale.ENGLISH)
            tts!!.defaultEngine
            tts!!.defaultVoice
            tts!!.setPitch(1.1F)
            tts!!.setSpeechRate(0.8F)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language specified is not supported!")
                Toast.makeText(this,"Language not supported",Toast.LENGTH_SHORT).show() }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }
        tts!!.speak("Ride mode Activated", TextToSpeech.QUEUE_FLUSH, null, "")
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }


}