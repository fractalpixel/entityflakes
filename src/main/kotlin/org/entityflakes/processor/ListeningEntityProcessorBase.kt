package org.entityflakes.processor

import org.entityflakes.Component
import org.entityflakes.Entity
import org.entityflakes.World
import org.entityflakes.entityfilters.AllEntitiesFilter
import org.entityflakes.entityfilters.EntityFilter
import org.entityflakes.entitygroup.EntityGroup
import org.entityflakes.entitygroup.EntityGroupListener
import org.mistutils.updating.strategies.UpdateStrategy
import org.mistutils.updating.strategies.VariableTimestepStrategy
import kotlin.reflect.KClass

/**
 * A base class for processors that runs an update for entities with the specified components,
 * and is notified when entities matching the filter are added or removed.
 * @param filter a filter that matches the entities that this processor should update.
 * @param updateStrategy how often to run the update function of this processor.
 */
abstract class ListeningEntityProcessorBase(filter: EntityFilter? = AllEntitiesFilter,
                                            updateStrategy: UpdateStrategy = VariableTimestepStrategy()): EntityProcessorBase(filter, updateStrategy), EntityGroupListener {

    /**
     * @param updateStrategy how often to run the update function of this processor.
     * @param requiredComponentTypes the types of components that entities processed by this processor must have.
     */
    constructor(updateStrategy: UpdateStrategy = VariableTimestepStrategy(),
                vararg requiredComponentTypes: KClass<out Component>): this(null, updateStrategy) {
        this.requiredComponentTypes = requiredComponentTypes
    }

    override fun doInit(world: World, entities: EntityGroup) {
        entities.forEachEntity { onEntityAdded(it) }
        entities.addListener(this)
    }

    override fun doDispose() {
        entities.removeListener(this)
        entities.forEachEntity { onEntityRemoved(it) }
    }

    /**
     * Called when an entity starts to match the filter used by this processor
     */
    override abstract fun onEntityAdded(entity: Entity)

    /**
     * Called when an entity no longer matches the filter used by this processor (e.g. deleted or a required component removed).
     */
    override abstract fun onEntityRemoved(entity: Entity)
}