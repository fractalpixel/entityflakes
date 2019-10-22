package org.entityflakes.processor

import org.kwrench.time.Time
import org.kwrench.updating.Updating
import org.kwrench.updating.strategies.UpdateStrategy
import org.kwrench.updating.strategies.VariableTimestepStrategy

/**
 * A base class for processors that have some update strategy.
 * @param updateStrategy how often to run the update function of this processor.
 */
abstract class UpdatingProcessorBase(var updateStrategy: UpdateStrategy = VariableTimestepStrategy()) : ProcessorBase() {

    // Updating object that delegates to doUpdate()
    private val localUpdate: Updating = object: Updating {
        override fun update(time: Time) {
            doUpdate(time)
        }
    }

    final override fun update(time: Time) {
        // Use the strategy to update (may call doUpdate zero or more times)
        updateStrategy.update(localUpdate, time)
    }

    /**
     * Called each time the processor is updated by its update strategy (may be more or less than once per frame).
     */
    protected open fun doUpdate(time: Time) {
    }

}