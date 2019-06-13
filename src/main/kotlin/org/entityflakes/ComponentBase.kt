package org.entityflakes

/**
 * A component base class that stores a reference to the entitiy that the component currently belongs to.
 */
abstract class ComponentBase: Component {

    private var currentEntity: Entity? = null
    val entity: Entity? get() = currentEntity

    final override fun init(entity: Entity) {
        currentEntity = entity
        doInit(entity)
    }

    final override fun dispose() {
        doDispose()
        currentEntity = null
    }

    protected abstract fun doInit(entity: Entity)
    protected abstract fun doDispose()
}