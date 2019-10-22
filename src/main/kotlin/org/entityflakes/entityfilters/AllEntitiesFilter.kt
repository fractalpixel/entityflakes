package org.entityflakes.entityfilters

import org.kwrench.collections.BitVector


/**
 *
 */
object AllEntitiesFilter : EntityFilter {

    override fun matches(containedComponents: BitVector): Boolean = true

}