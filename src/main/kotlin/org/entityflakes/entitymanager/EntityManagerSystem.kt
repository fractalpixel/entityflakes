package org.entityflakes.entitymanager

import org.entityflakes.system.System

/**
 * Interface for class that manages entities, is called regularly by the world,
 * and implements the EntitySupport interface, providing internal entity related utility functions.
 */
interface EntityManagerSystem: EntityManager, System, EntitySupport