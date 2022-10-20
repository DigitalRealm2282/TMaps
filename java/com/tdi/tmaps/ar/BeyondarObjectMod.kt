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

import com.beyondar.android.opengl.colision.MeshCollider
import com.beyondar.android.opengl.colision.SquareMeshCollider
import com.beyondar.android.opengl.renderable.Renderable
import com.beyondar.android.opengl.renderable.SquareRenderable
import com.beyondar.android.opengl.texture.Texture
import com.beyondar.android.plugin.BeyondarObjectPlugin
import com.beyondar.android.plugin.Plugable
import com.beyondar.android.util.cache.BitmapCache
import com.beyondar.android.util.math.geom.Point3
import java.util.ArrayList

/**
 * Basic object to be used with augmented reality. This class contains all the
 * needed information to be used in the AR [World].
 */
class BeyondarObjectMod : Plugable<BeyondarObjectPlugin?> {
    var placeId: String? = null
        private set
    private var mId: Long? = null
    /**
     * Get the list type of the
     * [BeyondarObject][BeyondarObjectMod].
     *
     * @return The list type.
     */
    /**
     * Set the list type of the this object. This type is configured once the
     * object is added to the World.
     *
     * @param worldListType
     */
    var worldListType = 0
    private var mTexture: Texture? = null

    /**
     * The the image uri used to represent the
     * [BeyondarObject][BeyondarObjectMod]
     *
     * @return
     */
    var imageUri: String? = null
        private set

    /**
     * Get the name of the [ BeyondarObject][BeyondarObjectMod].
     *
     * @return The name of the [         BeyondarObject][BeyondarObjectMod].
     */
    var name: String? = null
        private set
    private var mVisible = false
    private var mRenderable: Renderable? = null

    /**
     * Get the position where the
     * [BeyondarObject][BeyondarObjectMod] is being
     * rendered.
     *
     * @return The 3D position.
     */
    var position: Point3? = null
        private set

    /**
     * Get the used angle for rendering the
     * [BeyondarObject][BeyondarObjectMod].
     *
     * @return The angle in degrees.
     */
    var angle: Point3? = null
        private set

    /**
     * Check if the [ BeyondarObject][BeyondarObjectMod] is facing the camera.
     *
     * @return True if it is facing.
     */
    var isFacingToCamera = false
        private set
    private var mMeshCollider: MeshCollider? = null
    /**
     * Get the Distance from the user in meters.
     *
     * @return Distance in meters.
     */
    /**
     * Set how far is the object from the user (meters).
     *
     * This method is used by the [ARRenderer] to set this value.
     *
     * @param distance
     * Distance in meters.
     */
    var distanceFromUser = 0.0

    /**
     * Get the top left screen position of the
     * [BeyondarObject][BeyondarObjectMod] on the
     * screen. use the Z axis to check if the object is in front (z<1) or behind
     * (z>1) the screen.
     *
     * @return top left screen position.
     */
    var screenPositionTopLeft: Point3? = null
        private set

    /**
     * Get the top right screen position of the
     * [BeyondarObject][BeyondarObjectMod] on the
     * screen. use the Z axis to check if the object is in front (z<1) or behind
     * (z>1) the screen.
     *
     * @return Top right screen position.
     */
    var screenPositionTopRight: Point3? = null
        private set

    /**
     * Get the bottom left screen position of the
     * [BeyondarObject][BeyondarObjectMod] on the
     * screen. use the Z axis to check if the object is in front (z<1) or behind
     * (z>1) the screen.
     *
     * @return Bottom left screen position.
     */
    var screenPositionBottomLeft: Point3? = null
        private set

    /**
     * Get the bottom right screen position of the
     * [BeyondarObject][BeyondarObjectMod] on the
     * screen. use the Z axis to check if the object is in front (z<1) or behind
     * (z>1) the screen.
     *
     * @return Bottom right screen position.
     */
    var screenPositionBottomRight: Point3? = null
        private set

    /**
     * Get the center screen position of the
     * [BeyondarObject][BeyondarObjectMod] on the
     * screen. use the Z axis to check if the object is in front (z<1) or behind
     * (z>1) the screen.
     *
     * @return Center screen position.
     */
    var screenPositionCenter: Point3? = null
        private set
    private var mTopLeft: Point3? = null
    private var mBottomLeft: Point3? = null
    private var mBottomRight: Point3? = null
    private var mTopRight: Point3? = null

    /** This fields contains all the loaded plugins.  */
    protected var plugins: MutableList<BeyondarObjectPlugin?>? = null

