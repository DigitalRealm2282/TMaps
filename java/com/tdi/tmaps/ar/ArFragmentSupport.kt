package com.tdi.tmaps.ar

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.beyondar.android.opengl.renderer.ARRenderer.FpsUpdatable
import com.beyondar.android.sensor.BeyondarSensorManager
import com.beyondar.android.util.math.geom.Ray
import com.beyondar.android.view.OnClickBeyondarObjectListener
import com.beyondar.android.world.BeyondarObject
import com.beyondar.android.world.World
import com.tdi.tmaps.R
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by Amal Krishnan on 27-03-2017.
 */
open class ArFragmentSupport() :
    Fragment(),
    FpsUpdatable,
    View.OnClickListener,
    OnTouchListener {
    /**
     *
     * Returns the CameraView for this class instance.
     *
     * @return
     */
    var cameraView: ArSurfaceView? = null
        private set

    /**
     * Returns the SurfaceView for this class instance.
     *
     * @return
     */
    var gLSurfaceView: ArBeyondarGLSurfaceView? = null
        private set
    private var mFpsTextView: TextView? = null
    private var mMainLayout: RelativeLayout? = null
    private var mCamera: Camera? = null
    private var param: Camera.Parameters? = null
    private var mWorld: World? = null
    private var mTouchListener: OnTouchBeyondarViewListenerMod? = null

    // private OnTouchBeyondarViewListener mTouchListener;
    private var mClickListener: OnClickBeyondarObjectListener? = null
    private var mLastScreenTouchX = 0f
    private var mLastScreenTouchY = 0f
    private var mThreadPool: ThreadPoolExecutor? = null
    private var mBlockingQueue: BlockingQueue<Runnable>? = null
    private var mSensorManager: SensorManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBlockingQueue = LinkedBlockingQueue()
        mThreadPool = ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME,
            TimeUnit.MILLISECONDS, mBlockingQueue
        )
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        mSensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private fun init() {
        val params: ViewGroup.LayoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        mMainLayout = RelativeLayout(activity)
        gLSurfaceView = createBeyondarGLSurfaceView()
        gLSurfaceView?.setOnTouchListener(this)
        cameraView = createCameraView()
        mMainLayout?.addView(cameraView, params)
        mMainLayout?.addView(gLSurfaceView, params)
        gLSurfaceView?.maxDistanceToRender = 1000f
        Log.d("ARFRAGG", "init: MaxDistRender" + gLSurfaceView!!.maxDistanceToRender)
    }

    private fun checkIfSensorsAvailable() {
        val pm = requireActivity().packageManager
        val compass = pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS)
        val accelerometer = pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)
        if (!compass && !accelerometer) {
            throw IllegalStateException(
                javaClass.name +
                    " can not run without the compass and the acelerometer sensors."
            )
        } else if (!compass) {
            throw IllegalStateException(javaClass.name + " can not run without the compass sensor.")
        } else if (!accelerometer) {
            throw IllegalStateException(
                (
                    javaClass.name +
                        " can not run without the acelerometer sensor."
                    )
            )
        }
    }

    /**
     * Override this method to personalize the
     * [ BeyondarGLSurfaceView][com.beyondar.android.view.BeyondarGLSurfaceView] that will be instantiated.
     *
     * @return
     */
    private fun createBeyondarGLSurfaceView(): ArBeyondarGLSurfaceView {
        return ArBeyondarGLSurfaceView(requireActivity())
    }

    /**
     * Override this method to personalize the
     * [CameraView][com.beyondar.android.view.CameraView] that will be
     * instantiated.
     *
     * @return
     */
    private fun createCameraView(): ArSurfaceView {
        mCamera = cameraInstance
        param = mCamera?.parameters
        if (param?.supportedFocusModes
            ?.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) == true
        ) param?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        mCamera?.parameters = param
        return ArSurfaceView(activity, mCamera)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        init()
        startRenderingAR()
        return mMainLayout
    }

    override fun onResume() {
        super.onResume()
        cameraView!!.startPreviewCamera()
        gLSurfaceView!!.onResume()
        BeyondarSensorManager.resume(mSensorManager)
        if (mWorld != null) {
            mWorld!!.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        cameraView!!.releaseCamera()
        gLSurfaceView!!.onPause()
        BeyondarSensorManager.pause(mSensorManager)
        if (mWorld != null) {
            mWorld!!.onPause()
        }
    }

    /**
     * Set the listener to get notified when the user touch the AR view.
     *
     * @param listener
     */
    fun setOnTouchBeyondarViewListener(listener: OnTouchBeyondarViewListenerMod?) {
        mTouchListener = listener
    }

    /**
     * Set the [ OnClickBeyondarObjectListener][OnClickBeyondarObjectListener] to get notified when the user click on a
     * [BeyondarObject]
     *
     * @param listener
     */
    fun setOnClickBeyondarObjectListener(listener: OnClickBeyondarObjectListener?) {
        mClickListener = listener
        mMainLayout?.isClickable = listener != null
        mMainLayout?.setOnClickListener(this)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        mLastScreenTouchX = event.x
        mLastScreenTouchY = event.y
        if ((mWorld == null) || (mTouchListener == null) || (event == null)) {
            return false
        }
        mTouchListener!!.onTouchBeyondarView(event, gLSurfaceView)
        return false
    }

    override fun onClick(v: View) {
        if (v === mMainLayout) {
            if (mClickListener == null) {
                return
            }
            val lastX = mLastScreenTouchX
            val lastY = mLastScreenTouchY
            mThreadPool!!.execute(
                Runnable {
                    val beyondarObjects = ArrayList<BeyondarObject?>()
                    gLSurfaceView!!.getBeyondarObjectsOnScreenCoordinates(lastX, lastY, beyondarObjects)
                    if (beyondarObjects.size == 0) return@Runnable
                    gLSurfaceView!!.post {
                        val listener = mClickListener
                        if (listener != null) {
                            Log.d("ArFragment", "run: ListenerSet")
                            listener.onClickBeyondarObject(beyondarObjects)
                        }
                    }
                }
            )
        }
    }
    /**
     * Get the [World] in use by the
     * fragment.
     *
     * @return
     */
    /**
     * Set the [World] that contains all
     * the [BeyondarObject] that
     * will be displayed.
     *
     * @param world
     * The [World] that holds
     * the information of all the elements.
     *
     * @throws IllegalStateException
     * If the device do not have the required sensors available.
     */
    var world: World?
        get() = mWorld
        set(world) {
            try {
                checkIfSensorsAvailable()
            } catch (e: IllegalStateException) {
                throw e
            }
            mWorld = world
            gLSurfaceView!!.setWorld(world)
        }
    /**
     * Get the current sensor delay.
     *
     * @see {@link SensorManager SensorManager}
     *
     *
     * @return Current sensor delay.
     */
    /**
     * Specify the delay to apply to the accelerometer and the magnetic field
     * sensor. If you don't know what is the best value, don't touch it. The
     * following values are applicable:<br></br>
     * <br></br>
     * SensorManager.SENSOR_DELAY_UI<br></br>
     * SensorManager.SENSOR_DELAY_NORMAL <br></br>
     * SensorManager.SENSOR_DELAY_GAME <br></br>
     * SensorManager.SENSOR_DELAY_GAME <br></br>
     * SensorManager.SENSOR_DELAY_FASTEST <br></br>
     * <br></br>
     *
     * @see {@link SensorManager SensorManager}
     *
     *
     * @param delay
     * Sensor delay.
     */
    var sensorDelay: Int
        get() = gLSurfaceView!!.sensorDelay
        set(delay) {
            gLSurfaceView!!.sensorDelay = delay
        }

    /**
     * Use this method to check the frames per second.
     *
     * @param fpsUpdatable
     * Listener that will be notified with current fps.
     *
     * @see FpsUpdatable
     */
    fun setFpsUpdatable(fpsUpdatable: FpsUpdatable?) {
        gLSurfaceView!!.setFpsUpdatable(fpsUpdatable)
    }

    /**
     * Disable the GLSurface to stop rendering the AR world.
     */
    fun stopRenderingAR() {
        gLSurfaceView!!.visibility = View.INVISIBLE
    }

    /**
     * Enable the GLSurface to start rendering the AR world.
     */
    fun startRenderingAR() {
        gLSurfaceView!!.visibility = View.VISIBLE
    }

    /**
     * Get the GeoObject that intersect with the coordinates x, y on the screen.<br></br>
     * __Important__ When this method is called a new [List] is created.
     *
     * @param x
     * X screen position.
     * @param y
     * Y screen position.
     *
     * @return A new list with the
     * [BeyondarObject]
     * that collide with the screen cord
     */
    fun getBeyondarObjectsOnScreenCoordinates(x: Float, y: Float): List<BeyondarObject?> {
        val beyondarObjects = ArrayList<BeyondarObject?>()
        gLSurfaceView!!.getBeyondarObjectsOnScreenCoordinates(x, y, beyondarObjects)
        return beyondarObjects
    }

    /**
     * Get the GeoObject that intersect with the coordinates x, y on the screen.
     *
     * @param x
     * X screen position.
     * @param y
     * Y screen position.
     * @param beyondarObjects
     * The output list where all the
     * [            BeyondarObject][BeyondarObject] that collide with the screen cord will be
     * stored.
     */
    fun getBeyondarObjectsOnScreenCoordinates(
        x: Float,
        y: Float,
        beyondarObjects: ArrayList<BeyondarObject?>?
    ) {
        gLSurfaceView!!.getBeyondarObjectsOnScreenCoordinates(x, y, beyondarObjects)
    }

    /**
     * Get the GeoObject that intersect with the coordinates x, y on the screen.
     *
     * @param x
     * screen position.
     * @param y
     * screen position.
     * @param beyondarObjects
     * The output list where all the
     * [            BeyondarObject][BeyondarObject] that collide with the screen cord will be
     * stored.
     * @param ray
     * The ray that will hold the direction of the screen coordinate.
     */
    fun getBeyondarObjectsOnScreenCoordinates(
        x: Float,
        y: Float,
        beyondarObjects: ArrayList<BeyondarObject?>?,
        ray: Ray?
    ) {
        gLSurfaceView!!.getBeyondarObjectsOnScreenCoordinates(x, y, beyondarObjects, ray)
    }
    /**
     * Get the distance which all the [ GeoObject][com.beyondar.android.world.GeoObject] will be rendered if the are farther that the returned distance.
     *
     * @return The current max distance. 0 is the default behavior.
     */
    /**
     * When a [GeoObject][com.beyondar.android.world.GeoObject] is rendered
     * according to its position it could look very small if it is far away. Use
     * this method to render far objects as if there were closer.<br></br>
     * For instance if there are objects farther than 50 meters and we want them
     * to be displayed as they where at 50 meters, we could use this method for
     * that purpose. <br></br>
     * To set it to the default behavior just set it to 0
     *
     * @param maxDistanceSize
     * The top far distance (in meters) which we want to draw a
     * [GeoObject][com.beyondar.android.world.GeoObject] , 0 to
     * set again the default behavior
     */
    var pullCloserDistance: Float
        get() = gLSurfaceView!!.pullCloserDistance
        set(maxDistanceSize) {
            gLSurfaceView!!.pullCloserDistance = maxDistanceSize
        }
    /**
     * Get the closest distance which all the
     * [GeoObject][com.beyondar.android.world.GeoObject] can be displayed.
     *
     * @return The current minimum distance. 0 is the default behavior.
     */
    /**
     * When a [GeoObject][com.beyondar.android.world.GeoObject] is rendered
     * according to its position it could look very big if it is too close. Use
     * this method to render near objects as if there were farther.<br></br>
     * For instance if there is an object at 1 meters and we want to have
     * everything at to look like if they where at least at 10 meters, we could
     * use this method for that purpose. <br></br>
     * To set it to the default behavior just set it to 0.
     *
     * @param minDistanceSize
     * The top near distance (in meters) which we want to draw a
     * [GeoObject][com.beyondar.android.world.GeoObject] , 0 to
     * set again the default behavior.
     */
    var pushAwayDistance: Float
        get() = gLSurfaceView!!.pushAwayDistance
        set(minDistanceSize) {
            gLSurfaceView!!.pushAwayDistance = minDistanceSize
        }
    /**
     * Get the distance (in meters) which the objects are being considered when
     * rendering.
     *
     * @return meters
     */
    /**
     * Set the distance (in meters) which the objects will be considered to render.
     *
     * @param meters to be rendered from the user.
     */
    var maxDistanceToRender: Float
        get() = gLSurfaceView!!.maxDistanceToRender
        set(meters) {
            gLSurfaceView!!.maxDistanceToRender = meters
        }
    /**
     * Get the distance factor.
     *
     * @return Distance factor
     */
    /**
     * Set the distance factor for rendering all the objects. As bigger the
     * factor the closer the objects.
     *
     */
    var distanceFactor: Float
        get() = gLSurfaceView!!.distanceFactor
        set(meters) {
            gLSurfaceView!!.distanceFactor = meters
        }
    /**
     * Take a screenshot of the beyondar fragment. The screenshot will contain
     * the camera and the AR world overlapped.
     *
     * @param listener
     * [            OnScreenshotListener][com.beyondar.android.screenshot.OnScreenshotListener] That will be notified when the
     * screenshot is ready.
     * @param options
     * Bitmap options.
     */
    //    public void takeScreenshot(OnScreenshotListener listener, BitmapFactory.Options options) {
    //        ScreenshotHelper.takeScreenshot(getCameraView(), getGLSurfaceView(), listener, options);
    //    }
    /**
     * Take a screenshot of the beyondar fragment. The screenshot will contain
     * the camera and the AR world overlapped.
     *
     * @param listener
     * [            OnScreenshotListener][com.beyondar.android.screenshot.OnScreenshotListener] That will be notified when the
     * screenshot is ready.
     */
    //    public void takeScreenshot(OnScreenshotListener listener) {
    //        BitmapFactory.Options options = new BitmapFactory.Options();
    //        // TODO: Improve this part
    //        options.inSampleSize = 4;
    //        // options.inSampleSize = 1;
    //        takeScreenshot(listener, options);
    //    }
    /**
     * Show the number of frames per second in the left upper corner. False by
     * default.
     *
     * @param show
     * True to show the FPS, false otherwise.
     */
    fun showFPS(show: Boolean) {
        if (show) {
            if (mFpsTextView == null) {
                mFpsTextView = TextView(activity)
                mFpsTextView!!.setBackgroundResource(R.color.black)
                mFpsTextView!!.setTextColor(resources.getColor(R.color.white))
                val params = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                mMainLayout!!.addView(mFpsTextView, params)
            }
            mFpsTextView!!.visibility = View.VISIBLE
            setFpsUpdatable(this)
        } else if (mFpsTextView != null) {
            mFpsTextView!!.visibility = View.GONE
            setFpsUpdatable(null)
        }
    }

    override fun onFpsUpdate(fps: Float) {
        if (mFpsTextView != null) {
            mFpsTextView!!.post { mFpsTextView!!.text = "fps: $fps" }
        }
    }

    /**
     * Set the adapter to draw the views on top of the AR View.
     *
     * @param adapter
     */
    fun setBeyondarViewAdapter(adapter: BeyondarViewAdapter?) {
        gLSurfaceView!!.setBeyondarViewAdapter(adapter, mMainLayout)
    }

    /**
     * Use this method to fill all the screen positions of the
     * [BeyondarObject] when a
     * object is rendered. Remember that the information is filled when the
     * object is rendered, so it is asynchronous.<br></br>
     *
     * After this method is called you can use the following:<br></br>
     * [ BeyondarObject.getScreenPositionBottomLeft()][BeyondarObject]<br></br>
     * [ BeyondarObject.getScreenPositionBottomRight()][BeyondarObject]<br></br>
     * [ BeyondarObject.getScreenPositionTopLeft()][BeyondarObject]<br></br>
     * [ BeyondarObject.getScreenPositionTopRight()][BeyondarObject]
     *
     * __Important__ Enabling this feature will reduce the FPS, use only when is
     * needed.
     *
     * @param fill
     * Enable or disable this feature.
     */
    fun forceFillBeyondarObjectPositionsOnRendering(fill: Boolean) {
        gLSurfaceView!!.forceFillBeyondarObjectPositionsOnRendering(fill)
    }

    /**
     * Use this method to fill all the screen positions of the
     * [BeyondarObject]. After
     * this method is called you can use the following:<br></br>
     * [ BeyondarObject.getScreenPositionBottomLeft()][BeyondarObject]<br></br>
     * [ BeyondarObject.getScreenPositionBottomRight()][BeyondarObject]<br></br>
     * [ BeyondarObject.getScreenPositionTopLeft()][BeyondarObject]<br></br>
     * [ BeyondarObject.getScreenPositionTopRight()][BeyondarObject]
     *
     * @param beyondarObject
     * The [            BeyondarObject][BeyondarObject] to compute
     */
    fun fillBeyondarObjectPositions(beyondarObject: BeyondarObject?) {
        gLSurfaceView!!.fillBeyondarObjectPositions(beyondarObject)
    }

    /**
     * Use setPullCloserDistance instead.
     */
    @Deprecated("")
    fun setMaxFarDistance(maxDistanceSize: Float) {
        pullCloserDistance = maxDistanceSize
    }

    /**
     * Use getPushFrontDistance instead.
     */
    @get:Deprecated("")
    val maxDistanceSize: Float
        get() = pullCloserDistance

    /**
     * Use setPushAwayDistance instead.
     */
    @Deprecated("")
    fun setMinFarDistanceSize(minDistanceSize: Float) {
        pushAwayDistance = minDistanceSize
    }

    /**
     * Use getPushAwayDistance instead.
     */
    @get:Deprecated("")
    val minDistanceSize: Float
        get() = pushAwayDistance

    companion object {
        /**
         * Support fragment class that displays and control the
         * [CameraView][com.beyondar.android.view.CameraView] and the
         * [BeyondarGLSurfaceView][com.beyondar.android.view.BeyondarGLSurfaceView]
         * . It also provide a set of utilities to control the usage of the augmented
         * reality world.
         *
         */
        private val CORE_POOL_SIZE = 1
        private val MAXIMUM_POOL_SIZE = 1
        private val KEEP_ALIVE_TIME: Long =
            1000 // 1000 ms// Camera is not available (in use or does not exist)
        // returns null if camera is unavailable
// attempt to get a Camera instance
        /** A safe way to get an instance of the Camera object.  */
        val cameraInstance: Camera?
            get() {
                var c: Camera? = null
                try {
                    c = Camera.open() // attempt to get a Camera instance
                } catch (e: Exception) {
                    Log.d("Main Activity", "getCameraInstance: ERROR" + e.message)
                    // Camera is not available (in use or does not exist)
                }
                return c // returns null if camera is unavailable
            }
    }
}