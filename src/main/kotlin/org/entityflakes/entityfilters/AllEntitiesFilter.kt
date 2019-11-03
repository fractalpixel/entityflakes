package org.entityflakes.entityfilters

import org.kwrench.collections.BitVector


/**
 * A filter that matches all entities in the world.
 */
object AllEntitiesFilter : EntityFilter {

    override fun matches(containedComponents: BitVector): Boolean = true

}