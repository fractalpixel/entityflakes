package org.entityflakes.entitygroup

import org.entityflakes.Entity
import org.entityflakes.entitymanager.EntityManager

/**
 *
 */
class ManualEntityGroup(entityManager: EntityManager): EntityGroupBase(entityManager) {

    fun add(entity: Entity) {
        addEntity(entity)
    }

    fun remove(entity: Entity) {
        removeEntity(entity)
    }

}