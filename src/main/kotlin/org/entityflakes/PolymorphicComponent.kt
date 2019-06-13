package org.entityflakes

/**
 * Components that can have inheritance structures, where an entity can have only one of the components that implement
 * the same component interface at once.
 *
 * E.g. an entity can have an Appearance component, but there are several different appearance component types,
 * such as box, sphere, model, etc.
 */
interface PolymorphicComponent : Component {

    /**
     * The category that this component belongs to (the common parent interface/class for components of this type).
     * An entity can only have one component of a specific category.
     * This allows component interfaces that can have different implementations, but are used for the same purpose.
     * Defaults to the type of this component.
     */
    val componentCategory: Class<out Component> get() = this.javaClass

}