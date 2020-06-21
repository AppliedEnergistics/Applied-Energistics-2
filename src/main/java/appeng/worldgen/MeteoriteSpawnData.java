package appeng.worldgen;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

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
    private CompoundNBT getSpawnData(int chunkX, int chunkZ, boolean create) {
        chunkX /= 16;
        chunkZ /= 16;
        String key = chunkX + "," + chunkZ;
        INBT inbt = data.spawns.get(key);
        if (!(inbt instanceof CompoundNBT)) {
            if (create) {
                inbt = new CompoundNBT();
                data.spawns.put(key, inbt);
                data.markDirty();
            } else {
                return null;
            }
        }
        return (CompoundNBT) inbt;
    }

    /**
     * Checks whether another meteorite has spawned within the given block range of
     * the given position.
     */
    public boolean isMeteoriteSpawnInRange(BlockPos pos, int rangeSquared) {
        synchronized (data) {
            ChunkPos chunkPos = new ChunkPos(pos);
            CompoundNBT spawnData = getSpawnData(chunkPos.x, chunkPos.z, false);
            if (spawnData == null) {
                return false;
            }
            final int size = spawnData.getInt("num");
            for (int i = 0; i < size; i++) {
                CompoundNBT existingSettingsNbt = spawnData.getCompound(String.valueOf(i));
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
            CompoundNBT spawnData = getSpawnData(chunkPos.x, chunkPos.z, true);
            final int size = spawnData.getInt("num");
            spawnData.put(String.valueOf(size), settings.write(new CompoundNBT()));
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

                    final CompoundNBT data = getSpawnData(cx << 4, cz << 4, false);

                    if (data != null) {
                        // edit.
                        final int size = data.getInt("num");
                        for (int s = 0; s < size; s++) {
                            CompoundNBT settingsNbt = data.getCompound(String.valueOf(s));
                            ll.add(PlacedMeteoriteSettings.read(settingsNbt));
                        }
                    }
                }
            }
        }

        return ll;
    }

    public synchronized static MeteoriteSpawnData get(IWorld world) {
        ServerWorld serverWorld;
        if (world instanceof ServerWorld) {
            serverWorld = (ServerWorld) world;
        } else {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            serverWorld = server.getWorld(world.getDimension().getType());
        }

        SavedData savedData = serverWorld.getSavedData().getOrCreate(SavedData::new, NAME);
        return new MeteoriteSpawnData(savedData);
    }

    private static class SavedData extends WorldSavedData {

        private LongOpenHashSet generated;
        private CompoundNBT spawns;

        public SavedData() {
            super(NAME);
            this.generated = new LongOpenHashSet();
            this.spawns = new CompoundNBT();
        }

        @Override
        public synchronized void read(CompoundNBT nbt) {
            this.generated = new LongOpenHashSet(nbt.getLongArray("generated"));
            this.spawns = nbt.getCompound("spawns");
        }

        @Override
        public synchronized CompoundNBT write(CompoundNBT compound) {
            compound.putLongArray("generated", generated.toLongArray());
            compound.put("spawns", spawns);
            return compound;
        }
    }

}
