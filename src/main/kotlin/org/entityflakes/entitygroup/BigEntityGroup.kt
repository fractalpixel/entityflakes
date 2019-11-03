package org.entityflakes.entitygroup

import org.entityflakes.Entity
import org.entityflakes.EntityListener
import org.entityflakes.entitymanager.EntityManager


/**
 * An EntityGroup where the contents are updated manually.
 * Optimized for large groups that include a large portion of the entities in the world.
 * Stores entities as a bitvector of integer ids.
 * Requires an EntityManager at construction time.
 */
class BigEntityGroup(entityManager: EntityManager): EntityGroupBase(entityManager) {

    private val entityDeletionListener = object : EntityListener {
        override fun onEntityRemoved(entity: Entity) {
            removeEntity(entity)
        }
    }

    /**
     * Add entity, if not already added.
     * Notifies listeners of this group.
     */
    public override fun addEntity(entity: Entity) {
        super.addEntity(entity)
        entity.addListener(entityDeletionListener)
    }

    /**
     * Remove entity, if contained.
     * Notifies listeners of this group.
     */
    public override fun removeEntity(entity: Entity) {
        super.removeEntity(entity)
        entity.removeListener(entityDeletionListener)
    }

    /**
     * Removes all entities in this group.
     * Notifies listeners.
     */
    public override fun removeAllEntities() {
        super.removeAllEntities()
    }

}