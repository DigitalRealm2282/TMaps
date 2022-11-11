package com.tdi.tmaps

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.location.Location
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.beyondar.android.view.OnClickBeyondarObjectListener
import com.beyondar.android.world.BeyondarObject
import com.beyondar.android.world.GeoObject
import com.beyondar.android.world.World
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.SphericalUtil
import com.tdi.tmaps.ar.ArBeyondarGLSurfaceView
import com.tdi.tmaps.ar.ArFragmentSupport
import com.tdi.tmaps.ar.OnTouchBeyondarViewListenerMod
import com.tdi.tmaps.databinding.ActivityPoiBrowserBinding
import com.tdi.tmaps.network.PlaceResponse
import com.tdi.tmaps.network.PoiResponse
import com.tdi.tmaps.network.RetrofitInterface
import com.tdi.tmaps.network.poi.Result
import com.tdi.tmaps.utils.UtilsCheck
import dmax.dialog.SpotsDialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PoiBrowserActivity :
    FragmentActivity(),
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    OnClickBeyondarObjectListener,
    OnTouchBeyondarViewListenerMod {
    private var textView: TextView? = null
    private var mLastLocation: Location? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var arFragmentSupport: ArFragmentSupport? = null
    private var loading:SpotsDialog ?=null
    private var world: World? = null
    private lateinit var binding: ActivityPoiBrowserBinding
    private lateinit var resource: Resources


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPoiBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.seekbarCardview.visibility = View.GONE
        binding.poiBrwoserProgress.visibility = View.GONE
        binding.poiPlaceDetail.visibility = View.GONE

        resource = resources
        loading = SpotsDialog(this,resource.getString(R.string.loading),R.style.Custom)
        loading?.setCancelable(true)
        loading?.show()

        if (!UtilsCheck.isNetworkConnected(this)) {
            val mySnackBar = Snackbar.make(
                findViewById(R.id.poi_layout),
                "Turn Internet On", Snackbar.LENGTH_SHORT
            )
            mySnackBar.show()
        }

        arFragmentSupport = supportFragmentManager.findFragmentById(
            R.id.poi_cam_fragment
        ) as ArFragmentSupport?
        arFragmentSupport?.setOnClickBeyondarObjectListener(this)
        arFragmentSupport?.setOnTouchBeyondarViewListener(this)
        textView = findViewById<View>(R.id.loading_text) as TextView
        setGoogleApiClient() // Sets the GoogleApiClient
        binding.poiPlaceCloseBtn.setOnClickListener {
            binding.seekbarCardview.visibility = View.VISIBLE
            binding.poiPlaceDetail.visibility = View.GONE
            binding.poiPlaceImage.setImageResource(R.color.transparent)
            binding.poiPlaceName.text = " "
            binding.poiPlaceAddress.text = " "
        }
        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (i == 0) {
                    poiListCall(300)
                } else {
                    poiListCall((i + 1) * 300)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (seekBar.progress == 0) {
                    Toast.makeText(
                        this@PoiBrowserActivity,
                        "Radius: 300 Metres",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@PoiBrowserActivity,
                        "Radius: " + (seekBar.progress + 1) * 300 + " Metres",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    fun poiListCall(radius: Int) {
        binding.poiBrwoserProgress.visibility = View.VISIBLE
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(resources.getString(R.string.directions_base_url))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService: RetrofitInterface = retrofit.create(RetrofitInterface::class.java)
        val call: Call<PoiResponse?>? = apiService.listPOI(
            mLastLocation!!.latitude.toString() + "," + mLastLocation!!.longitude.toString(),
            radius,
            resources.getString(R.string.maps_web_key)
        )
        call?.enqueue(object : Callback<PoiResponse?> {
            override fun onResponse(call: Call<PoiResponse?>, response: Response<PoiResponse?>) {
                binding.poiBrwoserProgress.visibility = View.GONE
                binding.seekbarCardview.visibility = View.VISIBLE
                val poiResult: List<Result> = response.body()?.results!!
                configureAR(poiResult)
            }

            override fun onFailure(call: Call<PoiResponse?>, t: Throwable) {
                Toast.makeText(this@PoiBrowserActivity,t.message,Toast.LENGTH_LONG).show()
                binding.poiBrwoserProgress.visibility = View.GONE
            }
        })
    }

    private fun poiDetailsCall(placeId: String?) {
        binding.poiBrwoserProgress.visibility = View.VISIBLE
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(resources.getString(R.string.directions_base_url))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService: RetrofitInterface = retrofit.create(RetrofitInterface::class.java)
        val call: Call<PlaceResponse?>? = apiService.getPlaceDetail(
            placeId,
            resources.getString(R.string.maps_web_key)
        )
        call?.enqueue(object : Callback<PlaceResponse?> {
            override fun onResponse(call: Call<PlaceResponse?>, response: Response<PlaceResponse?>) {
                binding.seekbarCardview.visibility = View.GONE
                binding.poiPlaceDetail.visibility = View.VISIBLE
                binding.poiBrwoserProgress.visibility = View.GONE
                val result: com.tdi.tmaps.network.place.Result = response.body()?.result!!
                binding.poiPlaceName.text = result.name
                binding.poiPlaceAddress.text = result.formattedAddress
                try {
                    val url = HttpUrl.Builder()
                        .scheme("https")
                        .host("maps.googleapis.com")
                        .addPathSegments("maps/api/place/photo")
                        .addQueryParameter("maxwidth", "400")
                        .addQueryParameter(
                            "photoreference",
                            result.photos?.get(0)?.photoReference
                        )
                        .addQueryParameter("key", resources.getString(R.string.maps_web_key))
                        .build()
                    PoiPhotoAsync().execute(url.toString())
                } catch (e: Exception) {
                    Log.d(TAG, "onResponse: " + e.message)
                    Toast.makeText(
                        this@PoiBrowserActivity,
                        "No image available",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                binding.poiPlaceMapsDirection.setOnClickListener {
                    val intent: Intent
                    try {
                        val builder = Uri.Builder()
                        builder.scheme("http")
                            .authority("maps.google.com")
                            .appendPath("maps")
                            .appendQueryParameter(
                                "saddr",
                                mLastLocation?.latitude.toString() + "," + mLastLocation?.longitude
                            )
                            .appendQueryParameter(
                                "daddr",
                                result.geometry!!.location!!.lat.toString() + "," +
                                    result.geometry!!.location!!.lng.toString()
                            )
                        intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(builder.build().toString())
                        )
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        Log.d(
                            TAG,
                            "onClick: mapNav Exception caught"
                        )
                        Toast.makeText(
                            this@PoiBrowserActivity,
                            "Unable to Open Maps Navigation",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                binding.poiPlaceArDirection.setOnClickListener {
                    val intent = Intent(this@PoiBrowserActivity, ArCamActivity::class.java)
                    try {
                        intent.putExtra("SRC", "Current Location")
                        intent.putExtra(
                            "DEST",
                            result.geometry?.location?.lat.toString() + "," +
                                result.geometry?.location?.lng.toString()
                        )
                        intent.putExtra(
                            "SRCLATLNG",
                            mLastLocation?.latitude.toString() + "," + mLastLocation?.longitude
                        )
                        intent.putExtra(
                            "DESTLATLNG",
                            result.geometry!!.location!!.lat.toString() + "," +
                                result.geometry!!.location!!.lng.toString()
                        )
                        startActivity(intent)
                        finish()
                    } catch (npe: NullPointerException) {
                        Log.d(
                            TAG,
                            "onClick: The IntentExtras are Empty"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<PlaceResponse?>, t: Throwable) {
                Toast.makeText(this@PoiBrowserActivity,t.message,Toast.LENGTH_LONG).show()
                binding.poiBrwoserProgress.visibility = View.GONE
            }
        })
    }

    inner class PoiPhotoAsync :
        AsyncTask<String?, Void?, Bitmap?>() {
        override fun onPostExecute(bitmap: Bitmap?) {
            binding.poiPlaceImage.scaleType = ImageView.ScaleType.CENTER_CROP
            binding.poiPlaceImage.setImageBitmap(bitmap)
        }

        override fun doInBackground(vararg urls: String?): Bitmap? {
            val imageURL = urls[0]
            var bitmap: Bitmap? = null
            try {
                val input = URL(imageURL).openStream()
                bitmap = BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return bitmap
        }
    }

    private fun configureAR(pois: List<Result>) {
        // layoutInflater = getLayoutInflater()
        loading?.dismiss()
        world = World(applicationContext)
        world!!.setGeoPosition(mLastLocation!!.latitude, mLastLocation!!.longitude)
        world!!.defaultImage = R.drawable.ar_sphere_default.toString()
        arFragmentSupport?.gLSurfaceView?.pullCloserDistance = 25F
        arrayOfNulls<GeoObject>(pois.size)
        for (i in pois.indices) {
            val poiGeoObj = GeoObject((1000 * (i + 1)).toLong())
            // ArObject2 poiGeoObj=new ArObject2(1000*(i+1));

//            poiGeoObj.setImageUri(getImageUri(this,textAsBitmap(pois.get(i).getName(),10.0f, Color.WHITE)));
            poiGeoObj.setGeoPosition(
                pois[i].geometry!!.location!!.lat!!.toDouble(),
                pois[i].geometry!!.location!!.lng!!.toDouble()
            )
            poiGeoObj.name = pois[i].placeId
            // poiGeoObj.setPlaceId(pois.get(0).getPlaceId());

            // Bitmap bitmap=textAsBitmap(pois.get(i).getName(),30.0f,Color.WHITE);
            var snapshot: Bitmap? = null
            val view: View = layoutInflater.inflate(R.layout.poi_container, null)
            val name = view.findViewById<View>(R.id.poi_container_name) as TextView
            val dist = view.findViewById<View>(R.id.poi_container_dist) as TextView
            val icon = view.findViewById<View>(R.id.poi_container_icon) as ImageView
            name.text = pois[i].name
            val distance = (
                SphericalUtil.computeDistanceBetween(
                    LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude),
                    LatLng(
                        pois[i].geometry!!.location!!.lat!!.toDouble(),
                        pois[i].geometry!!.location!!.lng!!.toDouble()
                    )
                ) / 1000
                ).toString()
            val d = "$distance KM"
            dist.text = d
            val type: String = pois[i].types!![0]
            Log.d(TAG, "Configure_AR: TYPE:" + type + "LODGING:" + R.string.logding)
            when (type) {
                resources.getString(R.string.restaurant) -> {
                    icon.setImageResource(R.drawable.food_fork_drink)
                }
                resources.getString(R.string.logding) -> {
                    icon.setImageResource(R.drawable.hotel)
                }
                resources.getString(R.string.atm) -> {
                    icon.setImageResource(R.drawable.cash_usd)
                }
                resources.getString(R.string.hosp) -> {
                    icon.setImageResource(R.drawable.hospital)
                }
                resources.getString(R.string.movie) -> {
                    icon.setImageResource(R.drawable.filmstrip)
                }
                resources.getString(R.string.cafe) -> {
                    icon.setImageResource(R.drawable.coffee)
                }
                resources.getString(R.string.bakery) -> {
                    icon.setImageResource(R.drawable.food)
                }
                resources.getString(R.string.mall) -> {
                    icon.setImageResource(R.drawable.shopping)
                }
                resources.getString(R.string.pharmacy) -> {
                    icon.setImageResource(R.drawable.pharmacy)
                }
                resources.getString(R.string.park) -> {
                    icon.setImageResource(R.drawable.pine_tree)
                }
                resources.getString(R.string.bus) -> {
                    icon.setImageResource(R.drawable.bus)
                }
                else -> {
                    icon.setImageResource(R.drawable.map_icon)
                }
            }
            view.isDrawingCacheEnabled = true
            view.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_LOW
            try {
                //  Paint paint = new Paint(ANTI_ALIAS_FLAG);
//                paint.setTextSize(textSize);
//                paint.setColor(textColor);
                // paint.setTextAlign(Paint.Align.LEFT);
//                float baseline = -paint.ascent(); // ascent() is negative
//                int width = (int) (paint.measureText(pois.get(i).getName()) + 0.5f); // round
//                int height = (int) (baseline + paint.descent() + 0.5f);
                view.measure(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                snapshot = Bitmap.createBitmap(
                    view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(snapshot)
                view.layout(0, 0, view.measuredWidth, view.measuredHeight)
                view.draw(canvas)
                // canvas.drawBitmap(snapshot);
                // snapshot = Bitmap.createBitmap(view.getDrawingCache(),10,10,200,100); // You can tell how to crop the snapshot and whatever in this method
            } finally {
                view.isDrawingCacheEnabled = false
            }

            val uri = saveToInternalStorage(snapshot, pois[i].id + ".png")

            // icon.setImageURI(Uri.parse(uri));
            poiGeoObj.imageUri = uri
            world!!.addBeyondarObject(poiGeoObj)
        }
        textView?.visibility = View.INVISIBLE

        // ... and send it to the fragment
        arFragmentSupport?.world = world
    }

    private fun saveToInternalStorage(bitmapImage: Bitmap?, name: String): String {
        val cw = ContextWrapper(applicationContext)
        // path to /data/data/yourapp/app_data/imageDir
        val directory = cw.getDir("imageDir", MODE_PRIVATE)
        // Create imageDir
        val myPath = File(directory, name)
        Log.d(TAG, "saveToInternalStorage: PATH:$myPath")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(myPath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage!!.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return myPath.toString()
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path).toString()

//        val cw =ContextWrapper(getApplicationContext());
//        // path to /data/data/yourapp/app_data/imageDir
//        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
//        // Create imageDir
//        val mypath= File(directory,"profile.jpg");
//
//        var fos :FileOutputStream ?= null
//        try {
//            fos = FileOutputStream(mypath);
//            // Use the compress method on the BitMap object to write image to the OutputStream
//            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                fos.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return directory.getAbsolutePath();
    }

    fun textAsBitmap(text: String?, textSize: Float, textColor: Int): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = textSize
        paint.color = textColor
        paint.textAlign = Paint.Align.LEFT
        val baseline = -paint.ascent() // ascent() is negative
        val width = (paint.measureText(text) + 0.5f).toInt() // round
        val height = (baseline + paint.descent() + 0.5f).toInt()
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        canvas.drawText(text!!, 0f, baseline, paint)
        return image
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

    override fun onStart() {
        mGoogleApiClient?.connect()
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        mGoogleApiClient?.connect()
    }

    override fun onStop() {
        mGoogleApiClient!!.disconnect()
        super.onStop()
    }

    override fun onConnected(bundle: Bundle?) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient!!
            )
            if (mLastLocation != null) {
                try {
                    poiListCall(900)
                } catch (e: Exception) {
                    Log.d(TAG, "onCreate: Intent Error")
                }
            }
        }
    }

    override fun onClickBeyondarObject(beyondarObjects: ArrayList<BeyondarObject>) {
        if (beyondarObjects.size > 0) {
            poiDetailsCall(beyondarObjects[0].name)
        }
    }

    override fun onConnectionSuspended(i: Int) {}
    override fun onConnectionFailed(connectionResult: ConnectionResult) {}
    override fun onTouchBeyondarView(var1: MotionEvent?, var2: ArBeyondarGLSurfaceView?) {
        val x = var1?.x
        val y = var1?.y
        val geoObjects = ArrayList<BeyondarObject?>()

        // This method call is better to don't do it in the UI thread!
        // This method is also available in the BeyondarFragment
        var2?.getBeyondarObjectsOnScreenCoordinates(x!!, y!!, geoObjects)
        var textEvent = ""
        when (var1?.action) {
            MotionEvent.ACTION_DOWN -> textEvent = "Event type ACTION_DOWN: "
            MotionEvent.ACTION_UP -> textEvent = "Event type ACTION_UP: "
            MotionEvent.ACTION_MOVE -> textEvent = "Event type ACTION_MOVE: "
            else -> {}
        }
        val iterator: MutableIterator<BeyondarObject?> = geoObjects.iterator()
        while (iterator.hasNext()) {
            val geoObject = iterator.next()
            textEvent = textEvent + " " + geoObject?.name
            Log.d(
                TAG,
                "onTouchBeyondarView: ATTENTION !!! $textEvent"
            )

            // ...
            // Do something
            // ...
        }
    }

    companion object {
        private const val TAG = "PoiBrowserActivity"
    }
}