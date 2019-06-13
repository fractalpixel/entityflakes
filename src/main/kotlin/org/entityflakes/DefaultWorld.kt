package org.entityflakes

import org.entityflakes.entitymanager.DefaultEntityManager
import org.entityflakes.entitymanager.EntityManager
import org.entityflakes.entitymanager.EntityManagerProcessor
import org.entityflakes.processor.Processor
import org.mistutils.service.ServiceBase
import org.mistutils.service.ServiceProvider
import org.mistutils.time.RealTime
import org.mistutils.time.Time
import org.mistutils.updating.Updating
import org.mistutils.updating.strategies.FixedTimestepStrategy
import org.mistutils.updating.strategies.UpdateStrategy
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
                   val entityManager: EntityManagerProcessor = DefaultEntityManager()) :
        ServiceBase(), World, EntityManager by entityManager {

    override val processors = ArrayList<Processor>()
    private val processorsByType = HashMap<Class<out Processor>, Processor>()

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
        addProcessor(entityManager)
    }

    override fun <T : Processor> getOrNull(processorType: KClass<T>): T? = processorsByType[processorType.java] as T?
    override fun <T : Processor> has(processorType: KClass<T>): Boolean = processorsByType.containsKey(processorType.java)

    override fun <T : Processor> addProcessor(processor: T): T {
        if (shutdown) throw IllegalStateException("Can not add processors after dispose")
        processors.add(processor)
        processorsByType.put(processor.typeCategory, processor)

        // Tell the processor it was added (so that it can form a link to us)
        processor.onAddedToWorld(this)

        // If we are already initialized, initialize the processor
        if (active) processor.init(this)

        return processor
    }

    override fun <T : Processor> removeProcessor(processor: T): Boolean {
        val removed = processors.remove(processor)

        if (removed) {
            val processorType = processor.typeCategory
            processorsByType.remove(processorType)

            // Replace with last added processor of the same type
            val replacement = processors.findLast { it.typeCategory == processorType }
            if (replacement != null) processorsByType[processorType] = replacement
        }

        if (removed && active) processor.dispose()
        return removed
    }

    override fun doInit(serviceProvider: ServiceProvider?) {
        // Initialize processors
        for (processor in processors) {
            processor.init(this)
        }
    }

    override fun start() {
        // Initialize if not yet initialized
        if (!initialized) init()
        if (shutdown) throw IllegalStateException("Can not call start after dispose")

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
        if (!initialized) throw IllegalStateException("Init needs to be called before step")
        if (shutdown) throw IllegalStateException("Can not call step after dispose")

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
        for (processor in processors) {
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
        for (processor in processors.reversed()) {
            processor.dispose()
        }
    }


}