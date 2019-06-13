package org.entityflakes.entitymanager

import org.entityflakes.Component
import org.entityflakes.Entity
import org.entityflakes.World


/**
 * Used internally to provide various non-public support functions to Entities managed by some EntityManager.
 */
interface EntitySupport {

    /**
     * The world that the entity resides in.
     */
    val world: World

    /**
     * Entities should call this when components are added to them
     */
    fun entityComponentAdded(entity: Entity, componentId: Int)

    /**
     * Entities should call this when components are removed from them
     */
    fun entityComponentRemoved(entity: Entity, componentId: Int)

    /**
     * Highest currently registered component id
     */
    val maxComponentId: Int

    /**
     * Should create a new instance of a component with the specified component id, or throw an exception if not possible.
     */
    fun <T: Component> createComponent(componentId: Int): T

    fun releaseComponent(componentId: Int, component: Component)
}