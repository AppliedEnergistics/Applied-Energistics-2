package appeng.hooks.ticking;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import appeng.tile.AEBaseBlockEntity;

/**
 * A class to hold data related to ticking tiles.
 */
class ServerTileRepo {

    private final Map<World, Long2ObjectMap<Queue<AEBaseBlockEntity>>> tiles = new Object2ObjectOpenHashMap<>();

    /**
     * Resets all internal data
     *
     */
    void clear() {
        this.tiles.clear();
    }

    /**
     * Add a new tile to be initializes in a later tick.
     */
    synchronized void addTile(AEBaseBlockEntity tile) {
        final World world = tile.getWorld();
        final int x = tile.getPos().getX() >> 4;
        final int z = tile.getPos().getZ() >> 4;
        final long chunkPos = ChunkPos.asLong(x, z);

        Long2ObjectMap<Queue<AEBaseBlockEntity>> worldQueue = this.tiles.get(world);

        Queue<AEBaseBlockEntity> queue = worldQueue.computeIfAbsent(chunkPos, key -> {
            return new ArrayDeque<>();
        });

        queue.add(tile);
    }

    /**
     * Sets up the necessary defaults when a new world is loaded
     */
    synchronized void addWorld(World world) {
        this.tiles.computeIfAbsent(world, (key) -> {
            return new Long2ObjectOpenHashMap<>();
        });
    }

    /**
     * Tears down data related to a now unloaded world
     */
    synchronized void removeWorld(World world) {
        this.tiles.remove(world);
    }

    /**
     * Removes a unloaded chunk within a world.
     *
     * There is no related addWorldChunk. The necessary queue will be created once the first tile is added to a chunk to
     * save memory.
     */
    synchronized void removeWorldChunk(World world, long chunkPos) {
        Map<Long, Queue<AEBaseBlockEntity>> queue = this.tiles.get(world);
        if (queue != null) {
            queue.remove(chunkPos);
        }
    }

    /**
     * Get the tiles needing to be initialized in this specific {@link World}.
     */
    public Long2ObjectMap<Queue<AEBaseBlockEntity>> getTiles(World world) {
        return tiles.get(world);
    }

}