    /** Use this lock to access the plugins field.  */
    protected var lockPlugins = Any()

    /**
     * Create an instance of a [ BeyondarObject][BeyondarObjectMod] with an unique ID
     *
     * @param id
     * Unique ID
     */
    constructor(id: Long) {
        mId = id
        init()
    }

    constructor(id: Long, placeId: String?) {
        mId = id
        this.placeId = placeId
        init()
    }

    /**
     * Create an instance of a [ BeyondarObject][BeyondarObjectMod] with an unique ID. The hash of the object will be used as
     * the [BeyondarObject][BeyondarObjectMod]
     * unique id.
     */
    constructor() {
        init()
    }

    private fun init() {
        plugins = ArrayList(Plugable.DEFAULT_PLUGINS_CAPACITY)
        position = Point3()
        angle = Point3()
        mTexture = Texture()
        faceToCamera(true)
        isVisible = true
        mTopLeft = Point3()
        mBottomLeft = Point3()
        mBottomRight = Point3()
        mTopRight = Point3()
        screenPositionTopLeft = Point3()
        screenPositionTopRight = Point3()
        screenPositionBottomLeft = Point3()
        screenPositionBottomRight = Point3()
        screenPositionCenter = Point3()
    }

    /**
     * Get the unique id of the
     * [BeyondarObject][BeyondarObjectMod].
     */
    val id: Long
        get() {
            if (mId == null) {
                mId = hashCode().toLong()
            }
            return mId!!.toLong()
        }

    override fun addPlugin(plugin: BeyondarObjectPlugin?) {
        synchronized(lockPlugins) {
            if (plugins!!.contains(plugin)) {
                return
            }
            plugins!!.add(plugin)
        }
    }

    override fun removePlugin(plugin: BeyondarObjectPlugin?): Boolean {
        var removed = false
        synchronized(lockPlugins) { removed = plugins!!.remove(plugin) }
        if (removed) {
            plugin!!.onDetached()
        }
        return removed
    }

    override fun removeAllPlugins() {
        synchronized(lockPlugins) { plugins!!.clear() }
    }

    override fun getFirstPlugin(pluginClass: Class<out BeyondarObjectPlugin?>): BeyondarObjectPlugin? {
        synchronized(lockPlugins) {
            for (plugin in plugins!!) {
                if (pluginClass.isInstance(plugin)) {
                    return plugin
                }
            }
        }
        return null
    }

    override fun containsAnyPlugin(pluginClass: Class<out BeyondarObjectPlugin?>): Boolean {
        return getFirstPlugin(pluginClass) != null
    }

    override fun containsPlugin(plugin: BeyondarObjectPlugin?): Boolean {
        synchronized(lockPlugins) { return plugins!!.contains(plugin) }
    }

    override fun getAllPugins(pluginClass: Class<out BeyondarObjectPlugin?>): List<BeyondarObjectPlugin?> {
        val result = ArrayList<BeyondarObjectPlugin?>(5)
        return getAllPlugins(pluginClass, result)
    }

    override fun getAllPlugins(
        pluginClass: Class<out BeyondarObjectPlugin?>,
        result: MutableList<BeyondarObjectPlugin?>
    ): List<BeyondarObjectPlugin?> {
        synchronized(lockPlugins) {
            for (plugin in plugins!!) {
                if (pluginClass.isInstance(plugin)) {
                    result.add(plugin)
                }
            }
        }
        return result
    }

    /**
     * Get a [List] copy of the added plugins. Adding/removing plugins to
     * this list will not affect the added plugins
     *
     * @return
     */
    override fun getAllPlugins(): List<BeyondarObjectPlugin?> {
        synchronized(lockPlugins) { return ArrayList(plugins) }
    }

    fun onRemoved() {
        synchronized(lockPlugins) {
            for (plugin in plugins!!) {
                plugin!!.onDetached()
            }
        }
    }

    /**
     * Set the used angle for rendering the
     * [BeyondarObject][BeyondarObjectMod].
     *
     * @param x
     * The angle in degrees for x.
     *
     * @param y
     * The angle in degrees for y.
     * @param z
     * The angle in degrees for z.
     */
    fun setAngle(x: Float, y: Float, z: Float) {
        if (angle!!.x == x && angle!!.y == y && angle!!.z == z) return
        angle!!.x = x
        angle!!.y = y
        angle!!.z = z
        synchronized(lockPlugins) {
            for (plugin in plugins!!) {
                plugin!!.onAngleChanged(angle)
            }
        }
    }

