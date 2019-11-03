package org.entityflakes.entityfilters

import org.kwrench.collections.BitVector


/**
 * A filter that matches no entities in the world.
 */
object NoEntitiesFilter : EntityFilter {

    override fun matches(containedComponents: BitVector): Boolean = false

}