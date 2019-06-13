package org.entityflakes.processor

import org.entityflakes.Component
import org.entityflakes.World
import org.entityflakes.entityfilters.AllEntitiesFilter
import org.entityflakes.entityfilters.EntityFilter
import org.entityflakes.entityfilters.RequiredComponentsFilter
import org.entityflakes.entitygroup.EntityGroup
import org.mistutils.updating.strategies.UpdateStrategy
import org.mistutils.updating.strategies.VariableTimestepStrategy
import kotlin.reflect.KClass

/**
 * A base class for processors that have a set of entities with specified components, but do not need to update the entities.
 * For a base class for processors that update entities, see EntityProcessorBase.
 * @param filter a filter that matches the entities that this processor should update.
 * @param updateStrategy how often to run the update function of this processor.
 */
abstract class ProcessorWithEntitiesBase(var filter: EntityFilter? = AllEntitiesFilter,
                                         updateStrategy: UpdateStrategy = VariableTimestepStrategy()) : UpdatingProcessorBase(updateStrategy) {

    /**
     * @param updateStrategy how often to run the update function of this processor.
     * @param requiredComponentTypes the types of components that entities processed by this processor must have.
     */
    constructor(updateStrategy: UpdateStrategy = VariableTimestepStrategy(),
                vararg requiredComponentTypes: KClass<out Component>): this(null, updateStrategy) {
        this.requiredComponentTypes = requiredComponentTypes
    }

    /**
     * Used when a filter can not be provided in the constructor as the EntityManager is not yet available there.
     */
    protected var requiredComponentTypes: Array<out KClass<out Component>>? = null

    /**
     * A group with the entities in the world that match the filter used by this entity processor.
     */
    protected lateinit var entities: EntityGroup


    override final fun doInit(world: World) {
        if (filter == null) filter = RequiredComponentsFilter(world, *requiredComponentTypes!!)
        entities = world.getEntityGroup(filter!!)
        doInit(world, entities)
    }

    /**
     * Called when the processor is initialized.
     * @param world the world that the processor is added to.
     * @param entities the group containing the entities that match the provided filter.
     *                 A listener can be added if addition and removal of entities matching the filter is interesting.
     */
    protected open fun doInit(world: World, entities: EntityGroup) {
    }

}