package com.tdi.tmaps.ar

import android.content.Context
import android.graphics.PixelFormat
import android.hardware.SensorManager
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import com.beyondar.android.opengl.renderer.ARRenderer
import com.beyondar.android.opengl.renderer.ARRenderer.FpsUpdatable
import com.beyondar.android.opengl.renderer.ARRenderer.GLSnapshotCallback
import com.beyondar.android.opengl.renderer.OnBeyondarObjectRenderedListener
import com.beyondar.android.opengl.util.MatrixTrackingGL
import com.beyondar.android.sensor.BeyondarSensorManager
import com.beyondar.android.util.Logger
import com.beyondar.android.util.math.geom.Ray
import com.beyondar.android.world.BeyondarObject
import com.beyondar.android.world.World

// GL View to draw the [World] using the
// [ARRenderer]

open class ArBeyondarGLSurfaceView : GLSurfaceView, OnBeyondarObjectRenderedListener {
    private var mRenderer: ARRenderer? = null
    private var mViewAdapter: BeyondarViewAdapter? = null
    private var mParent: ViewGroup? = null
    private var mWorld: World? = null
    private var mSensorDelay = 0

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        mSensorDelay = SensorManager.SENSOR_DELAY_UI
        if (Logger.DEBUG_OPENGL) {
            debugFlags = DEBUG_CHECK_GL_ERROR or DEBUG_LOG_GL_CALLS
        }

        // Wrapper set so the renderer can
        // access the gl transformation matrixes.
        setGLWrapper { gl -> MatrixTrackingGL(gl) }
        mRenderer = createRenderer()
        mRenderer!!.setOnBeyondarObjectRenderedListener(this)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        setRenderer(mRenderer)
        requestFocus()
        // This call will allow the GLSurface to be on the top of all the
        // Surfaces. It is needed because when the camera is rotated the camera
        // tend to overlap the GLSurface.
        setZOrderMediaOverlay(true)
        isFocusableInTouchMode = true
    }

    /**
     * Take an snapshot of the view. The callback will be notified when the
     * picture is ready.
     *
     */
    fun tackePicture(callBack: GLSnapshotCallback?) {
        mRenderer!!.tackePicture(callBack)
    }

    /**
     * Override this method to change the renderer. For instance:<br></br>
     * `return new CustomARRenderer();`<br></br>
     *
     */
    private fun createRenderer(): ARRenderer {
        return ARRenderer()
    }

    /**
     * Set the
     * [ FpsUpdatable][FpsUpdatable] to get notified about the frames per seconds.
     *
     * @param fpsUpdatable
     * The event listener. Use null to remove the
     * [            FpsUpdatable][FpsUpdatable]
     */
    fun setFpsUpdatable(fpsUpdatable: FpsUpdatable?) {
        mRenderer!!.setFpsUpdatable(fpsUpdatable)
    }

    override fun setVisibility(visibility: Int) {
        if (visibility == VISIBLE) {
            mRenderer!!.isRendering = true
        } else {
            mRenderer!!.isRendering = false
        }
        super.setVisibility(visibility)
    }
    /**
     * Get the current sensor delay. See [SensorManager]
     * for more information
     *
     * @return sensor delay
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
     * You can find more information in the
     * [SensorManager] class
     *
     *
     * @param delay
     */
    var sensorDelay: Int
        get() = mSensorDelay
        set(delay) {
            mSensorDelay = delay
            unregisterSensorListener()
            registerSensorListener(mSensorDelay)
        }

    /**
     * Define the world where the objects are stored.
     *
     * @param world
     */
    fun setWorld(world: World?) {
        if (null == mWorld) { // first time
            unregisterSensorListener()
            registerSensorListener(mSensorDelay)
        }
        mWorld = world
        mRenderer!!.world = world
    }

    private fun unregisterSensorListener() {
        BeyondarSensorManager.unregisterSensorListener(mRenderer)
    }

    private fun registerSensorListener(sensorDealy: Int) {
        BeyondarSensorManager.registerSensorListener(mRenderer)
    }

    override fun onPause() {
        unregisterSensorListener()
        super.onPause()
        mRenderer!!.onPause()
    }

    override fun onResume() {
        super.onResume()
        registerSensorListener(mSensorDelay)
        if (mRenderer != null) {
            val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                .defaultDisplay
            mRenderer!!.rotateView(display.rotation)
            mRenderer!!.onResume()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (mWorld == null || event == null) {
            false
        } else false
    }

    /**
     * Get the GeoObject that intersect with the coordinates x, y on the screen
     *
     * @param x
     * @param y
     * @param beyondarObjects
     * The output list to place all the
     * [            BeyondarObject][BeyondarObject] that collide with the screen cord
     * @return
     */
    @Synchronized
    fun getBeyondarObjectsOnScreenCoordinates(
        x: Float,
        y: Float,
        beyondarObjects: ArrayList<BeyondarObject?>?
    ) {
        getBeyondarObjectsOnScreenCoordinates(x, y, beyondarObjects, sRay)
    }

    /**
     * Get the GeoObject that intersect with the coordinates x, y on the screen
     *
     * @param x
     * @param y
     * @param beyondarObjects
     * The output list to place all the
     * [            BeyondarObject][BeyondarObject] that collide with the screen cord
     * @param ray
     * The ray that will hold the direction of the screen coordinate
     * @return
     */
    @Synchronized
    fun getBeyondarObjectsOnScreenCoordinates(
        x: Float,
        y: Float,
        beyondarObjects: ArrayList<BeyondarObject?>?,
        ray: Ray?
    ) {
        mRenderer!!.getViewRay(x, y, ray)
        mWorld!!.getBeyondarObjectsCollideRay(ray, beyondarObjects, maxDistanceToRender)
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
        get() = mRenderer!!.pullCloserDistance
        set(maxDistanceSize) {
            mRenderer!!.pullCloserDistance = maxDistanceSize
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
        get() = mRenderer!!.pushAwayDistance
        set(minDistanceSize) {
            mRenderer!!.pushAwayDistance = minDistanceSize
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
        get() = mRenderer!!.maxDistanceToRender
        set(meters) {
            mRenderer!!.maxDistanceToRender = meters
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
        get() = mRenderer!!.distanceFactor
        set(meters) {
            mRenderer!!.distanceFactor = meters
        }

    fun setBeyondarViewAdapter(beyondarViewAdapter: BeyondarViewAdapter?, parent: ViewGroup?) {
        mViewAdapter = beyondarViewAdapter
        mParent = parent
    }

    override fun onBeyondarObjectsRendered(renderedBeyondarObjects: List<BeyondarObject>) {
        val tmpView = mViewAdapter
        if (tmpView != null) {
            val elements = World
                .sortGeoObjectByDistanceFromCenter(ArrayList(renderedBeyondarObjects))
            tmpView.processList(elements, mParent, this)
        }
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
        mRenderer!!.forceFillBeyondarObjectPositions(fill)
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
        mRenderer!!.fillBeyondarObjectScreenPositions(beyondarObject)
    }

    companion object {
        private val sRay = Ray(0F, 0F, 0F)
    }
}