package org.entityflakes.entityfilters

import org.entityflakes.Component
import org.entityflakes.entitymanager.EntityManager
import org.mistutils.collections.BitVector

/**
 * Represents entities with certain components.
 * @param entityManager The EntityManager that manages the entities that this filter can be applied to.
 * @param requiredComponentTypes The component types the entities in the filter should match.
 * @param forbiddenComponentTypes The component types the entities in the filter should not match.
 */
class RequiredAndForbiddenComponentsFilter(val entityManager: EntityManager,
                                           requiredComponentTypes: Collection<Class<out Component>>,
                                           forbiddenComponentTypes: Collection<Class<out Component>> = emptyList()) : EntityFilter {

    private val requiredComponentIds = getComponentTypesBitVector(requiredComponentTypes)
    private val forbiddenComponentIds = getComponentTypesBitVector(forbiddenComponentTypes)

    override fun matches(containedComponents: BitVector): Boolean {
        return containedComponents.containsAll(requiredComponentIds) &&
              !containedComponents.intersects(forbiddenComponentIds)
    }

    private fun getComponentTypesBitVector(types: Collection<Class<out Component>>): BitVector {
        val componentFlags = BitVector()
        for (type in types) {
            componentFlags.set(entityManager.getComponentTypeId(type))
        }
        return componentFlags
    }

    override fun equals(other: Any?): Boolean {
        if (other !is RequiredAndForbiddenComponentsFilter) return false
        return requiredComponentIds.equals(other.requiredComponentIds) &&
               forbiddenComponentIds.equals(other.forbiddenComponentIds)
    }

    override fun hashCode(): Int {
        return requiredComponentIds.hashCode().xor(127 + forbiddenComponentIds.hashCode())
    }
}