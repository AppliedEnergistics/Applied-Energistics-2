package appeng.server.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import appeng.core.AppEng;

/**
 * Implementation detail of {@link ChunkLoadingService} on Fabric, as {@code ForgeChunkManager} is not available there.
 */
class ChunkLoadState extends SavedData {

    public static final String NAME = AppEng.MOD_ID + "_chunk_load_state";

    public static ChunkLoadState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        () -> new ChunkLoadState(level),
                        (tag, provider) -> new ChunkLoadState(level, tag),
                        null),
                NAME);
    }

    private final ServerLevel level;
    private final Long2ObjectMap<Set<BlockPos>> forceLoadedChunks = new Long2ObjectOpenHashMap<>();

    private ChunkLoadState(ServerLevel level) {
        this.level = level;
    }

    private ChunkLoadState(ServerLevel level, CompoundTag tag) {
        this(level);
        var forcedChunks = tag.getList("forcedChunks", Tag.TAG_COMPOUND);
        for (int i = 0; i < forcedChunks.size(); ++i) {
            var forcedChunk = forcedChunks.getCompound(i);
            var chunkPos = new ChunkPos(forcedChunk.getInt("cx"), forcedChunk.getInt("cz"));

            var blockSet = new HashSet<BlockPos>();
            for (long blockPos : forcedChunk.getLongArray("blocks")) {
                blockSet.add(BlockPos.of(blockPos));
            }

            forceLoadedChunks.put(chunkPos.toLong(), blockSet);
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        var forcedChunks = new ListTag();
        for (var entry : forceLoadedChunks.long2ObjectEntrySet()) {
            var chunkPos = new ChunkPos(entry.getLongKey());

            var forcedChunk = new CompoundTag();
            forcedChunk.putInt("cx", chunkPos.x);
            forcedChunk.putInt("cz", chunkPos.z);

            var list = new LongArrayTag(entry.getValue().stream().map(BlockPos::asLong).toList());
            forcedChunk.put("blocks", list);

            forcedChunks.add(forcedChunk);
        }
        tag.put("forcedChunks", forcedChunks);
        return tag;
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
