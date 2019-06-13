package org.entityflakes.entitygroup

import org.entityflakes.Entity
import org.entityflakes.EntityListener
import java.util.*

/**
 * An EntityGroup where the contents are updated manually.
 * Optimized for small groups.  Does not require an EntityManager.
 * Automatically removes deleted entities.
 */
class SmallEntityGroup(): EntityGroup {

    private val entities_ = ArrayList<Entity>()
    private val listeners = ArrayList<EntityGroupListener>()

    private val entityDeletionListener = object : EntityListener {
        override fun onEntityRemoved(entity: Entity) {
            removeEntity(entity)
        }
    }

    /**
     * The entities currently in this group.
     */
    val entities: List<Entity> = entities_

    override fun forEachEntity(entityVisitor: (Entity) -> Unit) {
        for (i in 0 .. entities_.size - 1) {
            entityVisitor(entities_[i])
        }
    }

    override fun contains(entityId: Int): Boolean {
        for (i in 0 .. entities_.size - 1) {
            if (entities_[i].id == entityId) return true
        }
        return false
    }

    override fun contains(entity: Entity): Boolean {
        return entities_.contains(entity)
    }

    /**
     * Add entity, if not already added.
     * Notifies listeners of this group.
     */
    fun addEntity(entity: Entity) {
        if (!entities_.contains(entity)) {
            entities_.add(entity)
            entity.addListener(entityDeletionListener)
            notifyEntityAdded(entity)
        }
    }

    /**
     * Remove entity, if contained.
     * Notifies listeners of this group.
     */
    fun removeEntity(entity: Entity) {
        val removed = entities_.remove(entity)
        if (removed) {
            entity.removeListener(entityDeletionListener)
            notifyEntityRemoved(entity)
        }
    }

    /**
     * Removes all entities in this group.
     * Notifies listeners.
     */
    fun removeAllEntities() {
        var e = entities_.lastOrNull()
        while (e != null) {
            removeEntity(e)
            e = entities_.lastOrNull()
        }
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