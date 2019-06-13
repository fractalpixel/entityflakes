package org.entityflakes.entityfactory

import org.entityflakes.Component
import org.entityflakes.Entity
import org.entityflakes.World
import org.mistutils.random.RandomSequence
import org.mistutils.symbol.Symbol
import java.util.HashMap
import kotlin.reflect.KClass


/**
 * Utility for making repeated initialization of complex entities easier.
 * Describes what components an entity has, and can provide custom initialization for them.
 * Create entity instances with a single function call.
 */
// NOTE: API for this may still change.
// TODO: Perhaps create an interface for component initializers, so that it's easier to use from java as well.  In general usage from java needs work.
class DefaultEntityFactory(vararg componentTypes: KClass<out Component>): EntityFactoryBase() {

    val componentTypes: MutableList<KClass<out Component>> = ArrayList(componentTypes.toList())
    val initializers: MutableMap<KClass<out Component>, InitializationContext.(component: Component) -> Unit> = HashMap()

    /**
     * An initialization function that will run after all the components have been created and initialized.
     * Can do any final holistic initialization needed.
     */
    var entityInitializer: (InitializationContext.(entity: Entity) -> Unit)? = null


    private val random = RandomSequence.createDefault()
    private lateinit var context: InitializationContextImpl

    override fun doInit(world: World) {
        context = InitializationContextImpl(world, random)
    }

    /**
     * Specify initializer to run for the specified [componentType] when creating entities of this entityfactory.
     * The [initializer] has an [InitializationContext] as reciever (this value), that has a reference to the world,
     * the entity that the component belongs to, a random sequence, and any parameters passed in to the [createEntity] function.
     */
    fun <T: Component>setComponentInitializer(componentType: KClass<T>,
                                              initializer: InitializationContext.(component: T) -> Unit) {
        if (!componentTypes.contains(componentType)) componentTypes.add(componentType)
        initializers[componentType] = initializer as InitializationContext.(component: Component) -> Unit
    }

    override fun doCreateEntity(world: World, randomSeed: Long, parameters: Map<Symbol, Any>): Entity {

        // Create entity
        val entity = world.createEntity()

        // Initialization context
        context.params = parameters
        context.entity = entity

        // Create components
        for (componentType in componentTypes) {
            // Create
            val component = entity.createComponent(componentType)

            // Initialize if needed
            val initializer = initializers[componentType]
            if (initializer != null) {
                // Initialize random seed for the component, base it on the component type as well.
                random.setSeed(randomSeed, componentType.hashCode().toLong())

                // Invoke initializer
                context.initializer(component)
            }
        }

        // Do any final entity initialization
        val entityInit = entityInitializer
        if (entityInit != null) {
            random.setSeed(randomSeed)
            context.entityInit(entity)
        }

        return entity
    }

}
