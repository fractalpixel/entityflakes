package org.entityflakes.system

import org.entityflakes.World
import org.kwrench.time.Time
import org.kwrench.updating.Updating
import org.kwrench.updating.strategies.UpdateStrategy
import org.kwrench.updating.strategies.VariableTimestepStrategy

/**
 * Base class for Systems.
 * Contains logic for keeping track of system state (initialized/disposed), and an [UpdateStrategy] filtering calls to [doUpdate].
 *
 * @param updateStrategy how often to run the update function of this system.  By default calls doUpdate each frame.
 */
abstract class SystemBase(var updateStrategy: UpdateStrategy = VariableTimestepStrategy()) : System {

    private lateinit var world_ : World

    // System initialization state
    private var initDone: Boolean = false
    private var disposeCalled: Boolean = false

    final override val active: Boolean get() = initDone && !disposeCalled
    final override val initialized: Boolean get() = initDone
    final override val disposed: Boolean get() = disposeCalled

    // Updating object that delegates to doUpdate()
    private val localUpdate: Updating = object: Updating {
        override fun update(time: Time) {
            doUpdate(time)
        }
    }

    /**
     * The world that this system has been added to.
     * Throws an exception if attempted to access before the system has been added to the world.
     */
    val world: World get() = world_

    override fun onAddedToWorld(world: World) {
        world_ = world
    }

    final override fun init(world: World) {
        doInit(world)
        initDone = true
    }

    final override fun dispose() {
        disposeCalled = true
        doDispose()
    }

    final override fun update(time: Time) {
        // Use the strategy to update (may call doUpdate zero or more times)
        updateStrategy.update(localUpdate, time)
    }

    /**
     * Called when the system is initialized.  Do any initialization.
     */
    protected open fun doInit(world: World) {
    }

    /**
     * Called each time the system is updated by its update strategy (may be more or less than once per frame).
     */
    protected open fun doUpdate(time: Time) {
    }

    /**
     * Called when the system is disposed.  Do any cleanup.
     */
    protected open fun doDispose() {
    }

}