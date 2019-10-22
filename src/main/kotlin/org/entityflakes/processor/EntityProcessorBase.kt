package org.entityflakes.processor

import org.entityflakes.Component
import org.entityflakes.Entity
import org.entityflakes.entityfilters.AllEntitiesFilter
import org.entityflakes.entityfilters.EntityFilter
import org.entityflakes.entitygroup.EntityGroup
import org.kwrench.time.Time
import org.kwrench.updating.strategies.UpdateStrategy
import org.kwrench.updating.strategies.VariableTimestepStrategy
import kotlin.reflect.KClass

/**
 * A base class for processors that runs an update for entities with the specified components.
 * @param filter a filter that matches the entities that this processor should update.
 * @param updateStrategy how often to run the update function of this processor.
 */
abstract class EntityProcessorBase(filter: EntityFilter? = AllEntitiesFilter,
                                   updateStrategy: UpdateStrategy = VariableTimestepStrategy()) : ProcessorWithEntitiesBase(filter, updateStrategy) {

    /**
     * @param updateStrategy how often to run the update function of this processor.
     * @param requiredComponentTypes the types of components that entities processed by this processor must have.
     */
    constructor(updateStrategy: UpdateStrategy = VariableTimestepStrategy(),
                vararg requiredComponentTypes: KClass<out Component>): this(null, updateStrategy) {
        this.requiredComponentTypes = requiredComponentTypes
    }

    override final fun doUpdate(time: Time) {
        preUpdate(time)
        updateEntities(time, entities)
        postUpdate(time)
    }

    /**
     * Called before entities are updated
     */
    protected open fun preUpdate(time: Time) {
    }

    /**
     * Updates entities.  By default visits each entity and calls updateEntity with it
     */
    protected open fun updateEntities(time: Time, group: EntityGroup) {
        group.forEachEntity { entity ->
            updateEntity(entity, time)
        }
    }

    /**
     * Called to update the specified entity.
     */
    protected abstract fun updateEntity(entity: Entity, time: Time)

    /**
     * Called after entities have been updated
     */
    protected open fun postUpdate(time: Time) {
    }


}