package com.tdi.tmaps.ar

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.beyondar.android.util.math.geom.Point2
import com.beyondar.android.world.BeyondarObject
import java.util.*

/**
 * Adapter to attach views to the
 * [BeyondarObject]. This is an
 * example of how to use the adapter:
 *
 * <pre>
 * `
 * private class CustomBeyondarViewAdapter extends BeyondarViewAdapter {
 *
 * LayoutInflater inflater;
 *
 * public CustomBeyondarViewAdapter(Context context) {
 * super(context);
 * inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 * }
 *
 * @Override
 * public View getView(BeyondarObject beyondarObject, View recycledView, ViewGroup parent) {
 * if (!showViewOn.contains(beyondarObject)) {
 * return null;
 * }
 * if (recycledView == null) {
 * recycledView = inflater.inflate(R.layout.beyondar_object_view, null);
 * }
 *
 * TextView textView = (TextView) recycledView.findViewById(R.id.titleTextView);
 * textView.setText(beyondarObject.getName());
 * Button button = (Button) recycledView.findViewById(R.id.button);
 * button.setOnClickListener(AttachViewToGeoObjectActivity.this);
 *
 * // Once the view is ready we specify the position
 * setPosition(beyondarObject.getScreenPositionTopRight());
 *
 * return recycledView;
 * }
 * }
` *
</pre> *
 *
 * Then when the adapter is ready we can set it in the
 * [BeyondarFragment][com.beyondar.android.fragment.BeyondarFragment]:
 *
 * `
 * <pre>
 * CustomBeyondarViewAdapter customBeyondarViewAdapter = new CustomBeyondarViewAdapter(this);
 * mBeyondarFragment.setBeyondarViewAdapter(customBeyondarViewAdapter);
</pre>` *
 */
abstract class BeyondarViewAdapter(context: Context) {
    var mReusedViews: Queue<ViewGroup?>
    var mNewViews: Queue<ViewGroup?>
    var mParentView: ViewGroup? = null
    var mNewPosition: Point2? = null

    /**
     * Get [Context].
     *
     * @return
     */
    protected var context: Context
    val mLayoutParams: ViewGroup.LayoutParams

    init {
        mReusedViews = LinkedList()
        mNewViews = LinkedList()
        this.context = context
        mLayoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    fun processList(
        list: List<BeyondarObject>,
        parent: ViewGroup?,
        glSurface: ArBeyondarGLSurfaceView
    ) {
        mParentView = parent
        mParentView!!.post(object : Runnable {
            override fun run() {
                for (beyondarObject in list) {
                    if (beyondarObject.screenPositionCenter.z > 1) {
                        continue
                    }
                    var recycledParent = mReusedViews.poll() as CustomLayout?
                    glSurface.fillBeyondarObjectPositions(beyondarObject)
                    var toRecycle: View? = null
                    if (recycledParent != null && recycledParent.childCount > 0) {
                        toRecycle = recycledParent.getChildAt(0)
                    }
                    val view = getView(beyondarObject, toRecycle, mParentView)
                    var added = false
                    // Check if the recyclable view has been used, otherwise add
                    // it to the queue to recycle it
                    if ((toRecycle !== view || view == null) && toRecycle != null) {
                        // Store it again to recycle it
                        mReusedViews.add(recycledParent)
                        added = true
                    }

                    // Check if the view has a parent, if not create it
                    if (view != null && (recycledParent == null || view.parent !== recycledParent)) {
                        val parentLayout = CustomLayout(
                            context
                        )
                        parentLayout.addView(view, mLayoutParams)
                        if (!added) {
                            mReusedViews.add(recycledParent)
                        }
                        recycledParent = parentLayout
                    }
                    if (view != null) {
                        mNewViews.add(recycledParent)
                        if (recycledParent!!.parent == null) {
                            mParentView!!.addView(recycledParent, mLayoutParams)
                        }
                        recycledParent.setPosition(
                            mNewPosition!!.x.toInt(),
                            mNewPosition!!.y.toInt()
                        )
                    }
                }
                removeUnusedViews()
                val tmp = mNewViews
                mNewViews = mReusedViews
                mReusedViews = tmp
                mNewPosition = null
            }
        })
    }

    /**
     * Set the screen position of the view. When the view is created use this
     * method to specify the position on the screen.
     *
     *
     * @param position
     */
    protected fun setPosition(position: Point2?) {
        mNewPosition = position
    }

    private fun removeUnusedViews() {
        while (!mReusedViews.isEmpty()) {
            val view: View? = mReusedViews.poll()
            mParentView!!.removeView(view)
        }
    }

    /**
     * Override this method to create your own views from the
     * [BeyondarObject]. The
     * usage of this adapter is very similar to the [ListAdapter].
     *
     * <pre>
     * `
     * private class CustomBeyondarViewAdapter extends BeyondarViewAdapter {
     *
     * LayoutInflater inflater;
     *
     * public CustomBeyondarViewAdapter(Context context) {
     * super(context);
     * inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     * }
     *
     * @Override
     * public View getView(BeyondarObject beyondarObject, View recycledView, ViewGroup parent) {
     * if (!showViewOn.contains(beyondarObject)) {
     * return null;
     * }
     * if (recycledView == null) {
     * recycledView = inflater.inflate(R.layout.beyondar_object_view, null);
     * }
     *
     * TextView textView = (TextView) recycledView.findViewById(R.id.titleTextView);
     * textView.setText(beyondarObject.getName());
     * Button button = (Button) recycledView.findViewById(R.id.button);
     * button.setOnClickListener(AttachViewToGeoObjectActivity.this);
     *
     * // Once the view is ready we specify the position
     * setPosition(beyondarObject.getScreenPositionTopRight());
     *
     * return recycledView;
     * }
     * }
     ` *
     </pre> *
     *
     * @param beyondarObject
     * @param recycledView
     * @param parent
     * @return
     */
    abstract fun getView(
        beyondarObject: BeyondarObject?,
        recycledView: View?,
        parent: ViewGroup?
    ): View?
}