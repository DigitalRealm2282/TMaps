/*
 * Copyright (C) 2014 BeyondAR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tdi.tmaps.ar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.Gravity
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.RemoteViews.RemoteView

/**
 * Custom layout for to be able to attach a View to a
 * [BeyondarObject][com.beyondar.android.world.BeyondarObject]
 *
 * @author jpuig
 */
@SuppressLint("NewApi")
@RemoteView
internal class CustomLayout(context: Context?) : ViewGroup(context) {
    /** The amount of space used by children in the left gutter.  */
    private var mLeftWidth = 0

    /** The amount of space used by children in the right gutter.  */
    private var mRightWidth = 0

    /** These are used for computing child frames based on their gravity.  */
    private val mTmpContainerRect = Rect()
    private val mTmpChildRect = Rect()
    private var xPos = 0
    private var yPos = 0

    /**
     * Any layout manager that doesn't scroll will want this.
     */
    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    fun setPosition(x: Int, y: Int) {
        // If the android version is >= 3.0, use the existing way
        if (Build.VERSION.SDK_INT >= 11) {
            translationX = x.toFloat()
            translationY = y.toFloat()
            return
        }
        if (xPos == x && yPos == y) {
            return
        }
        xPos = x
        yPos = y
        invalidate()
    }

    /**
     * Ask all children to measure themselves and compute the measurement of
     * this layout based on the children.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val count = childCount

        // These keep track of the space we are using on the left and right for
        // views positioned there; we need member variables so we can also use
        // these for layout later.
        mLeftWidth = 0
        mRightWidth = 0

        // Measurement will ultimately be computing these values.
        var maxHeight = 0
        var maxWidth = 0
        var childState = 0

        // Iterate through all children, measuring them and computing our
        // dimensions from their size.
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                // Measure the child.
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)

                // Update our size information based on the layout params.
                // Children that asked to be positioned on the left or right go
                // in those gutters.
                val lp = child.layoutParams as LayoutParams
                if (lp.position == LayoutParams.POSITION_LEFT) {
                    mLeftWidth += Math.max(
                        maxWidth,
                        child.measuredWidth + lp.leftMargin +
                            lp.rightMargin
                    )
                } else if (lp.position == LayoutParams.POSITION_RIGHT) {
                    mRightWidth += Math.max(
                        maxWidth,
                        child.measuredWidth + lp.leftMargin +
                            lp.rightMargin
                    )
                } else {
                    maxWidth =
                        Math.max(maxWidth, child.measuredWidth + lp.leftMargin + lp.rightMargin)
                }
                maxHeight =
                    Math.max(maxHeight, child.measuredHeight + lp.topMargin + lp.bottomMargin)
                if (Build.VERSION.SDK_INT >= 11) {
                    childState = combineMeasuredStates(childState, child.measuredState)
                } else {
                    supportCombineMeasuredStates(childState, 0)
                }
            }
        }

        // Total width is the maximum width of all inner children plus the
        // gutters.
        maxWidth += mLeftWidth + mRightWidth

        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, suggestedMinimumHeight)
        maxWidth = Math.max(maxWidth, suggestedMinimumWidth)

        // maxWidth += 50;

        // Report our final dimensions.
        if (Build.VERSION.SDK_INT >= 11) {
            setMeasuredDimension(
                resolveSizeAndState(maxWidth, widthMeasureSpec, childState) + xPos,
                resolveSizeAndState(
                    maxHeight, heightMeasureSpec,
                    childState shl MEASURED_HEIGHT_STATE_SHIFT
                ) + yPos
            )
        } else {
            setMeasuredDimension(
                resolveSizeAndStateSupport(maxWidth, widthMeasureSpec, childState) + xPos,
                resolveSizeAndStateSupport(
                    maxHeight, heightMeasureSpec,
                    childState shl MEASURED_HEIGHT_STATE_SHIFT
                ) + yPos
            )
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val count = childCount

        // These are the far left and right edges in which we are performing
        // layout.
        var leftPos = paddingLeft
        var rightPos = right - left - paddingRight

        // This is the middle region inside of the gutter.
        val middleLeft = leftPos + mLeftWidth
        val middleRight = rightPos - mRightWidth

        // These are the top and bottom edges in which we are performing layout.
        val parentTop = paddingTop
        val parentBottom = bottom - top - paddingBottom
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val lp = child.layoutParams as LayoutParams
                val width = child.measuredWidth
                val height = child.measuredHeight

                // Compute the frame in which we are placing this child.
                if (lp.position == LayoutParams.POSITION_LEFT) {
                    mTmpContainerRect.left = leftPos + lp.leftMargin
                    mTmpContainerRect.right = leftPos + width + lp.rightMargin
                    leftPos = mTmpContainerRect.right
                } else if (lp.position == LayoutParams.POSITION_RIGHT) {
                    mTmpContainerRect.right = rightPos - lp.rightMargin
                    mTmpContainerRect.left = rightPos - width - lp.leftMargin
                    rightPos = mTmpContainerRect.left
                } else {
                    mTmpContainerRect.left = middleLeft + lp.leftMargin
                    mTmpContainerRect.right = middleRight - lp.rightMargin
                }
                mTmpContainerRect.top = parentTop + lp.topMargin
                mTmpContainerRect.bottom = parentBottom - lp.bottomMargin

                // Use the child's gravity and size to determine its final
                // frame within its container.
                Gravity.apply(lp.gravity, width, height, mTmpContainerRect, mTmpChildRect)

                // Place the child.
                child.layout(
                    mTmpChildRect.left + xPos, mTmpChildRect.top + yPos, mTmpChildRect.right + xPos,
                    mTmpChildRect.bottom + yPos
                )
            }
        }
    }

    // ----------------------------------------------------------------------
    // The rest of the implementation is for custom per-child layout parameters.
    // If you do not need these (for example you are writing a layout manager
    // that does fixed positioning of its children), you can drop all of this.
    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return LayoutParams(p)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams
    }

    /**
     * Custom per-child layout information.
     */
    internal class LayoutParams : MarginLayoutParams {
        /**
         * The gravity to apply with the View to which these layout parameters
         * are associated.
         */
        var gravity = Gravity.TOP or Gravity.START
        var position = POSITION_MIDDLE

        constructor(width: Int, height: Int) : super(width, height) {}
        constructor(source: ViewGroup.LayoutParams?) : super(source) {}

        companion object {
            var POSITION_MIDDLE = 0
            var POSITION_LEFT = 1
            var POSITION_RIGHT = 2
        }
    }

    companion object {
        fun resolveSizeAndStateSupport(size: Int, measureSpec: Int, childMeasuredState: Int): Int {
            var result = size
            val specMode = MeasureSpec.getMode(measureSpec)
            val specSize = MeasureSpec.getSize(measureSpec)
            when (specMode) {
                MeasureSpec.UNSPECIFIED -> result = size
                MeasureSpec.AT_MOST -> result = if (specSize < size) {
                    specSize or MEASURED_STATE_TOO_SMALL
                } else {
                    size
                }
                MeasureSpec.EXACTLY -> result = specSize
            }
            return result or (childMeasuredState and MEASURED_STATE_MASK)
        }

        private fun supportCombineMeasuredStates(curState: Int, newState: Int): Int {
            return curState or newState
        }
    }
}