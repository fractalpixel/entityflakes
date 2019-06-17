package org.entityflakes

import org.entityflakes.entityfilters.RequiredComponentsFilter
import org.entityflakes.entitymanager.ComponentRef
import org.entityflakes.entitymanager.EntityManager
import org.entityflakes.processor.Processor
import org.entityflakes.processor.SimpleEntityProcessor
import org.entityflakes.processor.SimpleProcessor
import org.mistutils.service.Service
import org.mistutils.time.Time
import org.mistutils.updating.Updating
import org.mistutils.updating.strategies.UpdateStrategy
import org.mistutils.updating.strategies.VariableTimestepStrategy
import kotlin.reflect.KClass

/**
 * Manages entities and the systems that process them.
 */
interface World: Service, Updating, EntityManager {

    /**
     * The game-time for the World.
     * Note that different Processors may have own [Time] instances.
     */
    val time: Time

    /**
     * The currently registered processors
     */
    val processors: List<Processor>

    /**
     * @return the processor of the specified type.
     * If there are several processors of that type, returns the last one added.
     * Throws an exception if there was no processor of the specified type.
     */
    operator fun <T: Processor> get(processorType: KClass<T>): T = getOrNull(processorType)!!

    /**
     * @return the processor of the specified type, or null if not available.
     * If there are several processors of that type, returns the last one added.
     */
    fun <T: Processor> getOrNull(processorType: KClass<T>): T?

    /**
     * @return true if there is a processor of the specified type.
     */
    fun <T: Processor> has(processorType: KClass<T>): Boolean

    /**
     * Adds a processor.
     */
    fun <T : Processor> addProcessor(processor: T): T

    /**
     * Adds a processor with the specified update function and update strategy.
     * @param updateFunction function to call on each update of this processor.  Gets the world and the time object as parameters.
     * @param updateStrategy how often to run the update function of this processor. Defaults to once per world update.
     */
    fun addProcessor(updateFunction: (World, Time)->Unit,
                     updateStrategy: UpdateStrategy = VariableTimestepStrategy()): SimpleProcessor =
            addProcessor(SimpleProcessor(updateFunction, updateStrategy))

    /**
     * Add a processor that updates entities with the specified components.
     * @param requiredComponentTypes the component accessors for the components that must be preset for the added processor to process an entity.
     *                               If no components are specified, the processor will be applied to all entities.
     *                               Use World.getComponentRef() to get the component accessor of a component class.
     * @param entityUpdater the function to run for each entity.
     * @param updateStrategy how often to run the update function of this processor. Defaults to once per world update.
     */
    fun addEntityProcessor(requiredComponentTypes: Collection<ComponentRef<out Component>>,
                           entityUpdater: (Entity, Time) -> Unit,
                           updateStrategy: UpdateStrategy = VariableTimestepStrategy()): SimpleEntityProcessor =
            addProcessor(SimpleEntityProcessor(RequiredComponentsFilter(requiredComponentTypes.map{it.getComponentTypeId(this)}), entityUpdater, updateStrategy))

    /**
     * Removes a processor.
     * @return true if the processor was found and removed.
     */
    fun <T : Processor> removeProcessor(processor: T): Boolean

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