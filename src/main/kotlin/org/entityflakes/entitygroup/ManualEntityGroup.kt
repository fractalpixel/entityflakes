package org.entityflakes.entitygroup

import org.entityflakes.Entity
import org.entityflakes.entitymanager.EntityManager

/**
 * Entity group that entities are added and removed from by calling the [add] and [remove] methods.
 */
class ManualEntityGroup(entityManager: EntityManager): EntityGroupBase(entityManager) {

    fun add(entity: Entity) {
        addEntity(entity)
    }

    fun remove(entity: Entity) {
        removeEntity(entity)
    }

}