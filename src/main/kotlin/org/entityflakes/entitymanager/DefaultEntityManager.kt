package org.entityflakes.entitymanager

import org.entityflakes.Component
import org.entityflakes.Entity
import org.entityflakes.entityfactory.EntityFactory
import org.entityflakes.entityfilters.EntityFilter
import org.entityflakes.entitygroup.DynamicEntityGroup
import org.entityflakes.entitygroup.EntityGroup
import org.entityflakes.entitygroup.EntityGroupListener
import org.entityflakes.system.SystemBase
import org.kwrench.collections.BitVector
import org.kwrench.collections.bag.Bag
import org.kwrench.collections.bag.IntBag
import org.kwrench.symbol.Symbol
import org.kwrench.time.Time
import java.lang.IllegalArgumentException


/**
 * Default implementation of EntityManager.  Also implements Processor and EntitySupport.
 */
class DefaultEntityManager(val maxEntityPoolSize: Int = 1024*10): SystemBase(), EntityManagerSystem {

    private data class ListenerEntry(val entityFilter: EntityFilter, val listeners: Bag<EntityGroupListener>) {}

    private val entities = Bag<Entity>()
    private val freeEntityIds = IntBag()
    private val entitiesToRemove = IntBag()
    private val addedEntities = IntBag()
    private var nextFreeEntityId = 1
    private val entityPool = Bag<Entity>()

    private val componentPools = Bag<ComponentPool<Component>>()
    private val componentTypesToIds = LinkedHashMap<Class<Component>, Int>()
    private val ComponentRefs = HashMap<Class<out Component>, ComponentRef<out Component>>()
    private val taggedEntities = HashMap<Symbol, Entity>()
    private val inverseTaggedEntities = HashMap<Entity, MutableSet<Symbol>>()
    private var nextFreeComponentId = 1

    private val entityFactories = LinkedHashMap<Symbol, EntityFactory>()

    private val listenerEntries = Bag<ListenerEntry>()

    private val tempComponentIds = BitVector()

    override val maxComponentId: Int get() = nextFreeComponentId - 1

    private val groups = HashMap<EntityFilter, EntityGroup>()

    override val entityCount: Int get() = entities.size()

    override fun createEntity(vararg components: Component): Entity {
        // Get id
        var entityId = freeEntityIds.removeLast()
        if (entityId <= 0) entityId = nextFreeEntityId++

        // Create entity
        val entity = entityPool.removeLast() ?: Entity(this)
        entity.id = entityId
        entities.set(entityId, entity)

        // Add components
        for (component in components) {
            entity.set(component)
        }

        // Keep track of added entities for later listener notification in the build method
        addedEntities.add(entityId)

        return entity
    }

    override fun deleteEntity(entity: Entity) {
        // Handle entity deletion in the build method
        entitiesToRemove.add(entity.id)
    }

    override fun registerEntityFactory(factoryName: Symbol, factory: EntityFactory) {
        entityFactories[factoryName] = factory
        factory.init(world, factoryName)
    }

    override fun removeEntityFactory(factoryName: Symbol) {
        entityFactories.remove(factoryName)
    }

    override fun getEntityFactory(factoryName: Symbol): EntityFactory? {
        return entityFactories[factoryName]
    }

    override fun createEntity(factoryName: Symbol, randomSeed: Long?, parameters: Map<Symbol, Any>?): Entity {
        // Get factory and throw exception if it is null
        val factory = entityFactories[factoryName] ?: throw IllegalArgumentException("Could not create entity: No entity factory with the id '$factoryName' was found.")

        // Create entity using the factory
        return factory.createEntity(randomSeed, parameters)
    }

    override fun tagEntity(entity: Entity, tag: Symbol) {
        // Remove the tag from any previous entity that may have it
        removeTag(tag)

        // Store tag
        taggedEntities[tag] = entity

        // Keep track of what tags an entity has, for fast removal of tags when entities are removed
        var tags = inverseTaggedEntities[entity]
        if (tags == null) {
            tags = HashSet(3)
            inverseTaggedEntities[entity] = tags
        }
        tags.add(tag)
    }

    override fun getTaggedEntity(tag: Symbol): Entity? = taggedEntities[tag]

    override fun removeTag(tag: Symbol) {
        val entity = taggedEntities[tag]
        if (entity != null) {
            taggedEntities.remove(tag)

            val tags = inverseTaggedEntities[entity]
            if (tags != null) {
                tags.remove(tag)
                if (tags.isEmpty()) inverseTaggedEntities.remove(entity)
            }
        }
    }

    override fun getEntity(entityId: Int): Entity? = entities.get(entityId)

    override fun forEachEntity(entityVisitor: (Entity) -> Unit) {
        for (i in 0..entities.size()-1) {
            val entity = entities.get(i)
            if (entity != null) entityVisitor(entity)
        }
    }

    override fun <T : Component> createComponent(componentId: Int): T {
        return componentPools.get(componentId).createComponent() as T
    }

    override fun releaseComponent(componentId: Int, component: Component) {
        // Reset and pool the component if it is reusable and the pool has space, otherwise dispose it
        componentPools.get(componentId).releaseComponent(component)
    }

    override fun <T : Component> getComponentTypeId(componentType: Class<T>): Int {
        var componentId = componentTypesToIds.get(componentType as Class<Component>)
        if (componentId == null) {
            // Register a new component id
            componentId = nextFreeComponentId++
            componentTypesToIds.put(componentType as Class<Component>, componentId)
            componentPools.set(componentId, ComponentPool(componentType))
        }
        return componentId
    }

