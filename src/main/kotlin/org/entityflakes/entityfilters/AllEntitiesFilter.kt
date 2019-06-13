package org.entityflakes.entityfilters

import org.mistutils.collections.BitVector


/**
 *
 */
object AllEntitiesFilter : EntityFilter {

    override fun matches(containedComponents: BitVector): Boolean = true

}