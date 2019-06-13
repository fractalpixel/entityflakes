package org.entityflakes

/**
 * Used to mark reusable components, where deleted components are stored in an object pool and used when
 * a component of that type is needed for an entity, instead of de-allocating and re-allocating memory.
 * <p>
 * Components of this type need to implement a no-arguments constructor that can be used to create new instances as needed.
 * <p>
 * Note that common component categories for different component types do not work well with reusable components
 * (as reusable components need to be instantiable by calling the component category constructor).
 * In general reusable components should not change the default implementation of componentCategory,
 * which just returns the class of the component itself.
 */
interface ReusableComponent : Component {

    /**
     * Called when a component is removed from an entity, and before it is attached to another entity.
     * Can be used to reset state.
     * Will not be called if the object is not pooled, e.g. because the pool is full, in that case dispose() is called
     * and the component will no longer be used.
     */
    fun reset()

}