package org.entityflakes.system

import org.entityflakes.World
import org.kwrench.time.Time
import org.kwrench.updating.strategies.UpdateStrategy
import org.kwrench.updating.strategies.VariableTimestepStrategy

/**
 * A simple processor implementation that takes an update strategy and an update function as a parameter.
 * The [updateFunction] gets called whenever the [updateStrategy] indicates, by default once per world update.
 *
 * If you want a system for regularly updating some type of entities use [SimpleEntitySystem] instead.
 *
 * @param updateFunction the function to run each update.  Gets the world that the processor is in, and a time object, as parameters.
 * @param updateStrategy how often to run the update function of this processor.  By default calls the update function once per world update.
 */
class SimpleSystem(updateStrategy: UpdateStrategy = VariableTimestepStrategy(),
                   val updateFunction: (World, Time) -> Unit) : SystemBase(updateStrategy) {

    override fun doUpdate(time: Time) {
        updateFunction(world, time)
    }
}