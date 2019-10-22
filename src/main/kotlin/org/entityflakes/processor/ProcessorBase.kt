package org.entityflakes.processor

import org.entityflakes.World
import org.kwrench.time.Time

/**
 * Base class for Processors.
 * Does not have any update strategy, so update will be called each frame.
 */
abstract class ProcessorBase : Processor {

    private lateinit var world_ : World

    private var initDone: Boolean = false
    private var disposeCalled: Boolean = false

    override final val active: Boolean get() = initDone && !disposeCalled
    override final val initialized: Boolean get() = initDone
    override final val disposed: Boolean get() = disposeCalled

    /**
     * The world that this processor has been added to.  Throws an exception if attempted to access before init has been called.
     */
    val world: World get() = world_

    override fun onAddedToWorld(world: World) {
        world_ = world
    }

    override final fun init(world: World) {
        doInit(world)
        initDone = true
    }

    override final fun dispose() {
        disposeCalled = true
        doDispose()
    }

    /**
     * Update processor.
     */
    override fun update(time: Time) {
    }

    /**
     * Called when the processor is initialized.  Do any initialization.
     */
    protected open fun doInit(world: World) {
    }

    /**
     * Called when the processor is disposed.  Do any cleanup.
     */
    protected open fun doDispose() {
    }

}