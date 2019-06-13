package org.entityflakes.entitygroup

import org.entityflakes.Entity


/**
 * A listener that is notified when entities are added or removed to or from a group of entities.
 */
interface EntityGroupListener {

    fun onEntityAdded(entity: Entity)

    fun onEntityRemoved(entity: Entity)

}