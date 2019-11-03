package org.entityflakes.system

import org.entityflakes.Disposable
import org.entityflakes.World
import org.kwrench.updating.Updating

/**
 * Handles some system or process in the world.
 */
interface System: Updating, Disposable {

    /**
     * Called when the system is added to a world (happens before it is initialized).
     */
    fun onAddedToWorld(world: World)

    /**
     * Called when the system starts up.
     */
    fun init(world: World)

    /**
     * The category to use for this system type when getting it from the World by type.
     * Allows multiple systems to implement the same interface and be mutually exclusive services.
     */
    val typeCategory: Class<out System> get() = this.javaClass

    /**
     * True if this system has been initialized and not yet disposed.
     */
    val active: Boolean

    /**
     * True if this system has been initialized (but might have been disposed)
     */
    val initialized: Boolean

    /**
     * True if this system has been disposed
     */
    val disposed: Boolean

}