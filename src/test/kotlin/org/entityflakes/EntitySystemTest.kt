package org.entityflakes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.kwrench.geometry.double2.MutableDouble2
import org.kwrench.updating.strategies.CappedFixedTimestepStrategy

/**
 *
 */
class EntitySystemTest {

    @Test
    fun testEntitySystem() {
        val world = DefaultWorld()

        val named = world.getComponentRef(Named::class)
        val positioned = world.getComponentRef(Positioned::class)

        var stopTime = 1

        world.addSystem(){ world, time ->
            if (time.secondsSinceStart > stopTime) world.stop()
        }

        world.addEntitySystem(named) { entity, time ->
            entity[named]!!.name = "Igor"+time.secondsSinceStart
        }

        world.addEntitySystem(Positioned::class) { entity, time ->
            val position: Positioned = entity[positioned]!!
            position.pos.x += time.stepDurationSeconds * 1.0
        }

        world.addEntitySystem(Named::class, Positioned::class) { entity, time ->
            entity[named]!!.name = "Orcus" + entity[positioned]!!.pos.x
        }

        world.addEntitySystem(updateStrategy = CappedFixedTimestepStrategy(0.5)) { entity, time ->
            println("${time.secondsSinceStart}: entity id: ${entity.id}, name: ${entity[named]?.name}, pos: ${entity[positioned]?.pos}")
        }

        world.createEntityWithTypes(named, positioned)
        val e2 = world.createEntityWithTypes(named, positioned)
        world.createEntityWithTypes(named)
        world.createEntityWithTypes(named)
        val e5 = world.createEntityWithTypes(positioned)
        world.createEntityWithTypes(positioned)
        world.createEntityWithTypes()
        world.createEntityWithTypes()
        val id5 = e5.id

        // Test group
        val namedPositionedEntities = world.getEntityGroup(named, positioned)
        assertTrue(namedPositionedEntities.contains(e2))
        assertTrue(!namedPositionedEntities.contains(e5))

        // Test running world
        world.start()

        // Try creating components
        val n = e5.createComponent(named)
        n.name = "muppets"
        assertEquals("muppets", world.getEntity(id5)?.get(named)?.name)
        assertTrue(namedPositionedEntities.contains(e5))

        // Try adding entity
        world.createEntityWithTypes()
        world.createEntityWithTypes(named, positioned)

        // Try deleting entity
        assertEquals(e5, world.getEntity(id5))
        e5.delete()
        assertEquals(e5, world.getEntity(id5))
        assertTrue(namedPositionedEntities.contains(e5))
        world.step()
        assertEquals(null, world.getEntity(id5))
        assertTrue(!namedPositionedEntities.contains(e5))

        // Try adding entity
        world.createEntityWithTypes(named, positioned)
        world.createEntityWithTypes()

        // Try deleting component
        assertTrue(e2[positioned] != null)
        e2.deleteComponent(named)
        e2.deleteComponent(positioned)
        assertTrue(e2[positioned] == null)

        // Add it back again and again
        assertEquals(0.0, e2.createComponent(positioned).pos.x, 0.00001)
        e2[positioned]!!.pos.x = 2.0
        assertEquals(0.0, e2.createComponent(positioned).pos.x, 0.00001)
        e2[positioned]!!.pos.x = 2.0
        assertEquals(0.0, e2.createComponent(positioned).pos.x, 0.00001)

        stopTime = 2
        world.start()

        world.shutdown()
    }


}


data class Named(var name: String = ""): ReusableComponent {
    override fun reset() {
        name = ""
    }
}

data class Positioned(val pos: MutableDouble2 = MutableDouble2()): ReusableComponent {
    override fun reset() {
        pos.zero()
    }
}

