package org.entityflakes.entityfactory

import org.entityflakes.Component
import org.entityflakes.Entity
import org.kwrench.random.Rand
import org.kwrench.symbol.Symbol
import java.util.HashMap
import kotlin.reflect.KClass

/**
 * Utility for making repeated initialization of complex entities easier.
 * Describes what components an entity has, and can provide custom initialization for them.
 * Create entity instances with a single function call.
 *
 * @param entityInitializer An initialization function that will run after all the components have been created and initialized.
 * Can do any final holistic initialization needed, or use this to do all initialization.
 */
class SimpleEntityFactory(vararg componentTypes: KClass<out Component>,
                          var entityInitializer: ((context: InitializationContext) -> Unit)? = null): EntityFactoryBase() {

    private val context: InitializationContextImpl = InitializationContextImpl()

    val componentTypes: MutableList<KClass<out Component>> = ArrayList(componentTypes.toList())
    val initializers: MutableMap<KClass<out Component>, (context: InitializationContext, component: Component) -> Unit> = HashMap()

    /**
     * Specify initializer to run for the specified [componentType] when creating entities of this entityfactory.
     * The [initializer] has an [InitializationContext] as receiver (this value), that has a reference to the world,
     * the entity that the component belongs to, a random sequence, and any parameters passed in to the [createEntity] function.
     */
    fun <T: Component>setComponentInitializer(componentType: KClass<T>,
                                              initializer: (context: InitializationContext, component: T) -> Unit) {
        if (!componentTypes.contains(componentType)) componentTypes.add(componentType)
        initializers[componentType] = initializer as (context: InitializationContext, component: Component) -> Unit
    }


    override fun doCreateEntity(entity: Entity, random: Rand, parameters: Map<Symbol, Any>) {

        // Create seed to be used for initializing the components and final setup
        val entitySeed = random.nextLong()

        // Initialization context
        context.params = parameters
        context.entity = entity
        context.world = world
        context.random = random

        // Create components
        for (componentType in componentTypes) {
            // Create
            val component = entity.createComponent(componentType)

            // Initialize if needed
            val initializer = initializers[componentType]
            if (initializer != null) {
                // Initialize random seed for the component, base it on the component type as well.
                random.setSeed(entitySeed, componentType.hashCode().toLong())

                // Invoke initializer
                initializer(context, component)
            }
        }

        // Do any final entity initialization
        val entityInit = entityInitializer
        if (entityInit != null) {
            random.setSeed(entitySeed)
            entityInit(context)
        }
    }

}