    /**
     * Set the used angle for rendering the
     * [BeyondarObject][BeyondarObjectMod].
     *
     * @param newAngle
     * The angle in degrees.
     */
    fun setAngle(newAngle: Point3) {
        if (newAngle === angle) return
        angle = newAngle
        synchronized(lockPlugins) {
            for (plugin in plugins!!) {
                plugin!!.onAngleChanged(angle)
            }
        }
    }

    /**
     * Get the position where the
     * [BeyondarObject][BeyondarObjectMod] is being
     * rendered.
     *
     * @param newPos
     * New position.
     */
    fun setPosition(newPos: Point3) {
        if (newPos === position) return
        position = newPos
        synchronized(lockPlugins) {
            for (plugin in plugins!!) {
                plugin!!.onPositionChanged(position)
            }
        }
    }

    fun setPosition(x: Float, y: Float, z: Float) {
        if (position!!.x == x && position!!.y == y && position!!.z == z) return
        position!!.x = x
        position!!.y = y
        position!!.z = z
        synchronized(lockPlugins) {
            for (plugin in plugins!!) {
                plugin!!.onPositionChanged(position)
            }
        }
    }

    /**
     * Override this method to change the default
     * [Renderable].
     *
     * @return The new [         Renderable][Renderable].
     */
    protected fun createRenderable(): Renderable {
        return SquareRenderable.getInstance()
    }
    /**
     * Get the [Texture] used
     * to render the [ BeyondarObject][BeyondarObjectMod].
     *
     * @return [Texture]
     * object in use.
     */
    /**
     * Set the [Texture] used
     * to render the [ BeyondarObject][BeyondarObjectMod].
     *
     */
    var texture: Texture?
        get() = mTexture
        set(texture) {
            var texture = texture
            if (texture === mTexture) {
                return
            }
            if (texture == null) {
                texture = Texture()
            }
            mTexture = texture
            synchronized(lockPlugins) {
                for (plugin in plugins!!) {
                    plugin!!.onTextureChanged(mTexture)
                }
            }
        }

    /**
     * Set the texture pointer of the
     * [BeyondarObject][BeyondarObjectMod].
     *
     * @param texturePointer
     * The new texture pointer.
     */
    fun setTexturePointer(texturePointer: Int) {
        if (texturePointer == mTexture!!.texturePointer) return
        mTexture!!.texturePointer = texturePointer
        synchronized(lockPlugins) {
            for (plugin in plugins!!) {
                plugin!!.onTextureChanged(mTexture)
            }
        }
    }

    /**
     * Get [Renderable]
     * that renders the [ BeyondarObject][BeyondarObjectMod]
     *
     * @return The [         Renderable][Renderable] used for rendering.
     */
    val openGLObject: Renderable?
        get() {
            if (null == mRenderable) {
                mRenderable = createRenderable()
            }
            return mRenderable
        }

    /**
     * Set a custom [ Renderable][Renderable] for the [ BeyondarObject][BeyondarObjectMod]
     *
     * @param renderable
     */
    fun setRenderable(renderable: Renderable) {
        if (renderable === mRenderable) return
        mRenderable = renderable
        synchronized(lockPlugins) {
            for (plugin in plugins!!) {
                plugin!!.onRenderableChanged(mRenderable)
            }
        }
    }

    /**
     * Define if the [ BeyondarObject][BeyondarObjectMod] should face the camera.
     *
     * @param faceToCamera
     * true if it should face the camera, false otherwise.
     */
    fun faceToCamera(faceToCamera: Boolean) {
        if (faceToCamera == isFacingToCamera) return
        isFacingToCamera = faceToCamera
        synchronized(lockPlugins) {
            for (plugin in plugins!!) {
                plugin!!.onFaceToCameraChanged(isFacingToCamera)
            }
        }
    }
    /**
     * Check the visibility of the
     * [BeyondarObject][BeyondarObjectMod].
     *
     * @return True if it is visible, false otherwise.
     */
    /**
     * Set the visibility of this object. if it is false, the engine will not
     * render it.
     *
     * @param visible
     * True to set it visible, false to don't render it.
     */
    var isVisible: Boolean
        get() = mVisible
        set(visible) {
            if (visible == mVisible) return
            mVisible = visible
            synchronized(lockPlugins) {
                for (plugin in plugins!!) {
                    plugin!!.onVisibilityChanged(mVisible)
                }
            }
        }

    /**
     * Set the name of the [ BeyondarObject][BeyondarObjectMod].
     *
     * @param name
     * Name of the [            BeyondarObject][BeyondarObjectMod].
     */
    fun setName(name: String) {
        if (name === this.name) return
        this.name = name
        synchronized(lockPlugins) {
            for (plugin in plugins!!) {
                plugin!!.onNameChanged(this.name)
            }
        }
    }

