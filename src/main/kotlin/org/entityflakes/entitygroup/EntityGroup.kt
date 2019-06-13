package org.entityflakes.entitygroup

import org.entityflakes.Entity


/**
 * Represents a group of entities
 */
interface EntityGroup {

    /**
     * Calls the entityVisitor for each entity in this group
     */
    fun forEachEntity(entityVisitor: (Entity)->Unit)

    /**
     * Adds a listener that is notified when entities are added or removed to/from this group.
     */
    fun addListener(listener: EntityGroupListener)

    /**
     * Remove listener.
     */
    fun removeListener(listener: EntityGroupListener)

    /**
     * @return true if this group contains the specified entity.
     */
    fun contains(entityId: Int): Boolean

    /**
     * @return true if this group contains the specified entity.
     */
    fun contains(entity: Entity): Boolean = contains(entity.id)
}