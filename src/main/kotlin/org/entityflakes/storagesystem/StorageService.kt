package org.entityflakes.storagesystem

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import org.entityflakes.Entity
import org.entityflakes.World
import org.entityflakes.system.SystemBase
import org.kwrench.properties.threadLocal
import org.kwrench.time.Time
import org.kwrench.updating.strategies.FixedTimestepStrategy
import org.kwrench.updating.strategies.VariableTimestepStrategy
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Every x minutes, serialize to buffer, save on other thread
 */
class StorageService(val saveFile: File = File("world_save.bin"),
                     updateIntervalSeconds: Double = 10.0 * 60.0): SystemBase(FixedTimestepStrategy(updateIntervalSeconds)) {

    private val threadLocalKryo = threadLocal{
        val kryo = Kryo()
        kryo
    }

    private val threadLocalOutput = threadLocal{
        Output()
    }

    override fun doInit(world: World) {
        // Load world
    }

    override fun doUpdate(time: Time) {
    }

/* TODO: Implement

    fun save(worldFile: File = saveFile) {
        val kryo = threadLocalKryo.get()

        val output = threadLocalOutput.get()
        output.reset()


        // Serialize all entities
        world.forEachEntity {
            kryo.writeObject(output, it)
        }



        output.close()
    }

    fun load(worldFile: File = saveFile) {
        val kryo = threadLocalKryo.get()

        val input = Input(FileInputStream(worldFile))

        var entity = kryo.readObjectOrNull(input, Entity::class.java)
        while (entity != null) {
            val createdEntity = world.createEntity()
            entity = kryo.readObjectOrNull(input, Entity::class.java)
        }

        input.close()
    }
*/
    override fun doDispose() {
    }
}