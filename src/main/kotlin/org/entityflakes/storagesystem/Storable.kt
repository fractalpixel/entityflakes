package org.entityflakes.storagesystem

import com.esotericsoftware.kryo.KryoSerializable


/**
 * Something that may optionally store itself.
 */
interface Storable: KryoSerializable {
}