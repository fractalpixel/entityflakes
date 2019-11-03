package org.entityflakes.system

import org.entityflakes.Component
import org.entityflakes.Entity
import org.entityflakes.World
import org.entityflakes.entityfilters.AllEntitiesFilter
import org.entityflakes.entityfilters.EntityFilter
import org.entityflakes.entityfilters.RequiredComponentsFilter
import org.entityflakes.entitygroup.EntityGroup
import org.entityflakes.entitygroup.EntityGroupListener
import org.kwrench.time.Time
import org.kwrench.updating.strategies.UpdateStrategy
import org.kwrench.updating.strategies.VariableTimestepStrategy
import kotlin.reflect.KClass

/**
 * A base class for systems that runs an update for entities with the specified components.
 *
 * @param filter a filter that matches the entities that this system should update.  If null, no entities are matched.
 * @param listenToEntityGroup if true, each time an entity is added or removed from the group of entities processed by this system,
 *        the [onEntityAdded] or [onEntityRemoved] functions are called.  If you are not going to use them and have a large,
 *        often changing group, you may want to disable this.
 * @param updateStrategy how often to run the update function of this system.
 */
abstract class EntitySystemBase(var filter: EntityFilter? = AllEntitiesFilter,
                                val listenToEntityGroup: Boolean = true,
                                updateStrategy: UpdateStrategy = VariableTimestepStrategy()) : SystemBase(updateStrategy), EntityGroupListener {

    /**
     * @param updateStrategy how often to run the update function of this system.
     * @param listenToEntityGroup if true, each time an entity is added or removed from the group of entities processed by this system,
     *        the [onEntityAdded] or [onEntityRemoved] functions are called.  If you are not going to use them and have a large,
     *        often changing group, you may want to disable this.
     * @param requiredComponentTypes the types of components that entities processed by this system must have.
     */
    constructor(updateStrategy: UpdateStrategy = VariableTimestepStrategy(),
                listenToEntityGroup: Boolean = true,
                vararg requiredComponentTypes: KClass<out Component>): this(null, listenToEntityGroup, updateStrategy) {
        this.requiredComponentTypes = requiredComponentTypes
    }

    /**
     * Used when a filter can not be provided in the constructor as the EntityManager is not yet available there.
     */
    protected var requiredComponentTypes: Array<out KClass<out Component>> = emptyArray()

    /**
     * A group with the entities in the world that match the filter used by this entity system.
     */
    protected lateinit var entities: EntityGroup


    final override fun doInit(world: World) {
        // Get group of entities that are processed by this system
        if (filter == null) filter = RequiredComponentsFilter(world, *requiredComponentTypes)
        entities = world.getEntityGroup(filter!!)

        // Initialize
        doInit(world, entities)

        if (listenToEntityGroup) {
            // Notify system of each entity that it handles
            entities.forEachEntity { onEntityAdded(it) }

            // Notify this system about entities added and removed to the entities it handles
            entities.addListener(this)
        }
    }

    final override fun doDispose() {
        if (listenToEntityGroup) {
            // Stop listening to entities
            entities.removeListener(this)

            // Notify system of each entity it no longer handles
            entities.forEachEntity { onEntityRemoved(it) }
        }

        doFinalDispose()
    }

    final override fun doUpdate(time: Time) {
        preUpdate(time)
        updateEntities(time, entities)
        postUpdate(time)
    }

    /**
     * Called when the processor is initialized.
     * @param world the world that the processor is added to.
     * @param entities the group containing the entities that match the provided filter.
     *                 If [listenToEntityGroup] is true, then [onEntityAdded] will be called for each entity in this
     *                 group after this method returns.
     */
    protected open fun doInit(world: World, entities: EntityGroup) {
    }

    /**
     * If [listenToEntityGroup] is true, this is called when an entity starts to match the filter used by this processor.
     */
    override fun onEntityAdded(entity: Entity) {}

    /**
     * If [listenToEntityGroup] is true, this is called when an entity no longer matches the filter used by this processor
     * (e.g. deleted or a required component removed).
     */
    override fun onEntityRemoved(entity: Entity) {}

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

    /**
     * Called when the system is disposed.  Do any cleanup.
     * If [listenToEntityGroup] is true, [onEntityRemoved] has already been called for each entity that was in the system.
     */
    protected open fun doFinalDispose() {
    }


}