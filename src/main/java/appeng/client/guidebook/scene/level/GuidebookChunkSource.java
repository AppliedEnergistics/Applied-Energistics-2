package appeng.client.guidebook.scene.level;

import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

class GuidebookChunkSource extends ChunkSource {
    private final GuidebookLevel level;

    private final Long2ObjectMap<GuidebookChunk> chunks = new Long2ObjectOpenHashMap<>();

    private final LevelLightEngine lightEngine;

    public GuidebookChunkSource(GuidebookLevel level) {
        this.level = level;
        this.lightEngine = new LevelLightEngine(this, true, true);
    }

    @Nullable
    @Override
    public ChunkAccess getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean load) {
        var chunkKey = ChunkPos.asLong(chunkX, chunkZ);
        var chunk = chunks.get(chunkKey);
        if (chunk == null) {
            chunk = new GuidebookChunk(level, new ChunkPos(chunkX, chunkZ));
            chunks.put(chunkKey, chunk);
        }
        return chunk;
    }

    @Override
    public void tick(BooleanSupplier booleanSupplier, boolean bl) {
    }

    @Override
    public String gatherStats() {
        return "";
    }

    @Override
    public int getLoadedChunksCount() {
        return 0;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return lightEngine;
    }

    @Override
    public BlockGetter getLevel() {
        return level;
    }
}