    /**
     * Set the image uri.
     *
     * @param uri
     * The image uri that represents the
     * [            BeyondarObject][BeyondarObjectMod].
     */
    fun setImageUri(uri: String) {
        if (uri === imageUri) return
        imageUri = uri
        synchronized(lockPlugins) {
            for (plugin in plugins!!) {
                plugin!!.onImageUriChanged(imageUri)
            }
        }
        texture = null
    }

    /**
     * Set an image resource for the
     * [BeyondarObject][BeyondarObjectMod].
     *
     * @param resId
     * The resource id.
     */
    fun setImageResource(resId: Int) {
        setImageUri(BitmapCache.generateUri(resId))
    }

    /**
     * Get the top left of the [ BeyondarObject][BeyondarObjectMod] on the 3D world.
     *
     * @return Top left 3D.
     */
    val topLeft: Point3?
        get() {
            mTopLeft!!.x = position!!.x + mTexture!!.vertices[3]
            mTopLeft!!.y = position!!.y + mTexture!!.vertices[4]
            mTopLeft!!.z = position!!.z + mTexture!!.vertices[5]
            mTopLeft!!.rotatePointDegrees_x(angle!!.x.toDouble(), position)
            mTopLeft!!.rotatePointDegrees_y(angle!!.y.toDouble(), position)
            mTopLeft!!.rotatePointDegrees_z(angle!!.z.toDouble(), position)
            return mTopLeft
        }

    /**
     * Get the bottom left of the
     * [BeyondarObject][BeyondarObjectMod] on the
     * 3D world.
     *
     * @return bottom left 3D.
     */
    val bottomLeft: Point3?
        get() {
            mBottomLeft!!.x = position!!.x + mTexture!!.vertices[0]
            mBottomLeft!!.y = position!!.y + mTexture!!.vertices[1]
            mBottomLeft!!.z = position!!.z + mTexture!!.vertices[2]
            mBottomLeft!!.rotatePointDegrees_x(angle!!.x.toDouble(), position)
            mBottomLeft!!.rotatePointDegrees_y(angle!!.y.toDouble(), position)
            mBottomLeft!!.rotatePointDegrees_z(angle!!.z.toDouble(), position)
            return mBottomLeft
        }

    /**
     * Get the bottom right of the
     * [BeyondarObject][BeyondarObjectMod] on the
     * 3D world.
     *
     * @return Bottom right 3D.
     */
    val bottomRight: Point3?
        get() {
            mBottomRight!!.x = position!!.x + mTexture!!.vertices[6]
            mBottomRight!!.y = position!!.y + mTexture!!.vertices[7]
            mBottomRight!!.z = position!!.z + mTexture!!.vertices[8]
            mBottomRight!!.rotatePointDegrees_x(angle!!.x.toDouble(), position)
            mBottomRight!!.rotatePointDegrees_y(angle!!.y.toDouble(), position)
            mBottomRight!!.rotatePointDegrees_z(angle!!.z.toDouble(), position)
            return mBottomRight
        }

    /**
     * Get the top right of the
     * [BeyondarObject][BeyondarObjectMod] on the
     * 3D world.
     *
     * @return Top right 3D.
     */
    val topRight: Point3?
        get() {
            mTopRight!!.x = position!!.x + mTexture!!.vertices[9]
            mTopRight!!.y = position!!.y + mTexture!!.vertices[10]
            mTopRight!!.z = position!!.z + mTexture!!.vertices[11]
            mTopRight!!.rotatePointDegrees_x(angle!!.x.toDouble(), position)
            mTopRight!!.rotatePointDegrees_y(angle!!.y.toDouble(), position)
            mTopRight!!.rotatePointDegrees_z(angle!!.z.toDouble(), position)
            return mTopRight
        } // TODO: Improve the mesh collider!!

    // Generate the collision detector
    /**
     * Get the [ MeshCollider][MeshCollider] of the [GeoObject][com.beyondar.android.world.GeoObject].
     *
     * @return Mesh collider.
     */
    val meshCollider: MeshCollider
        get() {
            // TODO: Improve the mesh collider!!
            val topLeft = topLeft
            val bottomLeft = bottomLeft
            val bottomRight = bottomRight
            val topRight = topRight

            // Generate the collision detector
            mMeshCollider = SquareMeshCollider(topLeft, bottomLeft, bottomRight, topRight)
            return mMeshCollider as SquareMeshCollider
        }
}