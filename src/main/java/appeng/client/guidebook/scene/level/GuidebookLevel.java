package appeng.client.guidebook.scene.level;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import appeng.core.AppEng;
import appeng.util.Platform;

public class GuidebookLevel extends Level {

    private static final ResourceKey<Level> LEVEL_ID = ResourceKey.create(Registries.DIMENSION,
            AppEng.makeId("guidebook"));

    private final TransientEntitySectionManager<Entity> entityStorage = new TransientEntitySectionManager<>(
            Entity.class, new EntityCallbacks());

    private final ChunkSource chunkSource = new GuidebookChunkSource(this);
    private final Holder<Biome> biome;
    private final RegistryAccess registryAccess;
    private final LongSet filledBlocks = new LongOpenHashSet();

    public GuidebookLevel() {
        this(getRegistryAccess());
    }

    private static RegistryAccess getRegistryAccess() {
        if (Minecraft.getInstance().level != null) {
            return Minecraft.getInstance().level.registryAccess();
        }
        return Objects.requireNonNull(Platform.fallbackClientRegistryAccess);
    }

    public GuidebookLevel(RegistryAccess registryAccess) {
        super(
                createLevelData(),
                LEVEL_ID,
                registryAccess,
                registryAccess.registryOrThrow(Registries.DIMENSION_TYPE)
                        .getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD),
                () -> InactiveProfiler.INSTANCE,
                true /* client-side */,
                false /* debug */,
                0 /* seed */,
                1000000 /* max neighbor updates */
        );
        this.registryAccess = registryAccess;
        this.biome = registryAccess.registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);
    }

    public Bounds getBounds() {
        if (filledBlocks.isEmpty()) {
            return new Bounds(BlockPos.ZERO, BlockPos.ZERO);
        }

        var min = new BlockPos.MutableBlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        var max = new BlockPos.MutableBlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        var cur = new BlockPos.MutableBlockPos();

        filledBlocks.forEach(packedPos -> {
            cur.set(packedPos);
            min.setX(Math.min(min.getX(), cur.getX()));
            min.setY(Math.min(min.getY(), cur.getY()));
            min.setZ(Math.min(min.getZ(), cur.getZ()));

            max.setX(Math.max(max.getX(), cur.getX() + 1));
            max.setY(Math.max(max.getY(), cur.getY() + 1));
            max.setZ(Math.max(max.getZ(), cur.getZ() + 1));
        });

        return new Bounds(min, max);
    }

    public boolean isFilledBlock(BlockPos blockPos) {
        return filledBlocks.contains(blockPos.asLong());
    }

    void removeFilledBlock(BlockPos pos) {
        filledBlocks.remove(pos.asLong());
    }

    void addFilledBlock(BlockPos pos) {
        filledBlocks.add(pos.asLong());
    }

    public record Bounds(BlockPos min, BlockPos max) {
    }

    private static ClientLevel.ClientLevelData createLevelData() {
        var levelData = new ClientLevel.ClientLevelData(Difficulty.PEACEFUL, false /* hardcore */, false /* flat */);

        // set time of day to noon (from TimeCommand noon)
        levelData.setDayTime(6000);

        return levelData;
    }

    public Stream<BlockPos> getFilledBlocks() {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        return filledBlocks.longStream()
                .sequential()
                .mapToObj(pos -> {
                    mutablePos.set(pos);
                    return mutablePos;
                });
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return entityStorage.getEntityGetter();
    }

    @Nullable
    @Override
    public Entity getEntity(int id) {
        return getEntities().get(id);
    }

    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
    }

    @Override
    public void playSeededSound(@Nullable Player player, double d, double e, double f, Holder<SoundEvent> holder,
            SoundSource soundSource, float g, float h, long l) {
    }

    @Override
    public void playSeededSound(@Nullable Player player, Entity entity, Holder<SoundEvent> holder,
            SoundSource soundSource, float f, float g, long l) {
    }

    @Override
    public String gatherChunkSourceStats() {
        return "";
    }

    @Nullable
    @Override
    public MapItemSavedData getMapData(String mapName) {
        return null;
    }

    @Override
    public void setMapData(String mapId, MapItemSavedData data) {
    }

    @Override
    public int getFreeMapId() {
        return 0;
    }

    @Override
    public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {
    }

    @Override
    public Scoreboard getScoreboard() {
        return new Scoreboard();
    }

    @Override
    public RecipeManager getRecipeManager() {
        return Platform.getClientRecipeManager();
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public ChunkSource getChunkSource() {
        return chunkSource;
    }

    @Override
    public void levelEvent(@Nullable Player player, int type, BlockPos pos, int data) {
    }

    @Override
    public void gameEvent(GameEvent gameEvent, Vec3 vec3, GameEvent.Context context) {
    }

    @Override
    public float getShade(Direction direction, boolean shade) {
        if (!shade) {
            return 1.0F;
        } else {
            return switch (direction) {
                case DOWN -> 0.5F;
                case NORTH, SOUTH -> 0.8F;
                case WEST, EAST -> 0.6F;
                default -> 1.0F;
            };
        }
    }

    @Override
    public List<? extends Player> players() {
        return List.of();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int i, int j, int k) {
        return biome;
    }

    @Override
    public RegistryAccess registryAccess() {
        return registryAccess;
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return FeatureFlagSet.of();
    }

    private static class EntityCallbacks implements LevelCallback<Entity> {
        @Override
        public void onCreated(Entity entity) {
        }

        @Override
        public void onDestroyed(Entity entity) {
        }

        @Override
        public void onTickingStart(Entity entity) {
        }

        @Override
        public void onTickingEnd(Entity entity) {
        }

        @Override
        public void onTrackingStart(Entity entity) {
        }

        @Override
        public void onTrackingEnd(Entity entity) {
        }

        @Override
        public void onSectionChange(Entity object) {
        }
    }
}
