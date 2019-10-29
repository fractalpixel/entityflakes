package org.entityflakes.storagesystem

import com.esotericsoftware.kryo.KryoSerializable
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import org.entityflakes.Component


/**
 * Something that may optionally store itself.
 */
interface Storable: KryoSerializable {
}