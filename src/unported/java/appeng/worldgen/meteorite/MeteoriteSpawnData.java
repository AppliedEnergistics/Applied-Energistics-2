package appeng.worldgen.meteorite;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/**
 * This is per-world data to track where Meteorites have been spawned.
 */
public final class MeteoriteSpawnData {

    private static final String NAME = "ae2_meteorite_spawns";

    private final SavedData data;

    private MeteoriteSpawnData(SavedData data) {
        this.data = data;
    }

    public void setGenerated(ChunkPos chunkPos) {
        synchronized (data) {
            // edit.
            data.generated.add(chunkPos.asLong());
            data.markDirty();
        }
    }

    public boolean hasGenerated(ChunkPos pos) {
        synchronized (data) {
            return data.generated.contains(pos.asLong());
        }
    }

    @Nullable
    private CompoundTag getSpawnData(int chunkX, int chunkZ, boolean create) {
        chunkX /= 16;
        chunkZ /= 16;
        String key = chunkX + "," + chunkZ;
        Tag inbt = data.spawns.get(key);
        if (!(inbt instanceof CompoundTag)) {
            if (create) {
                inbt = new CompoundTag();
                data.spawns.put(key, inbt);
                data.markDirty();
            } else {
                return null;
            }
        }
        return (CompoundTag) inbt;
    }

    /**
     * Checks whether another meteorite has spawned within the given block range of
     * the given position.
     */
    public boolean isMeteoriteSpawnInRange(BlockPos pos, int rangeSquared) {
        synchronized (data) {
            ChunkPos chunkPos = new ChunkPos(pos);
            CompoundTag spawnData = getSpawnData(chunkPos.x, chunkPos.z, false);
            if (spawnData == null) {
                return false;
            }
            final int size = spawnData.getInt("num");
            for (int i = 0; i < size; i++) {
                CompoundTag existingSettingsNbt = spawnData.getCompound(String.valueOf(i));
                BlockPos existingPos = PlacedMeteoriteSettings.read(existingSettingsNbt).getPos();
                int deltaX = (existingPos.getX() - pos.getX());
                int deltaZ = (existingPos.getZ() - pos.getZ());
                if (deltaX * deltaX + deltaZ * deltaZ <= rangeSquared) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean tryAddSpawnedMeteorite(PlacedMeteoriteSettings settings, int minDistanceSquared) {
        synchronized (data) {
            if (isMeteoriteSpawnInRange(settings.getPos(), minDistanceSquared)) {
                return false;
            }

            addSpawnedMeteorite(settings);
        }
        return true;
    }

    public void addSpawnedMeteorite(PlacedMeteoriteSettings settings) {
        synchronized (data) {
            ChunkPos chunkPos = new ChunkPos(settings.getPos());
            CompoundTag spawnData = getSpawnData(chunkPos.x, chunkPos.z, true);
            final int size = spawnData.getInt("num");
            spawnData.put(String.valueOf(size), settings.write(new CompoundTag()));
            spawnData.putInt("num", size + 1);
            data.markDirty();
        }
    }

    public Collection<PlacedMeteoriteSettings> getNearByMeteorites(int chunkX, int chunkZ) {
        final Collection<PlacedMeteoriteSettings> ll = new ArrayList<>();

        synchronized (data) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    final int cx = x + (chunkX >> 4);
                    final int cz = z + (chunkZ >> 4);

                    final CompoundTag data = getSpawnData(cx << 4, cz << 4, false);

                    if (data != null) {
                        // edit.
                        final int size = data.getInt("num");
                        for (int s = 0; s < size; s++) {
                            CompoundTag settingsNbt = data.getCompound(String.valueOf(s));
                            ll.add(PlacedMeteoriteSettings.read(settingsNbt));
                        }
                    }
                }
            }
        }

        return ll;
    }

    public synchronized static MeteoriteSpawnData get(WorldAccess world) {
        ServerWorld serverWorld;
        if (world instanceof ServerWorld) {
            serverWorld = (ServerWorld) world;
        } else {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            serverWorld = server.getWorld(world.getDimension().getType());
        }

        SavedData savedData = serverWorld.getPersistentStateManager().getOrCreate(SavedData::new, NAME);
        return new MeteoriteSpawnData(savedData);
    }

    private static class SavedData extends PersistentState {

        private LongOpenHashSet generated;
        private CompoundTag spawns;

        public SavedData() {
            super(NAME);
            this.generated = new LongOpenHashSet();
            this.spawns = new CompoundTag();
        }

        @Override
        public synchronized void fromTag(CompoundTag nbt) {
            this.generated = new LongOpenHashSet(nbt.getLongArray("generated"));
            this.spawns = nbt.getCompound("spawns");
        }

        @Override
        public synchronized CompoundTag toTag(CompoundTag compound) {
            compound.putLongArray("generated", generated.toLongArray());
            compound.put("spawns", spawns);
            return compound;
        }
    }

}
