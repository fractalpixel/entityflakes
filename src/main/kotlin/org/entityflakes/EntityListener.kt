package org.entityflakes

/**
 * Listens to entity removal
 */
interface EntityListener {
    fun onEntityRemoved(entity: Entity)
}