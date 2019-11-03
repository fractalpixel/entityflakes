package org.entityflakes

import org.entityflakes.entitymanager.ComponentRef
import org.entityflakes.entitymanager.EntityManager
import org.entityflakes.system.System
import org.entityflakes.system.SimpleEntitySystem
import org.entityflakes.system.SimpleSystem
import org.kwrench.service.Service
import org.kwrench.time.Time
import org.kwrench.updating.Updating
import org.kwrench.updating.strategies.UpdateStrategy
import org.kwrench.updating.strategies.VariableTimestepStrategy
import java.lang.IllegalStateException
import kotlin.reflect.KClass

/**
 * Manages entities and the systems that process them.
 */
interface World: Service, Updating, EntityManager {

    /**
     * The game-time for the World.
     * Note that different systems may have own [Time] instances.
     */
    val time: Time

    /**
     * The currently registered systems
     */
    val systems: List<System>

    /**
     * @return the processor of the specified type.
     * If there are several systems of that type, returns the last one added.
     * Throws an exception if there was no system of the specified type.
     */
    operator fun <T: System> get(systemType: KClass<T>): T = getOrNull(systemType) ?: throw IllegalStateException("No '${systemType.simpleName}' -system has been added to the world!")

    /**
     * @return the system of the specified type, or null if not available.
     * If there are several systems of that type, returns the last one added.
     */
    fun <T: System> getOrNull(systemType: KClass<T>): T?

    /**
     * @return true if there is a system of the specified type.
     */
    fun <T: System> has(systemType: KClass<T>): Boolean

    /**
     * Adds a system.
     */
    fun <T : System> addSystem(system: T): T

    /**
     * Adds a processor with the specified update function and update strategy.
     * @param updateFunction function to call on each update of this processor.  Gets the world and the time object as parameters.
     * @param updateStrategy how often to run the update function of this processor. Defaults to once per world update.
     */
    fun addSystem(updateStrategy: UpdateStrategy = VariableTimestepStrategy(),
                  updateFunction: (World, Time)->Unit): SimpleSystem =
            addSystem(SimpleSystem(updateStrategy, updateFunction))

    /**
     * Add a processor that updates entities with the specified components.
     *
     * @param requiredComponentTypes the types of the components that must be preset for the added processor to process an entity.
     *                               If no components are specified, the processor will be applied to all entities.
     * @param updateStrategy how often to run the update function of this processor. Defaults to once per world update.
     *                       (Note that you need to pass this as a named argument, as the preceding [requiredComponentTypes] is a vararg).
     * @param entityUpdater the function to run for each entity.  (Pass either as a named argument, or a function block).
     */
    fun addEntitySystem(vararg requiredComponentTypes: KClass<out Component>,
                        updateStrategy: UpdateStrategy = VariableTimestepStrategy(),
                        entityUpdater: (Entity, Time) -> Unit): SimpleEntitySystem =
            addSystem(SimpleEntitySystem(
                    this,
                    *requiredComponentTypes,
                    updateStrategy= updateStrategy,
                    entityUpdater= entityUpdater))

    /**
     * Add a processor that updates entities with the specified components.
     *
     * @param requiredComponentTypes the types for the components that must be preset for the added processor to process an entity.
     *                               If no components are specified, the processor will be applied to all entities.
     * @param updateStrategy how often to run the update function of this processor. Defaults to once per world update.
     *                       (Note that you need to pass this as a named argument, as the preceding [requiredComponentTypes] is a vararg).
     * @param entityUpdater the function to run for each entity.  (Pass either as a named argument, or a function block).
     */
    fun addEntitySystem(requiredComponentTypes: Collection<KClass<Component>>,
                        updateStrategy: UpdateStrategy = VariableTimestepStrategy(),
                        entityUpdater: (Entity, Time) -> Unit): SimpleEntitySystem =
            addEntitySystem(*requiredComponentTypes.toTypedArray(), updateStrategy = updateStrategy, entityUpdater = entityUpdater)

    /**
     * Add a processor that updates entities with the specified components.
     *
     * First in the parameter list, pass in the component accessors for the components that must be preset for
     * the added processor to process an entity (at least one must be specified).  Use World.getComponentRef() to get the component accessor of a component class.
     *
     * @param updateStrategy how often to run the update function of this processor. Defaults to once per world update.
     *                       (Note that you need to pass this as a named argument, as the preceding [requiredComponentTypes] is a vararg).
     * @param entityUpdater the function to run for each entity.  (Pass either as a named argument, or a function block).
     */
    fun addEntitySystem(firstRequiredComponentRef: ComponentRef<out Component>,
                        vararg additionalRequiredComponentRefs: ComponentRef<out Component>,
                        updateStrategy: UpdateStrategy = VariableTimestepStrategy(),
                        entityUpdater: (Entity, Time) -> Unit): SimpleEntitySystem =
            addSystem(SimpleEntitySystem(
                    this,
                    firstRequiredComponentRef,
                    *additionalRequiredComponentRefs,
                    updateStrategy= updateStrategy,
                    entityUpdater= entityUpdater))

    /**
     * Removes a processor.
     * @return true if the processor was found and removed.
     */
    fun <T : System> removeSystem(processor: T): Boolean

    /**
     * Start running an update loop, repeatedly calling step.
     * If init has not been called, it will be called first.
     * Keeps control until stop or dispose is called.
     */
    fun start()

    /**
     * Stops the update loop when it next reaches the end.
     */
    fun stop()

    /**
     * True if stop has been called.
     */
    val stopRequested: Boolean

    /**
     * Runs one update step for the world, updating the time and all the processors.
     * This is called automatically from start, but can also be called manually if start or update is not used.
     * init() needs to be called before this function is called.
     */
    fun step()

}