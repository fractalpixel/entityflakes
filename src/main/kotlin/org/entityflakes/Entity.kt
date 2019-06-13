package org.entityflakes

import org.entityflakes.entitymanager.ComponentRef
import org.entityflakes.entitymanager.EntitySupport
import org.mistutils.classes.createInstance
import org.mistutils.collections.BitVector
import java.util.*
import kotlin.reflect.KClass

/**
 * Represents some object in a World.  The Entity has no state itself, it is instead composed
 * of components that describe different aspects of the entity, and carry state for that aspect.
 * <p>
 * E.g. one component could contain the position of the entity in the world, another the appearance,
 * a third contain properties needed for entities that should be simulated with a physics engine, and so on.
 * <p>
 * It is faster to access components of an entity with componentIds instead of the component type, especially in tight loops.
 * The EntityManager (also implemented by the World) can be used to get the componentId of a specific component type.
 * <p>
 * Entities should not be instantiated by client code, instead use world.createEntity()
 */
// TODO: store components and component ids(?) in a bag (and intbag?)
class Entity internal constructor(val entitySupport: EntitySupport, var id: Int = 0) {

    private var components = Array<Component?>(INITIAL_SIZE, {null})
    internal val containedComponents = BitVector()
    internal var entityAdditionCompleted: Boolean = false
    private var listeners: ArrayList<EntityListener>? = null

    val world: World get() = entitySupport.world

    /**
     * Sets the specified component in this entity to the specified value, replacing any earlier component of that type.
     */
    operator fun <T: Component>set(componentRef: ComponentRef<T>, component: T) = set(componentRef.getComponentTypeId(world), component)

    /**
     * Sets the specified component in this entity to the specified value, replacing any earlier component of that type.
     */
    operator fun set(componentId: Int, component: Component?) {
        if (componentId < 1 || componentId > entitySupport.maxComponentId) throw IllegalArgumentException("component id $componentId is out of range (1 .. ${entitySupport.maxComponentId})")

        // Grow components array if necessary
        if (componentId > components.size) {
            if (component == null) return // We don't have that component, and no component was assigned to that id, so nothing to do.
            components = components.copyOf(componentId + EXTRA_SIZE)
        }

        // Set component
        val oldComponent = components[componentId - 1]
        if (oldComponent != component) {
            components[componentId - 1] = component

            // Update contained components
            if (oldComponent == null && component != null) {
                containedComponents.set(componentId)
            }
            else if (oldComponent != null && component == null) {
                containedComponents.clear(componentId)
            }

            // Initialize new component, if it is non-null
            component?.init(this)

            // Notify entity system if this is not a newly added entity
            if (entityAdditionCompleted) {
                if (oldComponent == null && component != null) {
                    entitySupport.entityComponentAdded(this, componentId)
                }
                else if (oldComponent != null && component == null) {
                    entitySupport.entityComponentRemoved(this, componentId)
                }
            }

            // Dispose and release old component
            if (oldComponent != null) entitySupport.releaseComponent(componentId, oldComponent)
        }
    }

    /**
     * Add the specified component to this entity, replacing any component with the same type.
     * @return the component for chaining manipulation.
     */
    fun <T: Component> set(component: T): T {
        set(world.getComponentTypeId(component), component)
        return component
    }

    /**
     * @return the component with the specified type id, or null if this entity does not have such a component.
     */
    operator fun <T: Component> get(componentId: Int): T? {
        if (componentId <= 0 || componentId > components.size) return null
        else return components[componentId-1] as T?
    }

    /**
     * @return the component with the specified component accessor.
     * Throws an exception if this entity does not have such a component.
     */
    operator fun <T: Component> get(componentRef: ComponentRef<T>): T? = get(componentRef.getComponentTypeId(world))

    /**
     * @return the component with the specified type, or null if this entity does not have such a component.
     * Note that this is slightly slower than looking up a component by componentTypeId or ComponentRef,
     * so should not be used in frequent update loops.
     */
    operator fun <T: Component> get(componentType: Class<T>): T? =
            get(world.getComponentTypeId(componentType))

    /**
     * @return the component with the specified type, or null if this entity does not have such a component.
     * Note that this is slightly slower than looking up a component by componentTypeId or ComponentRef,
     * so should not be used in frequent update loops.
     */
    operator fun <T: Component> get(componentType: KClass<T>): T? =
            get(componentType.java)