    override fun <T : Component> getComponentRef(componentType: Class<T>): ComponentRef<T> {
        var ref = ComponentRefs.get(componentType)
        if (ref == null) {
            ref = ComponentRef(componentType)
            ComponentRefs.put(componentType, ref)
        }
        return ref as ComponentRef<T>
    }


    override fun doUpdate(time: Time) {

        // Handle added entities
        // Use a do-while loop in case a listener triggers another entity addition
        do {
            val entityId = addedEntities.removeLast()
            if (entityId > 0) {
                val addedEntity = entities.get(entityId)

                // Mark entity as added
                addedEntity.entityAdditionCompleted = true

                // Notify listeners
                notifyEntityAdded(addedEntity)
            }
        } while (entityId > 0)

        // Handle entities to be removed
        // Use a do-while loop in case a listener triggers another entity deletion
        do {
            val entityId = entitiesToRemove.removeLast()
            if (entityId > 0) {
                val entityToRemove = entities.get(entityId)

                // Notify listeners
                notifyEntityRemoved(entityToRemove)

                // Remove any tags associated with the entity
                val tags = inverseTaggedEntities.get(entityToRemove)
                if (tags != null) {
                    tags.forEach {
                        taggedEntities.remove(it)
                    }
                    inverseTaggedEntities.remove(entityToRemove)
                }

                // Release all components of the entity, and reset the entity id
                entityToRemove.reset()

                // Mark the entity as unused and available
                entities.clear(entityId)
                freeEntityIds.add(entityId)

                // Pool the unused entity instance
                if (entityPool.size() < maxEntityPoolSize) entityPool.add(entityToRemove)
            }
        } while (entityId > 0)

    }

    private fun notifyEntityAdded(addedEntity: Entity) {
        for (listenerEntry in listenerEntries) {
            if (listenerEntry.entityFilter.matches(addedEntity.containedComponents)) {
                for (i in 0 until listenerEntry.listeners.size()) {
                    listenerEntry.listeners.get(i).onEntityAdded(addedEntity)
                }
            }
        }
    }

    private fun notifyEntityRemoved(removedEntity: Entity) {
        for (listenerEntry in listenerEntries) {
            if (listenerEntry.entityFilter.matches(removedEntity.containedComponents)) {
                for (i in 0 until listenerEntry.listeners.size()) {
                    listenerEntry.listeners.get(i).onEntityRemoved(removedEntity)
                }
            }
        }
    }

    override fun entityComponentAdded(entity: Entity, componentId: Int) {
        val newComponentIds = entity.containedComponents
        tempComponentIds.set(newComponentIds)
        tempComponentIds.clear(componentId)
        onEntityComponentsChanged(entity, tempComponentIds, newComponentIds)
    }

    override fun entityComponentRemoved(entity: Entity, componentId: Int) {
        val newComponentIds = entity.containedComponents
        tempComponentIds.set(newComponentIds)
        tempComponentIds.set(componentId)
        onEntityComponentsChanged(entity, tempComponentIds, newComponentIds)
    }

    private fun onEntityComponentsChanged(entity: Entity,
                                          oldComponentIds: BitVector,
                                          newComponentIds: BitVector) {
        for (listenerEntry in listenerEntries) {
            // TODO: move temp bitvector setup here
            val filter = listenerEntry.entityFilter
            val didMatch = filter.matches(oldComponentIds)
            val matchesNow = filter.matches(newComponentIds)

            // Check if did not previously match but matches now
            if (!didMatch && matchesNow) {
                for (i in 0 until listenerEntry.listeners.size()) {
                    listenerEntry.listeners.get(i).onEntityAdded(entity)
                }
            }
            // Check if did previously match but does not match now
            else if (didMatch && !matchesNow) {
                for (i in 0 until listenerEntry.listeners.size()) {
                    listenerEntry.listeners.get(i).onEntityRemoved(entity)
                }
            }
        }
    }

    override fun getEntityGroup(filter: EntityFilter): EntityGroup {
        return groups.getOrPut(filter) {
            DynamicEntityGroup(filter, this)
        }
    }

    override fun addListener(listener: EntityGroupListener, filter: EntityFilter) {
        // Search for existing entry with the same filter
        for (listenerEntry in listenerEntries) {
            if (listenerEntry.entityFilter == filter) {
                listenerEntry.listeners.add(listener)
            }
        }

        // Create new entry if none found
        val listeners = Bag<EntityGroupListener>()
        listeners.add(listener)
        listenerEntries.add(ListenerEntry(filter, listeners))
    }

    override fun removeListener(listener: EntityGroupListener) {
        for (listenerEntry in listenerEntries) {
            if (listenerEntry.listeners.remove(listener)) return
        }
    }

    /**
     * Disposes all entity components, removes all entities, clears all collections and resets the state of the entity manager to the start state.
     */
    override fun doDispose() {

        entities.forEach { it?.dispose() }
        componentPools.forEach { it?.dispose() }

        entities.clear()
        freeEntityIds.clear()
        entitiesToRemove.clear()
        addedEntities.clear()
        entityPool.clear()
        nextFreeEntityId = 1

        componentPools.clear()
        componentTypesToIds.clear()
        nextFreeComponentId = 1

        listenerEntries.clear()

        groups.clear()
    }



}