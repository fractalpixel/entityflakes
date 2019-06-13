package org.entityflakes.storagesystem

import org.entityflakes.Entity
import org.entityflakes.processor.EntityProcessorBase
import org.mistutils.time.Time
import org.mistutils.updating.strategies.VariableTimestepStrategy

/**
 *
 */
@Deprecated("Not (yet?) implemented")
class StorageService(): EntityProcessorBase(VariableTimestepStrategy()) {
    override fun updateEntity(entity: Entity, time: Time) {
        TODO("Implement")
    }
}