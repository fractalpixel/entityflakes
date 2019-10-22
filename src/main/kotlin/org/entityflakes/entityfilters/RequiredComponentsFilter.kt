package org.entityflakes.entityfilters

import org.entityflakes.Component
import org.entityflakes.entitymanager.ComponentRef
import org.entityflakes.entitymanager.EntityManager
import org.kwrench.collections.BitVector
import kotlin.reflect.KClass

/**
 * A filter that matches entities with certain components.
 * @param requiredComponentTypeIds The type ids component that the entities in the filter should match.
 */
class RequiredComponentsFilter(requiredComponentTypeIds: Collection<Int>) : EntityFilter {

    private val requiredComponentIds = BitVector(requiredComponentTypeIds)

    /**
     * @param entityManager The EntityManager that manages the entities that this filter can be applied to.
     * @param requiredComponentTypes The component types the entities in the filter should match.
     */
    constructor(entityManager: EntityManager, vararg requiredComponentTypes: KClass<out Component>): this(entityManager.getComponentTypeIds(*requiredComponentTypes))

    /**
     * @param requiredComponentTypes The component accessors of the component types the entities in the filter should match.
     */
    constructor(entityManager: EntityManager, vararg requiredComponentTypes: ComponentRef<out Component>): this(requiredComponentTypes.map{it.getComponentTypeId(entityManager)})


    override fun matches(containedComponents: BitVector): Boolean {
        return if (requiredComponentIds.isEmpty) true
               else containedComponents.containsAll(requiredComponentIds)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is RequiredComponentsFilter) return false
        return requiredComponentIds.equals(other.requiredComponentIds)
    }

    override fun hashCode(): Int {
        return requiredComponentIds.hashCode()
    }
}