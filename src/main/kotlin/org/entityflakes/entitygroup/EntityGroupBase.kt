package org.entityflakes.entitygroup

import org.entityflakes.Entity
import org.entityflakes.entitymanager.EntityManager
import org.mistutils.collections.BitVector


/**
 * Default functionality for an EntityGroup
 */
abstract class EntityGroupBase(val entityManager: EntityManager): EntityGroup {

    private val entityIds = BitVector()
    private val listeners = ArrayList<EntityGroupListener>()

    override fun forEachEntity(entityVisitor: (Entity) -> Unit) {
        var id = 0
        do  {
            id = entityIds.nextSetBit(id+1)
            if (id > 0) {
                entityVisitor(entityManager.getEntity(id)!!)
            }
        } while (id > 0)
    }

    override fun contains(entityId: Int): Boolean = entityIds.get(entityId)

    /**
     * Add entity, if not already added.
     * Notifies listeners of this group.
     */
    protected open fun addEntity(entity: Entity) {
        if (!entityIds.get(entity.id)) {
            entityIds.set(entity.id)
            notifyEntityAdded(entity)
        }
    }

    /**
     * Remove entity, if contained.
     * Notifies listeners of this group.
     */
    protected open fun removeEntity(entity: Entity) {
        if (entityIds.get(entity.id)) {
            entityIds.clear(entity.id)
            notifyEntityRemoved(entity)
        }
    }

    protected open fun removeAllEntities() {
        forEachEntity { removeEntity(it) }
    }

    override fun addListener(listener: EntityGroupListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: EntityGroupListener) {
        listeners.remove(listener)
    }

    private fun notifyEntityAdded(entity: Entity) {
        for (listener in listeners) {
            listener.onEntityAdded(entity)
        }
    }

    private fun notifyEntityRemoved(entity: Entity) {
        for (listener in listeners) {
            listener.onEntityRemoved(entity)
        }
    }
}