    /**
     * Create and add a component of the specified type to this entity.
     * Returns the created component.
     */
    fun <T: Component>createComponent(componentType: KClass<T>): T  {
        return if (PolymorphicComponent::class.java.isAssignableFrom(componentType.java)) {
            // Polymorphic components are not pooled, instantiate it directly.
            val component = componentType.createInstance(emptyList())
            set(component)
            component
        }
        else {
            createComponent(entitySupport.world.getComponentTypeId(componentType.java))
        }
    }

    /**
     * Create and add a component of the specified type to this entity.
     * Returns the created component.
     *
     * Note: only works for component types with no type hierarchy.
     * For component types that inherit from a common ancestor component type, create the component instance manually and
     * call setComponent instead.
     */
    fun <T: Component>createComponent(componentType: ComponentRef<T>): T =
            createComponent(componentType.getComponentTypeId(world))

    /**
     * Create and add a component to this entity.
     * Returns the created component.
     * Throws an exception if the component could not be instantiated.
     */
    fun <T: Component>createComponent(componentId: Int): T {
        val component = entitySupport.createComponent<Component>(componentId)
        set(componentId, component)
        return component as T
    }

    /**
     * Removes the specified component from this entity.
     */
    fun deleteComponent(componentId: Int) = set(componentId, null)

    /**
     * Removes the component of the specified type from this entity.
     */
    fun deleteComponent(componentType: ComponentRef<out Component>) {
        deleteComponent(componentType.getComponentTypeId(world))
    }

    /**
     * Removes the component of the specified type from this entity.
     */
    fun <T: Component>deleteComponent(componentType: KClass<T>) =
            deleteComponent(entitySupport.world.getComponentTypeId(componentType))

    /**
     * Removes all components from this entity, but does not delete the entity.
     * (If you want to delete the entity, just call delete() instead, it is more efficient).
     */
    fun removeAllComponents() {
        if (entityAdditionCompleted) {
            for (i in 0 .. components.size) {
                if (components[i] != null) {
                    components[i] = null
                    entitySupport.entityComponentRemoved(this, i+1)
                }
            }
        }
    }

    /**
     * True if this entity contains all the specified components
     */
    fun containsAll(componentIds: BitVector): Boolean = containedComponents.containsAll(componentIds)

    /**
     * True if this entity contains any of the specified components
     */
    fun containsAny(componentIds: BitVector): Boolean = containedComponents.intersects(componentIds)

    /**
     * @return true if this entity contains the specified component type
     */
    fun contains(componentId: Int): Boolean {
        if (componentId <= 0 || componentId > components.size) return false
        else return components[componentId-1] != null
    }

    /**
     * Add a listener that is notified when this entity is removed.
     */
    fun addListener(listener: EntityListener) {
        if (listeners == null) listeners = ArrayList(4)
        listeners!!.add(listener)
    }

    /**
     * Remove a listener.
     */
    fun removeListener(listener: EntityListener) {
        listeners?.remove(listener)
    }

    /**
     * Marks this entity for deletion at the next update step.
     */
    fun delete() {
        entitySupport.world.deleteEntity(this)
        notifyListenersAboutDeletion()
    }

    /**
     * Called when the entity is deleted, by the rest of the framework.
     */
    internal fun reset(disposeComponents: Boolean = false) {
        notifyListenersAboutDeletion()

        for (i in 0..components.size-1) {
            val component = components[i]
            if (component != null) {
                if (disposeComponents) component.dispose()
                else entitySupport.releaseComponent(i+1, component)
                components[i] = null
            }
        }

        containedComponents.clear()
        entityAdditionCompleted = false
        id = 0
    }

    private fun notifyListenersAboutDeletion() {
        if (listeners != null) {
            for (i in 0 .. listeners!!.size - 1) {
                listeners!![i].onEntityRemoved(this)
            }
            listeners?.clear()
        }
    }

    /**
     * Called when the world shuts down, disposes the components in the entity without releasing them.
     */
    internal fun dispose() = reset(true)

    companion object {
        private val INITIAL_SIZE = 4
        private val EXTRA_SIZE = 3
    }
}