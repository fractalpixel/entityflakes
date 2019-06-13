package org.entityflakes.processor

import org.entityflakes.World
import org.mistutils.time.Time
import org.mistutils.updating.strategies.UpdateStrategy
import org.mistutils.updating.strategies.VariableTimestepStrategy

/**
 * A simple processor implementation that takes an update strategy and an update function as a parameter.
 * @param updateFunction the function to run each update.  Gets the world that the processor is in, and a time object, as parameters.
 * @param updateStrategy how often to run the update function of this processor.  By default calls the update function once per world update.
 */
class SimpleProcessor(val updateFunction: (World, Time) -> Unit,
                      updateStrategy: UpdateStrategy = VariableTimestepStrategy()) : UpdatingProcessorBase(updateStrategy) {

    override fun doUpdate(time: Time) {
        updateFunction(world, time)
    }
}