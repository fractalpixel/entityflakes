package org.entityflakes.processor

import org.entityflakes.Entity
import org.entityflakes.entityfilters.AllEntitiesFilter
import org.entityflakes.entityfilters.EntityFilter
import org.kwrench.time.Time
import org.kwrench.updating.strategies.UpdateStrategy
import org.kwrench.updating.strategies.VariableTimestepStrategy

/**
 * A processor that runs an update function for entities with the specified components.
 * @param filter a filter that matches the entities that this processor should update.
 * @param entityUpdater the function to run for each entity.
 * @param updateStrategy how often to run the update function of this processor.  By default calls the update function once per world update.
 */
class SimpleEntityProcessor(filter: EntityFilter = AllEntitiesFilter,
                            val entityUpdater: (Entity, Time) -> Unit,
                            updateStrategy: UpdateStrategy = VariableTimestepStrategy()) : EntityProcessorBase(filter, updateStrategy) {

    override fun updateEntity(entity: Entity, time: Time) {
        entityUpdater(entity, time)
    }
}