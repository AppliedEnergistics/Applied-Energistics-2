package appeng.server.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import appeng.core.AppEng;

/**
 * Implementation detail of {@link ChunkLoadingService} on Fabric, as {@code ForgeChunkManager} is not available there.
 */
class ChunkLoadState extends SavedData {

    private record ForcedChunk(int cx, int cz, List<BlockPos> blocks) {
        public static final Codec<ForcedChunk> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.INT.fieldOf("cx").forGetter(ForcedChunk::cx),
                Codec.INT.fieldOf("cz").forGetter(ForcedChunk::cz),
                BlockPos.CODEC.listOf().fieldOf("blocks").forGetter(ForcedChunk::blocks))
                .apply(builder, ForcedChunk::new));
    }

    private static final SavedDataType<ChunkLoadState> TYPE = new SavedDataType<>(
            AppEng.MOD_ID + "_chunk_load_state",
            context -> new ChunkLoadState(context.levelOrThrow()),
            context -> RecordCodecBuilder.create(builder -> builder.group(
                    ForcedChunk.CODEC.listOf().fieldOf("forcedChunks").forGetter(ChunkLoadState::getForcedChunks))
                    .apply(builder, data -> new ChunkLoadState(context.levelOrThrow(), data))));

    public static ChunkLoadState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    private final ServerLevel level;
    private final Long2ObjectMap<Set<BlockPos>> forceLoadedChunks = new Long2ObjectOpenHashMap<>();

    private ChunkLoadState(ServerLevel level) {
        this.level = level;
    }

    private ChunkLoadState(ServerLevel level, List<ForcedChunk> forcedChunks) {
        this(level);

        for (var forcedChunk : forcedChunks) {
            var chunkPos = new ChunkPos(forcedChunk.cx, forcedChunk.cz);
            var blockSet = new HashSet<>(forcedChunk.blocks);
            forceLoadedChunks.put(chunkPos.toLong(), blockSet);
        }
    }

    private List<ForcedChunk> getForcedChunks() {
        var result = new ArrayList<ForcedChunk>(forceLoadedChunks.size());
        for (var entry : forceLoadedChunks.long2ObjectEntrySet()) {
            var cx = ChunkPos.getX(entry.getLongKey());
            var cz = ChunkPos.getZ(entry.getLongKey());
            result.add(new ForcedChunk(cx, cz, List.copyOf(entry.getValue())));
        }
        return result;
    }

    /**
     * @param chunkPos  Chunk to load.
     * @param sourcePos Source of the chunk load request.
     */
    public void forceChunk(ChunkPos chunkPos, BlockPos sourcePos) {
        long chunk = chunkPos.toLong();
        forceLoadedChunks.computeIfAbsent(chunk, pos -> new HashSet<>()).add(sourcePos.immutable());

        level.setChunkForced(chunkPos.x, chunkPos.z, true);
        setDirty();
    }

    public void releaseChunk(ChunkPos chunkPos, BlockPos sourcePos) {
        var map = forceLoadedChunks.get(chunkPos.toLong());
        if (map == null) {
            return;
        }

        map.remove(sourcePos);
        if (map.isEmpty()) {
            forceLoadedChunks.remove(chunkPos.toLong());
            level.setChunkForced(chunkPos.x, chunkPos.z, false);
        }
        setDirty();
    }

    public void releaseAll(BlockPos sourcePos) {
        var relevantChunks = forceLoadedChunks.long2ObjectEntrySet()
                .stream()
                .filter(entry -> entry.getValue().contains(sourcePos))
                .mapToLong(Long2ObjectMap.Entry::getLongKey)
                .toArray();

        for (var chunk : relevantChunks) {
            releaseChunk(new ChunkPos(chunk), sourcePos);
        }
    }

    public Map<BlockPos, LongSet> getAllBlocks() {
        var blocks = new HashMap<BlockPos, LongSet>();
        for (var entry : forceLoadedChunks.long2ObjectEntrySet()) {
            for (var blockPos : entry.getValue()) {
                blocks.computeIfAbsent(blockPos, pos -> new LongOpenHashSet()).add(entry.getLongKey());
            }
        }
        return blocks;
    }

    public boolean isForceLoaded(int cx, int cz) {
        return forceLoadedChunks.containsKey(ChunkPos.asLong(cx, cz));
    }
}
