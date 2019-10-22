package org.entityflakes.entityfactory

import org.entityflakes.Entity
import org.entityflakes.World
import org.kwrench.symbol.Symbol

/**
 * Something that can create entities for a world, typically of a certain type.
 */
interface EntityFactory {

    /**
     * Called when the entity factory is added to the world.  Gets passed a reference to the world,
     * and the [factoryId] that is used to denote this factory.
     */
    fun init(world: World, factoryId: Symbol)

    /**
     * Creates an entity, adds it to the world, adds components to it, and initializes the components and the entity as a whole.
     *
     * Optionally pass in a [parameters] map that will be given to any component initializers.
     *
     * Can also pass in a [randomSeed] that will be used to provide random sequences to the component initializers.
     * If omitted, the system (nano) time will be used instead.
     *
     * Also returns the created entity
     */
    fun createEntity(randomSeed: Long? = null,
                     parameters: Map<Symbol, Any>? = null): Entity

}