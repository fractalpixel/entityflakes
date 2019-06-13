package org.entityflakes

import org.entityflakes.entitygroup.EntityGroupListener

/**
 * A reference to an entity.
 * Will become null if the referenced entity is deleted from the world.
 * The referenced entity can be changed.
 */
class EntityRef() {

    private var referencedEntityId: Int = 0
    private var world: World? = null
    private @Transient var cachedRef: Entity? = null
    private @Transient var listenerRegistered: Boolean = false
    private val listener: EntityGroupListener = object : EntityGroupListener {
        override fun onEntityAdded(entity: Entity) {
        }

        override fun onEntityRemoved(entity: Entity) {
            if (entity.id == referencedEntityId) {
                removeListener()
                cachedRef = null
                referencedEntityId = 0
            }
        }

    }

    /**
     * @param referencedEntity the referenced entity, or null if the reference doesn't exist at the moment.
     */
    constructor(referencedEntity: Entity): this() {
        cachedRef = referencedEntity
        referencedEntityId = referencedEntity.id
        world = referencedEntity.world
        addListener()
    }

    /**
     * @param world the world that the entity is stored in
     * @param entityId the id of the entity
     */
    constructor(world: World, entityId: Int): this() {
        this.world = world
        referencedEntityId = entityId
        if (referencedEntityId != 0) {
            addListener()
        }
    }

    /**
     * The referenced entity.
     * Can be changed.
     * If the referenced entity is deleted, this will become null.
     */
    var entity: Entity?
        get(): Entity? {
            if (referencedEntityId == 0) return null
            else {
                if (cachedRef == null) {
                    cachedRef = world!!.getEntity(referencedEntityId)
                }
                return cachedRef
            }
        }
        set(newEntity) {
            val newId = newEntity?.id ?: 0
            if (newId != referencedEntityId) {
                referencedEntityId = newId

                if (referencedEntityId == 0) {
                    removeListener()
                    cachedRef = null
                }
                else {
                    // Init world if needed
                    if (world == null) world = newEntity!!.world

                    cachedRef = newEntity
                    addListener()
                }
            }
        }

    private fun removeListener() {
        if (listenerRegistered) {
            listenerRegistered = false
            world!!.removeListener(listener)
        }
    }

    private fun addListener() {
        if (!listenerRegistered) {
            listenerRegistered = true
            world!!.addListener(listener)
        }
    }

}