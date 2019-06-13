package org.entityflakes.entitymanager

import org.entityflakes.Component
import org.entityflakes.Entity
import org.entityflakes.PolymorphicComponent
import org.entityflakes.entityfilters.AllEntitiesFilter
import org.entityflakes.entityfilters.EntityFilter
import org.entityflakes.entityfilters.RequiredAndForbiddenComponentsFilter
import org.entityflakes.entityfilters.RequiredComponentsFilter
import org.entityflakes.entitygroup.EntityGroup
import org.entityflakes.entitygroup.EntityGroupListener
import org.mistutils.symbol.Symbol
import kotlin.reflect.KClass

/**
 * Something that manages entities.
 */
interface EntityManager {

    /**
     * Creates a new entity with the specified component instances.
     * Returns the created entity.
     */
    fun createEntity(vararg components: Component): Entity

    /**
     * Creates a new entity with the specified component types.
     * A new instance of each component type is created using the default constructor for that component type.
     * Returns the created entity.
     */
    fun createEntityWithTypes(componentType: KClass<out Component>, vararg componentTypes: KClass<out Component>): Entity {
        val entity = createEntity()
        entity.createComponent(componentType)
        for (compType in componentTypes) {
            entity.createComponent(compType)
        }
        return entity
    }

    /**
     * Creates a new entity with the specified component types.
     * A new instance of each component type is created using the no-arguments constructor for that component type.
     * Returns the created entity.
     * Throws an exception if some of the components can not be instantiated.
     */
    fun createEntityWithTypes(vararg componentTypes: ComponentRef<out Component>): Entity {
        val entity = createEntity()
        for (componentType in componentTypes) {
            entity.createComponent(componentType)
        }
        return entity
    }

    /**
     * Assigns an entity a tag that can be used to retrieve it later.
     * Any previous entity with the same tag no longer has that tag.
     */
    fun tagEntity(entity: Entity, tag: Symbol)

    /**
     * Removes the specified tag if it has been assigned to an entity.
     */
    fun removeTag(tag: Symbol)

    /**
     * @return the entity with the specified tag, or null if there is no such entity (or it has been deleted).
     */
    fun getTaggedEntity(tag: Symbol): Entity?

    /**
     * @return the entity with the specified id, or null if no entity with that id found.
     */
    fun getEntity(entityId: Int): Entity?

    /**
     * Deletes the specified entity and releases components used by it.
     * The entityId is also released and can be used by other entities.
     */
    fun deleteEntity(entity: Entity)

    /**
     * Calls the supplied function for each entity in the world.
     */
    fun forEachEntity(entityVisitor: (Entity) -> Unit)

    /**
     * @return a ComponentRef that returns components of the specified type for entities, faster than retrieving the entities by type.
     */
    fun <T: Component> getComponentRef(componentType: Class<T>): ComponentRef<T>

    /**
     * @return a ComponentRef that returns components of the specified type for entities, faster than retrieving the entities by type.
     */
    fun <T: Component> getComponentRef(componentType: KClass<T>): ComponentRef<T> = getComponentRef(componentType.java)

    /**
     * @return the id for the specified component type.
     */
    fun <T: Component>getComponentTypeId(componentType: Class<T>): Int

    /**
     * @return the id for the specified component type.
     */
    fun <T: Component>getComponentTypeId(componentType: KClass<T>): Int = getComponentTypeId(componentType.java)

    /**
     * @return the component type id for the specified component.
     */
    fun <T: Component>getComponentTypeId(component: T): Int = getComponentTypeId(if (component is PolymorphicComponent) component.componentCategory else component.javaClass)

    /**
     * @return the ids for the specified component types.
     */
    fun getComponentTypeIds(vararg componentTypes: KClass<out Component>): List<Int> = componentTypes.map{getComponentTypeId(it)}

    /**
     * Adds a listener that will be notified when entities matching the specified filter appear or disappear.
     * Throws an exception if the listener is already registered.
     */
    fun addListener(listener: EntityGroupListener, filter: EntityFilter = AllEntitiesFilter)

    /**
     * Removes the specified listener.
     */
    fun removeListener(listener: EntityGroupListener)

    /**
     * @return a group that contains all the entities managed by this EntityManager that match the specified filter.
     * The group is updated when entities change so that they no longer match the filter or start to match the filter.
     * Listeners can be added to the group as well.
     * Instances of groups with the same filter are shared.
     */
    fun getEntityGroup(filter: EntityFilter): EntityGroup

    /**
     * @return a group that contains all the entities managed by this EntityManager that contain all the specified component types.
     * The group is updated when entities change so that they no longer match the filter or start to match the filter.
     * Listeners can be added to the group as well.
     * Instances of groups with the same filter are be shared.
     */
    fun getEntityGroup(vararg requiredComponents: ComponentRef<out Component>): EntityGroup {
        return getEntityGroup(RequiredComponentsFilter(this, *requiredComponents))
    }

    /**
     * @return an EntityFilter that matches all entities that have all components of the specified types (they may also have other components).
     */
    fun createEntityFilter(vararg requiredComponentTypes: KClass<out Component>): EntityFilter = RequiredComponentsFilter(this, *requiredComponentTypes)

    /**
     * @return an EntityFilter that matches all entities that have all components of the required types,
     * and no components of the forbidden types.
     */
    fun createEntityFilter(requiredComponentTypes: Collection<Class<out Component>>,
                           forbiddenComponentTypes: Collection<Class<out Component>>): EntityFilter = RequiredAndForbiddenComponentsFilter(this, requiredComponentTypes, forbiddenComponentTypes)

}