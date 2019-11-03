package org.entityflakes.entitygroup

import org.entityflakes.Entity
import org.entityflakes.entityfilters.EntityFilter
import org.entityflakes.entitymanager.EntityManager

/**
 * An EntityGroup that listens to the entities in the world and updates the group contents on the fly based on a filter.
 */
class DynamicEntityGroup(var filter: EntityFilter, entityManager: EntityManager): EntityGroupBase(entityManager) {

    private val listener: EntityGroupListener = object: EntityGroupListener {
        override fun onEntityAdded(entity: Entity) {
            addEntity(entity)
        }

        override fun onEntityRemoved(entity: Entity) {
            removeEntity(entity)
        }
    }

    init {
        // Add all entities matching the filter
        entityManager.forEachEntity { entity ->
            if (filter.matches(entity.containedComponents)) addEntity(entity)
        }

        // Listen to changes
        entityManager.addListener(listener, filter)
    }

}