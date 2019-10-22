package org.entityflakes.entityfilters

import org.kwrench.collections.BitVector


/**
 * A filter that matches some entities.
 */
interface EntityFilter {

    fun matches(containedComponents: BitVector): Boolean

}