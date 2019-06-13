package org.entityflakes


/**
 * Base interface for components.
 */
interface Component: Disposable {

    /**
     * Called when the component is added to an entity, and before listeners have been notified about the addition.
     * If the component implements ReusableComponent, this may be called again after reset().
     */
    fun init(entity: Entity) {}

    /**
     * Called when the component is no longer used at all.
     */
    override fun dispose() {}
}