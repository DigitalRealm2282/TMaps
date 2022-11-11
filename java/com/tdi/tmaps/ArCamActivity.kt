package com.tdi.tmaps

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.beyondar.android.util.location.BeyondarLocationManager
import com.beyondar.android.world.GeoObject
import com.beyondar.android.world.World
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.tdi.tmaps.ar.ArFragmentSupport
import com.tdi.tmaps.databinding.ActivityArCamBinding
import com.tdi.tmaps.network.DirectionsResponse
import com.tdi.tmaps.network.RetrofitInterface
import com.tdi.tmaps.network.model.Step
import com.tdi.tmaps.service.MyLocationReceiver
import com.tdi.tmaps.utils.LocationCalc
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

open class ArCamActivity :
    FragmentActivity(),
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener {

    private var srcLatLng: String? = null
    private var destLatLng: String? = null
    private lateinit var steps: Array<Step?>
    private var locationManager: LocationManager? = null
    private var mLastLocation: Location? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var arFragmentSupport: ArFragmentSupport? = null
    private var world: World? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var binding: ActivityArCamBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArCamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        steps = emptyArray()
        binding.arSourceDest.text = intent.getStringExtra("SRC") + " -> " + intent.getStringExtra("DEST")
        srcLatLng = intent.getStringExtra("SRCLATLNG")
        destLatLng = intent.getStringExtra("DESTLATLNG")

        setGoogleApiClient() // Sets the GoogleApiClient



        configureAR() // Configure AR Environment



        directionsCall()

    }

    private fun setGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
        }
    }

    private fun configureAR() {
        val polylineLatLng = ArrayList<List<LatLng>>()
        world = World(applicationContext)
        mLastLocation?.latitude
            ?.let { world?.setGeoPosition(it, mLastLocation!!.longitude) }
        Log.d(
            TAG,
            "Configure_AR: LOCATION" + mLastLocation?.latitude + " " + mLastLocation?.longitude
        )
        world?.setDefaultImage(R.drawable.ar_sphere_default)
        arFragmentSupport = supportFragmentManager.findFragmentById(
            R.id.ar_cam_fragment
        ) as ArFragmentSupport?

        arrayOfNulls<GeoObject>(steps.size)
        Log.d(TAG, "Configure_AR: STEP.LENGTH:" + steps.size)
        // TODO The given below is for rendering MAJOR STEPS LOCATIONS
        for (i in steps.indices) {
            polylineLatLng.add(i, PolyUtil.decode(steps[i]?.polyline?.points))
            val instructions: String = steps[i]?.htmlInstructions!!
            if (i == 0) {
                val signObject = GeoObject((10000 + i).toLong())
                signObject.setImageResource(R.drawable.start)
                signObject.setGeoPosition(
                    steps[i]!!.startLocation?.lat!!.toDouble(),
                    steps[i]!!.startLocation?.lng!!.toDouble()
                )
                world!!.addBeyondarObject(signObject)
                Log.d(TAG, "Configure_AR: START SIGN:$i")
            }
            if (i == steps.size - 1) {
                val signObject = GeoObject((10000 + i).toLong())
                signObject.setImageResource(R.drawable.stop)
                val latLng = SphericalUtil.computeOffset(
                    LatLng(
                        steps[i]?.endLocation?.lat!!.toDouble(),
                        steps[i]?.endLocation?.lng!!.toDouble()
                    ),
                    4.0,
                    SphericalUtil.computeHeading(
                        LatLng(
                            steps[i]?.startLocation?.lat!!.toDouble(),
                            steps[i]?.startLocation?.lng!!.toDouble()
                        ),
                        LatLng(
                            steps[i]?.endLocation?.lat!!.toDouble(),
                            steps[i]?.endLocation?.lng!!.toDouble()
                        )
                    )
                )
                signObject.setGeoPosition(latLng.latitude, latLng.longitude)
                world!!.addBeyondarObject(signObject)
                Log.d(TAG, "Configure_AR: STOP SIGN:$i")
            }
            if (instructions.contains("right")) {
                Log.d(TAG, "Configure_AR: $instructions")
                val signObject = GeoObject((10000 + i).toLong())
                signObject.setImageResource(R.drawable.turn_right)
                signObject.setGeoPosition(
                    steps[i]?.startLocation?.lat!!.toDouble(),
                    steps[i]?.startLocation?.lng!!.toDouble()
                )
                world!!.addBeyondarObject(signObject)
                Log.d(TAG, "Configure_AR: RIGHT SIGN:$i")
            } else if (instructions.contains("left")) {
                Log.d(TAG, "Configure_AR: $instructions")
                val signObject = GeoObject((10000 + i).toLong())
                signObject.setImageResource(R.drawable.turn_left)
                signObject.setGeoPosition(
                    steps[i]!!.startLocation?.lat!!.toDouble(),
                    steps[i]!!.startLocation?.lng!!.toDouble()
                )
                world!!.addBeyondarObject(signObject)
                Log.d(TAG, "Configure_AR: LEFT SIGN:$i")
            }
        }
        var tempPolyCount = 0
        var tempInterPolyCount = 0

        // TODO The Given below is for rendering all the LatLng in THe polylines , which is more accurate
        for (j in polylineLatLng.indices) {
            for (k in polylineLatLng[j].indices) {
                val polyGeoObj = GeoObject((1000 + tempPolyCount++).toLong())
                polyGeoObj.setGeoPosition(
                    polylineLatLng[j][k].latitude,
                    polylineLatLng[j][k].longitude
                )
                polyGeoObj.setImageResource(R.drawable.ar_sphere_150x)
                polyGeoObj.name = "arObj$j$k"

                /*
                To fill the gaps between the Poly objects as AR Objects in the AR View , add some more
                AR Objects which are equally spaced and provide a continuous AR Object path along the route

                Haversine formula , Bearing Calculation and formula to find
                Destination point given distance and bearing from start point is used .
                 */try {

                    // Initialize distance of consecutive polyobjects
                    val dist: Double = LocationCalc.haversine(
                        polylineLatLng[j][k].latitude,
                        polylineLatLng[j][k].longitude, polylineLatLng[j][k + 1].latitude,
                        polylineLatLng[j][k + 1].longitude
                    ) * 1000

                    // Log.d(TAG, "Configure_AR: polyLineLatLng("+j+","+k+")="+polylineLatLng.get(j).get(k).latitude+","+polylineLatLng.get(j).get(k).longitude);
                    // Log.d(TAG, "Configure_AR: polyLineLatLng("+j+","+(k+1)+")="+polylineLatLng.get(j).get(k+1).latitude+","+polylineLatLng.get(j).get(k+1).longitude);

                    // Check if distance between polyobjects is greater than twice the amount of space
                    // intended , here it is (3*2)=6 .
                    if (dist > 6) {

                        // Initialize count of ar objects to be added
                        val arObjCount = dist.toInt() / 3 - 1

                        // Log.d(TAG, "Configure_AR: Dist:" + dist + " # No of Objects: " + arObj_count + "\n --------");
                        LocationCalc.calcBearing(
                            polylineLatLng[j][k].latitude,
                            polylineLatLng[j][k + 1].latitude,
                            polylineLatLng[j][k].longitude,
                            polylineLatLng[j][k + 1].longitude
                        )
                        val heading = SphericalUtil.computeHeading(
                            LatLng(
                                polylineLatLng[j][k].latitude,
                                polylineLatLng[j][k].longitude
                            ),
                            LatLng(
                                polylineLatLng[j][k + 1].latitude,
                                polylineLatLng[j][k + 1].longitude
                            )
                        )
                        var tempLatLng = SphericalUtil.computeOffset(
                            LatLng(
                                polylineLatLng[j][k].latitude,
                                polylineLatLng[j][k].longitude
                            ),
                            3.0, heading
                        )

                        // The distance to be incremented
                        var incrementDist = 3.0
                        for (i in 0 until arObjCount) {
                            val interPolyGeoObj =
                                GeoObject((5000 + tempInterPolyCount++).toLong())

                            // Store the Lat,Lng details into new LatLng Objects using the functions
                            // in LocationCalc class.
                            if (i > 0 && k < polylineLatLng[j].size) {
                                incrementDist += 3.0
                                tempLatLng = SphericalUtil.computeOffset(
                                    LatLng(
                                        polylineLatLng[j][k].latitude,
                                        polylineLatLng[j][k].longitude
                                    ),
                                    incrementDist,
                                    SphericalUtil.computeHeading(
                                        LatLng(
                                            polylineLatLng[j][k].latitude,
                                            polylineLatLng[j][k].longitude
                                        ),
                                        LatLng(
                                            polylineLatLng[j][k + 1].latitude,
                                            polylineLatLng[j][k + 1].longitude
                                        )
                                    )
                                )
                            }

                            // Set the Geoposition along with image and name
                            interPolyGeoObj.setGeoPosition(
                                tempLatLng.latitude,
                                tempLatLng.longitude
                            )
                            interPolyGeoObj.setImageResource(R.drawable.ar_sphere_default_125x)
                            interPolyGeoObj.name = "inter_arObj$j$k$i"

                            // Log.d(TAG, "Configure_AR: LOC: k="+k+" "+ inter_polyGeoObj.getLatitude() + "," + inter_polyGeoObj.getLongitude());

                            // Add Intermediate ArObjects to Augmented Reality World
                            world!!.addBeyondarObject(interPolyGeoObj)
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Configure_AR: EXCEPTION CAUGHT:" + e.message)
                }

                // Add PolyObjects as ArObjects to Augmented Reality World
                world!!.addBeyondarObject(polyGeoObj)
                Log.d(TAG, "\n\n")
            }
        }

        // Send to the fragment
        arFragmentSupport?.world = world
    }

    private fun getInt() {
        if (intent != null) {
            //intent = intent
            binding.arSourceDest.text =
                (intent)?.getStringExtra("SRC") + " -> " + (intent)?.getStringExtra("DEST")
            srcLatLng = intent.getStringExtra("SRCLATLNG")
            destLatLng = (intent)?.getStringExtra("DESTLATLNG")
            directionsCall()

            // HTTP Google Directions API Call
        }
    }

    private fun directionsCall() {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(resources.getString(R.string.directions_base_url))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService: RetrofitInterface = retrofit.create(RetrofitInterface::class.java)
        val call: Call<DirectionsResponse?>? = apiService.getDirections(
            srcLatLng, destLatLng, // TravelMode.DRIVING,
            resources.getString(R.string.maps_web_key)
        )
        Log.d(
            TAG,
            "Directions_call: srcLat lng:$srcLatLng\ndestLatLng:$destLatLng"
        )
        call?.enqueue(object : Callback<DirectionsResponse?> {
            override fun onResponse(
                call: Call<DirectionsResponse?>,
                response: Response<DirectionsResponse?>
            ) {
                val directionsResponse = response.body()
                val stepArraySize = directionsResponse?.routes!![0].legs?.get(0)?.steps!!.size
                binding.arDirDistance.visibility = View.VISIBLE
                binding.arDirDistance.text = directionsResponse.routes?.get(0)?.legs?.get(0)
                    ?.distance?.text
                binding.arDirTime.visibility = View.VISIBLE
                binding.arDirTime.text = directionsResponse.routes?.get(0)!!.legs?.get(0)
                    ?.duration?.text
                steps = arrayOfNulls(stepArraySize)
                for (i in 0 until stepArraySize) {
                    steps[i] =
                        directionsResponse.routes?.get(0)!!.legs?.get(0)?.steps?.get(i)
                    Log.d(
                        TAG,
                        "onResponse: STEP " + i + ": " + steps[i]?.endLocation?.lat +
                            " " + steps[i]?.endLocation?.lng
                    )
                }
                configureAR()
            }

            override fun onFailure(call: Call<DirectionsResponse?>, t: Throwable) {
                Log.d(TAG, "onFailure: FAIL" + t.message)
                AlertDialog.Builder(applicationContext).setMessage("Fetch Failed").show()
            }
        })
    }

    public override fun onStart() {
        mGoogleApiClient?.connect()
        super.onStart()
    }

    public override fun onStop() {
        mGoogleApiClient?.disconnect()
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
        BeyondarLocationManager.disable()
    }

    override fun onResume() {
        super.onResume()
        BeyondarLocationManager.enable()
    }

    override fun onConnected(bundle: Bundle?) {
        if ((
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            )
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
            LocationManager.NETWORK_PROVIDER

            // mLastLocation = locationManager?.getLastKnownLocation(locationProvider)
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                (mGoogleApiClient)!!
            )
            if (mLastLocation != null) {
                try {
                    getInt() // Fetch Intent Values
                } catch (e: Exception) {
                    Log.d(TAG, "onCreate: Intent Error")
                }
            }
        }
        startLocationUpdates()
    }

    private fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.Builder(1000L)
            .setIntervalMillis(1000L)
            .setMinUpdateIntervalMillis(500L)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)

        return locationRequest.build()
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, MyLocationReceiver::class.java)
        intent.action = MyLocationReceiver.ACTION
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
    private fun startLocationUpdates() {
        try {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            if (ActivityCompat.checkSelfPermission(this@ArCamActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this@ArCamActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                fusedLocationProviderClient.requestLocationUpdates(createLocationRequest(), getPendingIntent())
        } catch (e: SecurityException) {
            Toast.makeText(
                this, "Location Permission not granted . Please Grant the permissions",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onConnectionSuspended(i: Int) {}
    override fun onConnectionFailed(connectionResult: ConnectionResult) {}
    override fun onLocationChanged(location: Location) {
        if (world != null) {
            world!!.setGeoPosition(location.latitude, location.longitude)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onLocationChanged(locations: List<Location?>) {
//        super<LocationListener>.onLocationChanged(locations)
        super.onLocationChanged(locations)
    }

//    override fun onFlushComplete(requestCode: Int) {
// //        super<LocationListener>.onFlushComplete(requestCode)
//        super.onFlushComplete(requestCode)
//    }
//
//
//    override fun onPointerCaptureChanged(hasCapture: Boolean) {
//        super.onPointerCaptureChanged(hasCapture)
//    }

    companion object {
        private const val TAG = "ArCamActivity"
//        fun setTint(d: Drawable?, color: Int): Drawable {
//            val wrappedDrawable = DrawableCompat.wrap((d)!!)
//            DrawableCompat.setTint(wrappedDrawable, color)
//            return wrappedDrawable
//        }
    }
}