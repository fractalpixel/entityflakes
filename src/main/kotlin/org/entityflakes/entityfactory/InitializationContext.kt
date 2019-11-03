package org.entityflakes.entityfactory

import org.entityflakes.Component
import org.entityflakes.Entity
import org.entityflakes.World
import org.entityflakes.entitymanager.ComponentRef
import org.kwrench.random.Rand
import org.kwrench.symbol.Symbol
import kotlin.reflect.KClass

interface InitializationContext {
    val world: World
    val entity: Entity
    val params: Map<Symbol, Any>
    val random: Rand

    /** Gets or creates component of the specified type in the entity being built and returns it. */
    operator fun <T: Component> get(componentType:  KClass<T>): T = entity.getOrCreate(componentType)

    /** Gets or creates component of the specified type in the entity being built and returns it. */
    operator fun <T: Component> get(componentType:  Class<T>): T = entity.getOrCreate(componentType)

    /** Gets or creates component of the specified type in the entity being built and returns it. */
    operator fun <T: Component> get(componentType: ComponentRef<T>): T = entity.getOrCreate(componentType)
}

class InitializationContextImpl: InitializationContext {
    override lateinit var world: World
    override lateinit var entity: Entity
    override lateinit var params: Map<Symbol, Any>
    override lateinit var random: Rand
}