package org.entityflakes.entityfilters

import org.mistutils.collections.BitVector


/**
 * A filter that matches some entities.
 */
interface EntityFilter {

    fun matches(containedComponents: BitVector): Boolean

}