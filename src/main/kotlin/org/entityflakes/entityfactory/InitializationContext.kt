package org.entityflakes.entityfactory

import org.entityflakes.Entity
import org.entityflakes.World
import org.mistutils.random.RandomSequence
import org.mistutils.symbol.Symbol

interface InitializationContext {
    val world: World
    val entity: Entity
    val params: Map<Symbol, Any>
    val random: RandomSequence
}

class InitializationContextImpl: InitializationContext {
    override lateinit var world: World
    override lateinit var entity: Entity
    override lateinit var params: Map<Symbol, Any>
    override lateinit var random: RandomSequence
}