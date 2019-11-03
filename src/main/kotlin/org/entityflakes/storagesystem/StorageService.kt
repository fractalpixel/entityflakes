package org.entityflakes.storagesystem

import org.entityflakes.Entity
import org.entityflakes.system.EntitySystemBase
import org.kwrench.time.Time
import org.kwrench.updating.strategies.VariableTimestepStrategy

/**
 *
 */
@Deprecated("Not (yet?) implemented")
class StorageService(): EntitySystemBase(VariableTimestepStrategy()) {
    override fun updateEntity(entity: Entity, time: Time) {
        TODO("Implement")
    }

    override fun onEntityAdded(entity: Entity) {
        TODO("not implemented")
    }

    override fun onEntityRemoved(entity: Entity) {
        TODO("not implemented")
    }
}