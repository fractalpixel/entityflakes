package org.entityflakes.entitymanager

import org.entityflakes.Component
import org.entityflakes.Entity
import kotlin.reflect.KClass

/**
 * Used to quickly retrieve a component of a specific type from entities.
 * @param componentType The type of component that this accessor retrieves.
 */
class ComponentRef<T: Component>(val componentType: Class<T>) {

    /**
     * @param componentType The type of component that this accessor retrieves.
     */
    constructor(componentType: KClass<T>) : this(componentType.java)

    private var typeId: Int = 0
    private var entityManager: EntityManager? = null

    /**
     * @param entityManager [EntityManager] that can be queried for the component id, e.g. [World].
     * @return The id for the specified component type
     */
    fun getComponentTypeId(entityManager: EntityManager): Int {
        if (this.entityManager != entityManager) {
            typeId = entityManager.getComponentTypeId(componentType)
            this.entityManager = entityManager
        }

        return typeId;
    }

    /**
     * @returns this component for the specified entity, or throws an exception if the entity does not have this type of component.
     */
    operator fun get(entity: Entity): T {
        val world = entity.world
        if (this.entityManager != world) {
            getComponentTypeId(world)
        }
        return entity[typeId]!!
    }

    /**
     * @returns this component for the specified entity, or null if the entity does not have this type of component.
     */
    fun getOrNull(entity: Entity): T? {
        val world = entity.world
        if (this.entityManager != world) {
            getComponentTypeId(world)
        }
        return entity[typeId]
    }

}