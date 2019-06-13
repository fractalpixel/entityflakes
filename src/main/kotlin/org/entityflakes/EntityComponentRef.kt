package org.entityflakes

import org.entityflakes.entitymanager.ComponentRef
import kotlin.reflect.KClass

/**
 * Reference to a specific component on a specific entity.
 */
data class EntityComponentRef<T: Component>(val entityRef: EntityRef,
                                            val componentRef: ComponentRef<T>) {

    constructor(entity: Entity, componentType: KClass<T>): this(EntityRef(entity), ComponentRef(componentType))

    /**
     * The referenced component, or null if either the entity doesn't exist, or the component isn't available on the entity.
     */
    val component: T? get() {
        val entity = entityRef.entity
        return if (entity != null) componentRef.getOrNull(entity)
        else null
    }

    /**
     * The entity that the component is in, or null if not available.
     */
    val entity: Entity? get() = entityRef.entity

    /**
     * The type of component referenced.
     */
    val componentType: Class<T> get() = componentRef.componentType
}