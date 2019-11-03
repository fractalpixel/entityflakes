package org.entityflakes.system

import org.entityflakes.Component
import org.entityflakes.Entity
import org.entityflakes.entityfilters.AllEntitiesFilter
import org.entityflakes.entityfilters.EntityFilter
import org.entityflakes.entityfilters.RequiredComponentsFilter
import org.entityflakes.entitymanager.ComponentRef
import org.entityflakes.entitymanager.EntityManager
import org.kwrench.time.Time
import org.kwrench.updating.strategies.UpdateStrategy
import org.kwrench.updating.strategies.VariableTimestepStrategy
import kotlin.reflect.KClass

/**
 * A system that runs an update function for entities with the specified components.
 * @param filter a filter that matches the entities that this system should update.
 * @param updateStrategy how often to run the update function of this system.  By default calls the update function once per world update.
 * @param entityUpdater the function to run for each entity on each update.
 */
class SimpleEntitySystem(filter: EntityFilter = AllEntitiesFilter,
                         updateStrategy: UpdateStrategy = VariableTimestepStrategy(),
                         val entityUpdater: (Entity, Time) -> Unit) : EntitySystemBase(filter, false, updateStrategy) {

    /**
     * @param entityManager the [World] or [EntityManager], necessary for creating a filter from a list of component types.
     * @param requiredComponentTypes the types of components that entities processed by this system must have.
     * @param updateStrategy how often to run the update function of this system.
     * @param entityUpdater the function to run for each entity.
     */
    constructor(entityManager: EntityManager,
                vararg requiredComponentTypes: KClass<out Component>,
                updateStrategy: UpdateStrategy = VariableTimestepStrategy(),
                entityUpdater: (Entity, Time) -> Unit):
            this(RequiredComponentsFilter(entityManager, *requiredComponentTypes), updateStrategy, entityUpdater) {
    }


    /**
     * @param entityManager the [World] or [EntityManager], necessary for creating a filter from a list of component types.
     * @param requiredComponentTypes the types of components that entities processed by this system must have.
     * @param updateStrategy how often to run the update function of this system.
     * @param entityUpdater the function to run for each entity.
     */
    constructor(entityManager: EntityManager,
                requiredComponentTypes: Collection<KClass<out Component>>,
                updateStrategy: UpdateStrategy = VariableTimestepStrategy(),
                entityUpdater: (Entity, Time) -> Unit):
            this(RequiredComponentsFilter(entityManager, *requiredComponentTypes.toTypedArray()), updateStrategy, entityUpdater) {
    }

    /**
     * After the [entityManager] parameter, pass in one or more references to the types of components that entities
     * processed by this system must have.
     *
     * @param entityManager the [World] or [EntityManager], necessary for creating a filter from a list of component types.
     * @param updateStrategy how often to run the update function of this system.
     * @param entityUpdater the function to run for each entity.
     */
    constructor(entityManager: EntityManager,
                firstRequiredComponentRefs: ComponentRef<out Component>,
                vararg additionalRequiredComponentRefs: ComponentRef<out Component>,
                updateStrategy: UpdateStrategy = VariableTimestepStrategy(),
                entityUpdater: (Entity, Time) -> Unit):
            this(RequiredComponentsFilter(entityManager, firstRequiredComponentRefs, *additionalRequiredComponentRefs), updateStrategy, entityUpdater) {
    }

    override fun updateEntity(entity: Entity, time: Time) {
        entityUpdater(entity, time)
    }

}