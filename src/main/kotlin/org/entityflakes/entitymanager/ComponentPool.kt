package org.entityflakes.entitymanager

import org.entityflakes.Component
import org.entityflakes.ReusableComponent
import org.kwrench.collections.bag.Bag

/**
 * Handles component creation and deletion, using a pool to recycle components if they support that.
 */
class ComponentPool<T: Component>(val type: Class<T>,
                                  val maxObjectPoolSize: Int = 1024*16) {

    val reusable: Boolean = ReusableComponent::class.java.isAssignableFrom(type)
    val pool: Bag<T>? = if (reusable) Bag<T>(maxObjectPoolSize / 16) else null

    fun createComponent(): T {
        return pool?.removeLast() ?: createComponentInstance()
    }

    /**
     * Reset the component and add it to the component pool, or call dispose on the component if the pool is full or the component is not reusable.
     */
    fun releaseComponent(component: T) {
        if (reusable && pool!!.size() < maxObjectPoolSize) {
            (component as ReusableComponent).reset()
            pool.add(component)
        }
        else {
            component.dispose()
        }
    }

    private fun createComponentInstance(): T {
        return type.newInstance()
    }

    /**
     * Removes all pooled components and calls dispose on them.
     */
    fun dispose() {
        if (pool != null) {
            for (i in 0 until pool.size()) {
                pool.get(i)?.dispose()
            }
            pool.clear()
        }
    }

}