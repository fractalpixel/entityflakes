package org.entityflakes.entityfactory

import org.entityflakes.Entity
import org.entityflakes.World
import org.kwrench.random.Rand
import org.kwrench.symbol.Symbol

interface InitializationContext {
    val world: World
    val entity: Entity
    val params: Map<Symbol, Any>
    val random: Rand
}

class InitializationContextImpl: InitializationContext {
    override lateinit var world: World
    override lateinit var entity: Entity
    override lateinit var params: Map<Symbol, Any>
    override lateinit var random: Rand
}