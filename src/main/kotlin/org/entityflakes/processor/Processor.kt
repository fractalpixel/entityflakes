package org.entityflakes.processor

import org.entityflakes.Disposable
import org.entityflakes.World
import org.kwrench.updating.Updating

/**
 * Handles some process in the world.
 */
interface Processor: Updating, Disposable {

    /**
     * Called when the processor is added to a world (happens before it is initialized).
     */
    fun onAddedToWorld(world: World)

    /**
     * Called when the processor starts up.
     */
    fun init(world: World)

    /**
     * The category to use for this processor type when getting it from the World by type.
     * Allows multiple processors to implement the same interface and be mutually exclusive services.
     */
    val typeCategory: Class<out Processor> get() = this.javaClass

    /**
     * True if this processor has been initialized and not yet disposed.
     */
    val active: Boolean

    /**
     * True if this processor has been initialized (but might have been disposed)
     */
    val initialized: Boolean

    /**
     * True if this processor has been disposed
     */
    val disposed: Boolean

}