package org.entityflakes

import org.entityflakes.entitymanager.DefaultEntityManager
import org.entityflakes.entitymanager.EntityManager
import org.entityflakes.entitymanager.EntityManagerSystem
import org.entityflakes.system.System
import org.kwrench.service.ServiceBase
import org.kwrench.service.ServiceProvider
import org.kwrench.time.RealTime
import org.kwrench.time.Time
import org.kwrench.updating.Updating
import org.kwrench.updating.strategies.FixedTimestepStrategy
import org.kwrench.updating.strategies.UpdateStrategy
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

/**
 * Default implementation of World.
 */
class DefaultWorld(var updateStrategy: UpdateStrategy = FixedTimestepStrategy(),
                   override var time: Time = RealTime(),
                   var targetUpdatesPerSecond: Double = 100.0,
                   val sleepForSurplusTime: Boolean = true,
                   val entityManager: EntityManagerSystem = DefaultEntityManager()) :
        ServiceBase(), World, EntityManager by entityManager {

    override val systems = ArrayList<System>()
    private val processorsByType = HashMap<Class<out System>, System>()

    private val stopRequested_: AtomicBoolean = AtomicBoolean(false)
    private val running: AtomicBoolean = AtomicBoolean(false)
    private val shutdownAfterStop: AtomicBoolean = AtomicBoolean(false)

    override val stopRequested: Boolean get() = stopRequested_.get()

    private val thisUpdating = object : Updating {
        override fun update(time: Time) {
            doUpdate(time)
        }
    }

    init {
        addSystem(entityManager)
    }

    override fun <T : System> getOrNull(processorType: KClass<T>): T? = processorsByType[processorType.java] as T?
    override fun <T : System> has(processorType: KClass<T>): Boolean = processorsByType.containsKey(processorType.java)

    override fun <T : System> addSystem(processor: T): T {
        check(!shutdown) { "Can not add processors after dispose" }
        systems.add(processor)
        processorsByType.put(processor.typeCategory, processor)

        // Tell the processor it was added (so that it can form a link to us)
        processor.onAddedToWorld(this)

        // If we are already initialized, initialize the processor
        if (active) processor.init(this)

        return processor
    }

    override fun <T : System> removeSystem(processor: T): Boolean {
        val removed = systems.remove(processor)

        if (removed) {
            val processorType = processor.typeCategory
            processorsByType.remove(processorType)

            // Replace with last added processor of the same type
            val replacement = systems.findLast { it.typeCategory == processorType }
            if (replacement != null) processorsByType[processorType] = replacement
        }

        if (removed && active) processor.dispose()
        return removed
    }

    override fun doInit(serviceProvider: ServiceProvider?) {
        // Initialize processors
        for (processor in systems) {
            processor.init(this)
        }
    }

    override fun start() {
        // Initialize if not yet initialized
        if (!initialized) init()
        check(!shutdown) { "Can not call start after dispose" }

        // Loop until stop or dispose is called
        stopRequested_.set(false)
        running.set(true)
        while (!stopRequested_.get()) {
            step()
        }
        running.set(false)

        // Check if shutdown was requested
        if (shutdownAfterStop.getAndSet(false)) {
            shutdown()
        }
    }

    override fun step() {
        check(initialized) { "Init needs to be called before step" }
        check(!shutdown) { "Can not call step after dispose" }

        // Step timer
        time.nextStep()

        // Update
        update(time)

        // Sleep if we have surplus time
        if (sleepForSurplusTime) time.sleepUntilTargetUpdatesPerSecond(targetUpdatesPerSecond)
    }

    override fun update(time: Time) {
        updateStrategy.update(thisUpdating, time)
    }

    private fun doUpdate(time: Time) {
        for (processor in systems) {
            processor.update(time)
        }
    }

    override fun stop() {
        stopRequested_.set(true)
    }

    override fun shutdown() {
        if (running.get()) {
            // We are still running, after stopping call shutdown again
            shutdownAfterStop.set(true)
            stop()
        } else {
            super<ServiceBase>.shutdown()
        }
    }

    override fun doShutdown() {
        // Dispose processors
        for (processor in systems.reversed()) {
            processor.dispose()
        }
    }


}