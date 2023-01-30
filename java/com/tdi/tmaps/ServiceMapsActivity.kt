package com.tdi.tmaps

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.tdi.tmaps.databinding.ActivityServiceMapsBinding
import com.tdi.tmaps.model.*
import com.tdi.tmaps.utils.Common
import com.tdi.tmaps.utils.Common.evInfo
import com.tdi.tmaps.utils.Common.fsInfo
import com.tdi.tmaps.utils.Common.serviceInfo
import com.tdi.tmaps.utils.Common.tireInfo
import com.tdi.tmaps.utils.Common.washInfo
import java.util.*


class ServiceMapsActivity : AppCompatActivity(), OnMapReadyCallback, ValueEventListener,
    TextToSpeech.OnInitListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityServiceMapsBinding
    private var tts: TextToSpeech? = null
    private lateinit var preferences: SharedPreferences
    private lateinit var pref_icon: SharedPreferences
    private lateinit var prefMap: SharedPreferences
    private lateinit var userLocation: DatabaseReference
    private var userMarker: Marker? = null
    lateinit var eStationInfo: DatabaseReference
    lateinit var fStationInfo: DatabaseReference
    lateinit var washInfoDb: DatabaseReference
    lateinit var tireInfoDb: DatabaseReference
    lateinit var serviceInfoDb: DatabaseReference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityServiceMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefMap = getSharedPreferences("map", MODE_PRIVATE)
        preferences = getSharedPreferences("rideMode", MODE_PRIVATE)
        pref_icon = getSharedPreferences("icon", MODE_PRIVATE)
        eStationInfo = FirebaseDatabase.getInstance().getReference(Common.EV_STATION)
        fStationInfo = FirebaseDatabase.getInstance().getReference(Common.FS_STATION)
        washInfoDb = FirebaseDatabase.getInstance().getReference(Common.CAR_WASH)
        tireInfoDb = FirebaseDatabase.getInstance().getReference(Common.CAR_TIRE)
        serviceInfoDb = FirebaseDatabase.getInstance().getReference(Common.CAR_SERVICE)


        tts = if (preferences.getBoolean("ttsMode", true)) {
            TextToSpeech(this, this)
        } else {
            null
            //Toast.makeText(this, "Ride mode deactivated", Toast.LENGTH_SHORT).show()
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.evStation.setOnClickListener {
            selected(0)
            mMap.clear()
            if (tts != null){
                tts!!.speak(resources.getString(R.string.title_activity_ev_maps), TextToSpeech.QUEUE_FLUSH, null, "")
            }
            getAllEvStations()
        }
        binding.carWash.setOnClickListener {
            selected(1)
            mMap.clear()
            if (tts != null){
                tts!!.speak("Wash", TextToSpeech.QUEUE_FLUSH, null, "")
            }
            getAllCarWash()

        }
        binding.carService.setOnClickListener {
            selected(2)
            mMap.clear()

            if (tts != null){
                tts!!.speak("Service", TextToSpeech.QUEUE_FLUSH, null, "")
            }
            getAllServiceCenters()
        }
        binding.carTire.setOnClickListener {
            selected(3)
            mMap.clear()

            if (tts != null){
                tts!!.speak("Tire Map", TextToSpeech.QUEUE_FLUSH, null, "")
            }
            getAllTireCenters()
        }
        binding.fuelStation.setOnClickListener {
            selected(4)
            mMap.clear()
            if (tts != null){
                tts!!.speak("fuel", TextToSpeech.QUEUE_FLUSH, null, "")
            }
            getAllFuelStations()
        }

        registerEventRealtime()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                MapsActivity.REQUEST_LOCATION_PERMISSION
            )
        }

        mMap.isMyLocationEnabled = true
        if (prefMap.getBoolean("mapStyle", true))
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.tmap_style))
        else
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.my_uber_style))

        mMap.setOnMapLongClickListener {

            if (binding.evStation.isChecked){
                addEV(it)
            }else if (binding.carService.isChecked){
                addCS(it)
            }else if (binding.carTire.isChecked){
                addTS(it)

            }else if (binding.carWash.isChecked){
                addCW(it)

            }else if (binding.fuelStation.isChecked){
                addFS(it)

            }else{
                Toast.makeText(this@ServiceMapsActivity,"Select Category",Toast.LENGTH_SHORT).show()
            }

        }

    }


    private fun addEV(it:LatLng){
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(R.string.app_name)
        alertDialog.setMessage("Add EV Station Here")

        val input = EditText(this)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

        input.layoutParams = lp

        alertDialog.setView(input)

        alertDialog.setPositiveButton(R.string.setting_ok){d,_ ->

            evInfo = EvStation(input.text.toString(), it.latitude,it.longitude,
                Common.loggedUser!!.uid!!,getRandomId(15))

            eStationInfo.child(evInfo!!.id!!)
                .setValue(evInfo)


            d.dismiss()
        }
        alertDialog.setNegativeButton(R.string.cancel){d,_ -> d.dismiss()}
        alertDialog.show()

    }
    private fun addCS(it:LatLng){
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(R.string.app_name)
        alertDialog.setMessage("Add Service Center Here")

        val input = EditText(this)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

        input.layoutParams = lp

        alertDialog.setView(input)

        alertDialog.setPositiveButton(R.string.setting_ok){d,_ ->

            serviceInfo = CarService(input.text.toString(), it.latitude,it.longitude,
                Common.loggedUser!!.uid!!,getRandomId(15))

            serviceInfoDb.child(serviceInfo!!.id!!)
                .setValue(serviceInfo)


            d.dismiss()
        }
        alertDialog.setNegativeButton(R.string.cancel){d,_ -> d.dismiss()}
        alertDialog.show()

    }
    private fun addTS(it: LatLng){
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(R.string.app_name)
        alertDialog.setMessage("Add Tire Center Here")

        val input = EditText(this)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

        input.layoutParams = lp

        alertDialog.setView(input)

        alertDialog.setPositiveButton(R.string.setting_ok){d,_ ->

             tireInfo = CarTires(input.text.toString(), it.latitude,it.longitude,
                Common.loggedUser!!.uid!!,getRandomId(15))

            tireInfoDb.child(tireInfo!!.id!!)
                .setValue(tireInfo)


            d.dismiss()
        }
        alertDialog.setNegativeButton(R.string.cancel){d,_ -> d.dismiss()}
        alertDialog.show()
    }
    private fun addCW(it: LatLng){
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(R.string.app_name)
        alertDialog.setMessage("Add Car Wash Here")

        val input = EditText(this)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

        input.layoutParams = lp

        alertDialog.setView(input)

        alertDialog.setPositiveButton(R.string.setting_ok){d,_ ->

            washInfo = CarWash(input.text.toString(), it.latitude,it.longitude,
                Common.loggedUser!!.uid!!,getRandomId(15))

            washInfoDb.child(washInfo!!.id!!)
                .setValue(washInfo)


            d.dismiss()
        }
        alertDialog.setNegativeButton(R.string.cancel){d,_ -> d.dismiss()}
        alertDialog.show()
    }
    private fun addFS(it:LatLng){
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(R.string.app_name)
        alertDialog.setMessage("Add Fuel Station Here")

        val input = EditText(this)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

        input.layoutParams = lp

        alertDialog.setView(input)

        alertDialog.setPositiveButton(R.string.setting_ok){d,_ ->

            fsInfo = FuelStation(input.text.toString(), it.latitude,it.longitude,
                Common.loggedUser!!.uid!!,getRandomId(15))

            fStationInfo.child(fsInfo!!.id!!)
                .setValue(fsInfo)


            d.dismiss()
        }
        alertDialog.setNegativeButton(R.string.cancel){d,_ -> d.dismiss()}
        alertDialog.show()

    }


    private fun registerEventRealtime() {
        userLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION)
                .child(Common.loggedUser!!.uid!!)

        userLocation.addValueEventListener(this)
    }

    override fun onResume() {
        super.onResume()
        userLocation.addValueEventListener(this)
    }

    override fun onStop() {
        super.onStop()
        userLocation.removeEventListener(this)
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        if (snapshot.value != null){
            val location = snapshot.getValue(MyLocation::class.java)
            val userMarker = LatLng(location!!.latitude, location.longitude)
            val icon = if (pref_icon.getString("iconStyle", "car") == "car") bitmapDescriptorFromVector(this, R.drawable.car_145008)
            else
                bitmapDescriptorFromVector(this, R.drawable.ic_motorbike_icon)

            if (Common.loggedUser != null) {
                if (location.speed * 3.6 >= 10) {
                        if (this.userMarker != null) {
                            this.userMarker!!.remove()
                            this.userMarker = null
                            this.userMarker = mMap.addMarker(
                                MarkerOptions().position(userMarker)
                                    .title(Common.loggedUser!!.email).icon(icon).snippet(
                                    Common.getDataFormatted(
                                        Common.convertTimeStampToDate(location.time)
                                    ) + ",Speed: " + location.speed * 3.6 + " km/h"
                                )
                            )!!
                        }else{
                            this.userMarker = mMap.addMarker(
                                MarkerOptions().position(userMarker)
                                    .title(Common.loggedUser!!.email).icon(icon).snippet(
                                        Common.getDataFormatted(
                                            Common.convertTimeStampToDate(location.time)
                                        ) + ",Speed: " + location.speed * 3.6 + " km/h"
                                    )
                            )!!
                        }

                } else {
                        if (this.userMarker != null) {
                            this.userMarker!!.remove()
                            this.userMarker = null
                            this.userMarker = mMap.addMarker(
                                MarkerOptions().position(userMarker)
                                    .title(Common.loggedUser!!.email)
                                    .icon(bitmapDescriptorFromVector(this, R.drawable.ic_male))
                                    .snippet(
                                        Common.getDataFormatted(
                                            Common.convertTimeStampToDate(location.time)
                                        ) + ",Speed: " + location.speed * 3.6 + " km/h"
                                    )
                            )!!
                        }else{
                            this.userMarker = mMap.addMarker(
                                MarkerOptions().position(userMarker)
                                    .title(Common.loggedUser!!.email).icon(icon).snippet(
                                        Common.getDataFormatted(
                                            Common.convertTimeStampToDate(location.time)
                                        ) + ",Speed: " + location.speed * 3.6 + " km/h"
                                    )
                            )!!
                        }
                }
//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userMarker, 16F))
            }
        }
    }

    override fun onCancelled(error: DatabaseError) {
        Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
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
                Log.e("TTS", "The Language specified is not supported!")
                Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }
        tts!!.speak("Ride mode Activated", TextToSpeech.QUEUE_FLUSH, null, "")
    }

    private fun getAllTireCenters(){
        val markerList = ArrayList<CarTires>()
        tireInfoDb.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (markerSnapshot in snapshot.children){
                    val markers = markerSnapshot.getValue(CarTires::class.java)
                    markerList.add(markers!!)
                }
                markerList.forEach{ markerData ->
                    mMap.addMarker(MarkerOptions()
                        .position(LatLng(markerData.Lat!!,markerData.Long!!))
                        .anchor(0.5f, 0.5f)
                        .title(markerData.name)
                        .snippet(markerData.id)
                        .icon(bitmapDescriptorFromVector(this@ServiceMapsActivity, R.drawable.baseline_tire_repair_24)))
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ServiceMapsActivity,error.message,Toast.LENGTH_SHORT).show()
            }

        })
    }
    private fun getAllFuelStations(){
        val markerList = ArrayList<FuelStation>()
        fStationInfo.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (markerSnapshot in snapshot.children){
                    val markers = markerSnapshot.getValue(FuelStation::class.java)
                    markerList.add(markers!!)
                }
                markerList.forEach{ markerData ->
                    mMap.addMarker(MarkerOptions()
                        .position(LatLng(markerData.Lat!!,markerData.Long!!))
                        .anchor(0.5f, 0.5f)
                        .title(markerData.name)
                        .snippet(markerData.id)
                        .icon(bitmapDescriptorFromVector(this@ServiceMapsActivity, R.drawable.baseline_local_gas_station_24)))
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ServiceMapsActivity,error.message,Toast.LENGTH_SHORT).show()
            }

        })
    }
    private fun getAllCarWash(){
        val markerList = ArrayList<CarWash>()
        washInfoDb.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (markerSnapshot in snapshot.children){
                    val markers = markerSnapshot.getValue(CarWash::class.java)
                    markerList.add(markers!!)
                }
                markerList.forEach{ markerData ->
                    mMap.addMarker(MarkerOptions()
                        .position(LatLng(markerData.Lat!!,markerData.Long!!))
                        .anchor(0.5f, 0.5f)
                        .title(markerData.name)
                        .snippet(markerData.id)
                        .icon(bitmapDescriptorFromVector(this@ServiceMapsActivity, R.drawable.baseline_local_car_wash_24)))
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ServiceMapsActivity,error.message,Toast.LENGTH_SHORT).show()
            }

        })
    }
    private fun getAllServiceCenters(){
        val markerList = ArrayList<CarService>()

        serviceInfoDb.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (markerSnapshot in snapshot.children){
                    val markers = markerSnapshot.getValue(CarService::class.java)
                    markerList.add(markers!!)
                }
                markerList.forEach{ markerData ->
                    mMap.addMarker(MarkerOptions()
                        .position(LatLng(markerData.Lat!!,markerData.Long!!))
                        .anchor(0.5f, 0.5f)
                        .title(markerData.name)
                        .snippet(markerData.id)
                        .icon(bitmapDescriptorFromVector(this@ServiceMapsActivity, R.drawable.baseline_car_repair_24)))
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ServiceMapsActivity,error.message,Toast.LENGTH_SHORT).show()
            }

        })
    }
    private fun getAllEvStations() {
        val markerList = ArrayList<EvStation>()
        eStationInfo.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (markerSnapshot in snapshot.children){
                    val markers = markerSnapshot.getValue(EvStation::class.java)
                    markerList.add(markers!!)
                }
                markerList.forEach{ markerData ->
                    mMap.addMarker(MarkerOptions()
                        .position(LatLng(markerData.Lat!!,markerData.Long!!))
                        .anchor(0.5f, 0.5f)
                        .title(markerData.name)
                        .snippet(markerData.id)
                        .icon(bitmapDescriptorFromVector(this@ServiceMapsActivity, R.drawable.baseline_electrical_services_24)))
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ServiceMapsActivity,error.message,Toast.LENGTH_SHORT).show()
            }

        })
    }
    private fun getRandomId(length:Int): String {

            val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
            return (1..length)
                .map { allowedChars.random() }
                .joinToString("")

    }
    private fun selected(select: Int){
        when (select) {
            0 -> {
                binding.evStation.isChecked   = true
                binding.carWash.isChecked     = false
                binding.carService.isChecked  = false
                binding.carTire.isChecked     = false
                binding.fuelStation.isChecked = false

                serviceInfoDb.removeEventListener(this)
                washInfoDb.removeEventListener(this)
                fStationInfo.removeEventListener(this)
                tireInfoDb.removeEventListener(this)

            }
            1 -> {
                binding.evStation.isChecked  = false
                binding.carWash.isChecked    = true
                binding.carService.isChecked = false
                binding.carTire.isChecked    = false
                binding.fuelStation.isChecked= false

                serviceInfoDb.removeEventListener(this)
                fStationInfo.removeEventListener(this)
                tireInfoDb.removeEventListener(this)
                eStationInfo.removeEventListener(this)

            }
            2 -> {
                binding.evStation.isChecked  = false
                binding.carWash.isChecked    = false
                binding.carService.isChecked = true
                binding.carTire.isChecked    = false
                binding.fuelStation.isChecked= false

                washInfoDb.removeEventListener(this)
                fStationInfo.removeEventListener(this)
                tireInfoDb.removeEventListener(this)
                eStationInfo.removeEventListener(this)
            }
            3 -> {
                binding.evStation.isChecked   = false
                binding.carWash.isChecked     = false
                binding.carService.isChecked  = false
                binding.carTire.isChecked     = true
                binding.fuelStation.isChecked = false

                serviceInfoDb.removeEventListener(this)
                washInfoDb.removeEventListener(this)
                fStationInfo.removeEventListener(this)
                eStationInfo.removeEventListener(this)
            }
            4 -> {
                binding.evStation.isChecked   = false
                binding.carWash.isChecked     = false
                binding.carService.isChecked  = false
                binding.carTire.isChecked     = false
                binding.fuelStation.isChecked = true

                serviceInfoDb.removeEventListener(this)
                washInfoDb.removeEventListener(this)
                tireInfoDb.removeEventListener(this)
                eStationInfo.removeEventListener(this)
            }
        }

    }
}