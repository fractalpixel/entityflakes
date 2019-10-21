package org.entityflakes.entitymanager

import org.entityflakes.processor.Processor

/**
 * Interface for class that manages entities, is called regularly by the world,
 * and implements the EntitySupport interface, providing internal entity related utility functions.
 */
interface EntityManagerProcessor: EntityManager, Processor, EntitySupport