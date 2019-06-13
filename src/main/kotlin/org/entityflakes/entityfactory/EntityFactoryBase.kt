package org.entityflakes.entityfactory

import org.entityflakes.Entity
import org.entityflakes.World
import org.mistutils.symbol.Symbol
import java.lang.IllegalStateException

/**
 * Provides some basic functionality for EntityFactories.
 */
abstract class EntityFactoryBase: EntityFactory {

    private var initialized = false
    private lateinit var registeredWorld: World
    private lateinit var registeredFactoryId: Symbol

    private val emptyParams: Map<Symbol, Any> = emptyMap()

    /**
     * The world that this EntityFactory is added to.
     * Throws an exception if accessed before the factory is added to the world.
     */
    val world: World get() {
        if (!initialized) throw IllegalStateException("This entity factory is not yet added to a World, so can't access the world.")
        return registeredWorld
    }

    /**
     * The factoryId that is used for this factory.
     * Throws an exception if accessed before the factory is added to the world.
     */
    val factoryId: Symbol get() {
        if (!initialized) throw IllegalStateException("This entity factory is not yet added to a World, so can't access the identifier that it was added with.")
        return registeredFactoryId
    }

    final override fun init(world: World, factoryId: Symbol) {
        if (initialized) throw IllegalStateException("The EntityFactory is already initialized! (init(world) has already been called).  Make sure it is not added twice.")
        registeredWorld = world
        registeredFactoryId = factoryId
        initialized = true

        doInit(registeredWorld)
    }

    final override fun createEntity(randomSeed: Long?, parameters: Map<Symbol, Any>?): Entity {
        if (!initialized) throw IllegalStateException("Can't create entity with entity factory, the entity factory has not yet been added registered with the world.  Use world.registerEntityFactory(factory).")

        val seed = (randomSeed ?: (System.nanoTime() + world.entityCount))
        val params = parameters ?: emptyParams

        return doCreateEntity(world, seed, params)
    }

    /**
     * Initialize the entity factory.
     */
    protected abstract fun doInit(world: World)

    /**
     * Create an entity instance.
     *
     * The random seed is either the one passed in to createEntity, or based on the current system nano time.
     * The parameters are either the ones passed in to createEntity, or an empty map if no parameters given-
     */
    protected abstract fun doCreateEntity(world: World, randomSeed: Long, parameters: Map<Symbol, Any>): Entity

}