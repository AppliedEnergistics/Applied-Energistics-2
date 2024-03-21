package appeng.client.guidebook.scene.element;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.TickPriority;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;

/**
 * Makes it possible to use a {@link LevelAccessor} where code requires a {@link ServerLevelAccessor}, when that code
 * doesn't actually use the {@link ServerLevelAccessor#getLevel()} method.
 * <p/>
 * When porting this class, just use IntelliJ's "Code -> Delegate To" code generation to generate new methods in
 * LevelAccessor and make them delegate to {@link #delegate}. If {@link ServerLevelAccessor} requires new methods, make
 * them throw {@link UnsupportedOperationException}. As long as
 * {@link net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate#placeInWorld} only makes use of
 * the server level in specific circumstances we don't use, this should continue to work.
 */
class FakeForwardingServerLevel implements ServerLevelAccessor {
    private final LevelAccessor delegate;

    public FakeForwardingServerLevel(LevelAccessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public ServerLevel getLevel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long dayTime() {
        return delegate.dayTime();
    }

    @Override
    public long nextSubTickCount() {
        return delegate.nextSubTickCount();
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return delegate.getBlockTicks();
    }

    @Override
    public void scheduleTick(BlockPos pos, Block block, int delay, TickPriority priority) {
        delegate.scheduleTick(pos, block, delay, priority);
    }

    @Override
    public void scheduleTick(BlockPos pos, Block block, int delay) {
        delegate.scheduleTick(pos, block, delay);
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return delegate.getFluidTicks();
    }

    @Override
    public void scheduleTick(BlockPos pos, Fluid fluid, int delay, TickPriority priority) {
        delegate.scheduleTick(pos, fluid, delay, priority);
    }

    @Override
    public void scheduleTick(BlockPos pos, Fluid fluid, int delay) {
        delegate.scheduleTick(pos, fluid, delay);
    }

    @Override
    public LevelData getLevelData() {
        return delegate.getLevelData();
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos pos) {
        return delegate.getCurrentDifficultyAt(pos);
    }

    @Override
    @Nullable
    public MinecraftServer getServer() {
        return delegate.getServer();
    }

    @Override
    public Difficulty getDifficulty() {
        return delegate.getDifficulty();
    }

    @Override
    public ChunkSource getChunkSource() {
        return delegate.getChunkSource();
    }

    @Override
    public boolean hasChunk(int chunkX, int chunkZ) {
        return delegate.hasChunk(chunkX, chunkZ);
    }

    @Override
    public RandomSource getRandom() {
        return delegate.getRandom();
    }

    @Override
    public void blockUpdated(BlockPos pos, Block block) {
        delegate.blockUpdated(pos, block);
    }

    @Override
    public void neighborShapeChanged(Direction direction, BlockState queried, BlockPos pos, BlockPos offsetPos,
            int flags, int recursionLevel) {
        delegate.neighborShapeChanged(direction, queried, pos, offsetPos, flags, recursionLevel);
    }

    @Override
    public void playSound(@Nullable Player player, BlockPos pos, SoundEvent sound, SoundSource source) {
        delegate.playSound(player, pos, sound, source);
    }

    @Override
    public void playSound(@Nullable Player player, BlockPos pos, SoundEvent sound, SoundSource source, float volume,
            float pitch) {
        delegate.playSound(player, pos, sound, source, volume, pitch);
    }

    @Override
    public void addParticle(ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed,
            double zSpeed) {
        delegate.addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public void levelEvent(@Nullable Player player, int type, BlockPos pos, int data) {
        delegate.levelEvent(player, type, pos, data);
    }

    @Override
    public void levelEvent(int type, BlockPos pos, int data) {
        delegate.levelEvent(type, pos, data);
    }

    @Override
    public void gameEvent(Holder<GameEvent> event, Vec3 position, GameEvent.Context context) {
        delegate.gameEvent(event, position, context);
    }

    @Override
    public void gameEvent(@Nullable Entity entity, Holder<GameEvent> event, Vec3 position) {
        delegate.gameEvent(entity, event, position);
    }

    @Override
    public void gameEvent(@Nullable Entity entity, Holder<GameEvent> event, BlockPos pos) {
        delegate.gameEvent(entity, event, pos);
    }

    @Override
    public void gameEvent(Holder<GameEvent> event, BlockPos pos, GameEvent.Context context) {
        delegate.gameEvent(event, pos, context);
    }

    @Override
    public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos pos, BlockEntityType<T> type) {
        return delegate.getBlockEntity(pos, type);
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB collisionBox) {
        return delegate.getEntityCollisions(entity, collisionBox);
    }

    @Override
    public boolean isUnobstructed(@Nullable Entity entity, VoxelShape shape) {
        return delegate.isUnobstructed(entity, shape);
    }

    @Override
    public BlockPos getHeightmapPos(Heightmap.Types heightmapType, BlockPos pos) {
        return delegate.getHeightmapPos(heightmapType, pos);
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity entity, AABB area, Predicate<? super Entity> predicate) {
        return delegate.getEntities(entity, area, predicate);
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> entityTypeTest, AABB bounds,
            Predicate<? super T> predicate) {
        return delegate.getEntities(entityTypeTest, bounds, predicate);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesOfClass(Class<T> clazz, AABB area, Predicate<? super T> filter) {
        return delegate.getEntitiesOfClass(clazz, area, filter);
    }

    @Override
    public List<? extends Player> players() {
        return delegate.players();
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity entity, AABB area) {
        return delegate.getEntities(entity, area);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesOfClass(Class<T> entityClass, AABB area) {
        return delegate.getEntitiesOfClass(entityClass, area);
    }

    @Override
    @Nullable
    public Player getNearestPlayer(double x, double y, double z, double distance,
            @Nullable Predicate<Entity> predicate) {
        return delegate.getNearestPlayer(x, y, z, distance, predicate);
    }

    @Override
    @Nullable
    public Player getNearestPlayer(Entity entity, double distance) {
        return delegate.getNearestPlayer(entity, distance);
    }

    @Override
    @Nullable
    public Player getNearestPlayer(double x, double y, double z, double distance, boolean creativePlayers) {
        return delegate.getNearestPlayer(x, y, z, distance, creativePlayers);
    }

    @Override
    public boolean hasNearbyAlivePlayer(double x, double y, double z, double distance) {
        return delegate.hasNearbyAlivePlayer(x, y, z, distance);
    }

    @Override
    @Nullable
    public Player getNearestPlayer(TargetingConditions predicate, LivingEntity target) {
        return delegate.getNearestPlayer(predicate, target);
    }

    @Override
    @Nullable
    public Player getNearestPlayer(TargetingConditions predicate, LivingEntity target, double x, double y, double z) {
        return delegate.getNearestPlayer(predicate, target, x, y, z);
    }

    @Override
    @Nullable
    public Player getNearestPlayer(TargetingConditions predicate, double x, double y, double z) {
        return delegate.getNearestPlayer(predicate, x, y, z);
    }

    @Override
    @Nullable
    public <T extends LivingEntity> T getNearestEntity(Class<? extends T> entityClazz, TargetingConditions conditions,
            @Nullable LivingEntity target, double x, double y, double z, AABB boundingBox) {
        return delegate.getNearestEntity(entityClazz, conditions, target, x, y, z, boundingBox);
    }

    @Override
    @Nullable
    public <T extends LivingEntity> T getNearestEntity(List<? extends T> entities, TargetingConditions predicate,
            @Nullable LivingEntity target, double x, double y, double z) {
        return delegate.getNearestEntity(entities, predicate, target, x, y, z);
    }

    @Override
    public List<Player> getNearbyPlayers(TargetingConditions predicate, LivingEntity target, AABB area) {
        return delegate.getNearbyPlayers(predicate, target, area);
    }

    @Override
    public <T extends LivingEntity> List<T> getNearbyEntities(Class<T> entityClazz, TargetingConditions entityPredicate,
            LivingEntity entity, AABB area) {
        return delegate.getNearbyEntities(entityClazz, entityPredicate, entity, area);
    }

    @Override
    @Nullable
    public Player getPlayerByUUID(UUID uniqueId) {
        return delegate.getPlayerByUUID(uniqueId);
    }

    @Override
    @Nullable
    public ChunkAccess getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
        return delegate.getChunk(x, z, requiredStatus, nonnull);
    }

    @Override
    public int getHeight(Heightmap.Types heightmapType, int x, int z) {
        return delegate.getHeight(heightmapType, x, z);
    }

    @Override
    public int getSkyDarken() {
        return delegate.getSkyDarken();
    }

    @Override
    public BiomeManager getBiomeManager() {
        return delegate.getBiomeManager();
    }

    @Override
    public Holder<Biome> getBiome(BlockPos pos) {
        return delegate.getBiome(pos);
    }

    @Override
    public Stream<BlockState> getBlockStatesIfLoaded(AABB aabb) {
        return delegate.getBlockStatesIfLoaded(aabb);
    }

    @Override
    public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
        return delegate.getBlockTint(blockPos, colorResolver);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int i, int j, int k) {
        return delegate.getNoiseBiome(i, j, k);
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        return delegate.getUncachedNoiseBiome(x, y, z);
    }

    @Override
    public boolean isClientSide() {
        return delegate.isClientSide();
    }

    @Override
    @Deprecated
    public int getSeaLevel() {
        return delegate.getSeaLevel();
    }

    @Override
    public DimensionType dimensionType() {
        return delegate.dimensionType();
    }

    @Override
    public int getMinBuildHeight() {
        return delegate.getMinBuildHeight();
    }

    @Override
    public int getHeight() {
        return delegate.getHeight();
    }

    @Override
    public boolean isEmptyBlock(BlockPos pos) {
        return delegate.isEmptyBlock(pos);
    }

    @Override
    public boolean canSeeSkyFromBelowWater(BlockPos pos) {
        return delegate.canSeeSkyFromBelowWater(pos);
    }

    @Override
    public float getPathfindingCostFromLightLevels(BlockPos blockPos) {
        return delegate.getPathfindingCostFromLightLevels(blockPos);
    }

    @Override
    @Deprecated
    public float getLightLevelDependentMagicValue(BlockPos blockPos) {
        return delegate.getLightLevelDependentMagicValue(blockPos);
    }

    @Override
    public int getDirectSignal(BlockPos pos, Direction direction) {
        return delegate.getDirectSignal(pos, direction);
    }

    @Override
    public ChunkAccess getChunk(BlockPos pos) {
        return delegate.getChunk(pos);
    }

    @Override
    public ChunkAccess getChunk(int chunkX, int chunkZ) {
        return delegate.getChunk(chunkX, chunkZ);
    }

    @Override
    public ChunkAccess getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus) {
        return delegate.getChunk(chunkX, chunkZ, requiredStatus);
    }

    @Override
    @Nullable
    public BlockGetter getChunkForCollisions(int chunkX, int chunkZ) {
        return delegate.getChunkForCollisions(chunkX, chunkZ);
    }

    @Override
    public boolean isWaterAt(BlockPos pos) {
        return delegate.isWaterAt(pos);
    }

    @Override
    public boolean containsAnyLiquid(AABB bb) {
        return delegate.containsAnyLiquid(bb);
    }

    @Override
    public int getMaxLocalRawBrightness(BlockPos pos) {
        return delegate.getMaxLocalRawBrightness(pos);
    }

    @Override
    public int getMaxLocalRawBrightness(BlockPos pos, int amount) {
        return delegate.getMaxLocalRawBrightness(pos, amount);
    }

    @Override
    @Deprecated
    public boolean hasChunkAt(int x, int z) {
        return delegate.hasChunkAt(x, z);
    }

    @Override
    @Deprecated
    public boolean hasChunkAt(BlockPos pos) {
        return delegate.hasChunkAt(pos);
    }

    @Override
    @Deprecated
    public boolean hasChunksAt(BlockPos from, BlockPos to) {
        return delegate.hasChunksAt(from, to);
    }

    @Override
    @Deprecated
    public boolean hasChunksAt(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
        return delegate.hasChunksAt(fromX, fromY, fromZ, toX, toY, toZ);
    }

    @Override
    @Deprecated
    public boolean hasChunksAt(int fromX, int fromZ, int toX, int toZ) {
        return delegate.hasChunksAt(fromX, fromZ, toX, toZ);
    }

    @Override
    public RegistryAccess registryAccess() {
        return delegate.registryAccess();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return delegate.enabledFeatures();
    }

    @Override
    public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
        return delegate.holderLookup(resourceKey);
    }

    @Override
    public float getShade(Direction direction, boolean shade) {
        return delegate.getShade(direction, shade);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return delegate.getLightEngine();
    }

    @Override
    public int getBrightness(LightLayer lightType, BlockPos blockPos) {
        return delegate.getBrightness(lightType, blockPos);
    }

    @Override
    public int getRawBrightness(BlockPos blockPos, int amount) {
        return delegate.getRawBrightness(blockPos, amount);
    }

    @Override
    public boolean canSeeSky(BlockPos blockPos) {
        return delegate.canSeeSky(blockPos);
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        return delegate.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return delegate.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return delegate.getFluidState(pos);
    }

    @Override
    public int getLightEmission(BlockPos pos) {
        return delegate.getLightEmission(pos);
    }

    @Override
    public int getMaxLightLevel() {
        return delegate.getMaxLightLevel();
    }

    @Override
    public Stream<BlockState> getBlockStates(AABB area) {
        return delegate.getBlockStates(area);
    }

    @Override
    public BlockHitResult isBlockInLine(ClipBlockStateContext context) {
        return delegate.isBlockInLine(context);
    }

    @Override
    public BlockHitResult clip(ClipContext context) {
        return delegate.clip(context);
    }

    @Override
    @Nullable
    public BlockHitResult clipWithInteractionOverride(Vec3 startVec, Vec3 endVec, BlockPos pos, VoxelShape shape,
            BlockState state) {
        return delegate.clipWithInteractionOverride(startVec, endVec, pos, shape, state);
    }

    @Override
    public double getBlockFloorHeight(VoxelShape shape, Supplier<VoxelShape> belowShapeSupplier) {
        return delegate.getBlockFloorHeight(shape, belowShapeSupplier);
    }

    @Override
    public double getBlockFloorHeight(BlockPos pos) {
        return delegate.getBlockFloorHeight(pos);
    }

    public static <T, C> T traverseBlocks(Vec3 from, Vec3 to, C context, BiFunction<C, BlockPos, T> tester,
            Function<C, T> onFail) {
        return BlockGetter.traverseBlocks(from, to, context, tester, onFail);
    }

    @Override
    public int getMaxBuildHeight() {
        return delegate.getMaxBuildHeight();
    }

    @Override
    public int getSectionsCount() {
        return delegate.getSectionsCount();
    }

    @Override
    public int getMinSection() {
        return delegate.getMinSection();
    }

    @Override
    public int getMaxSection() {
        return delegate.getMaxSection();
    }

    @Override
    public boolean isOutsideBuildHeight(BlockPos pos) {
        return delegate.isOutsideBuildHeight(pos);
    }

    @Override
    public boolean isOutsideBuildHeight(int y) {
        return delegate.isOutsideBuildHeight(y);
    }

    @Override
    public int getSectionIndex(int y) {
        return delegate.getSectionIndex(y);
    }

    @Override
    public int getSectionIndexFromSectionY(int sectionIndex) {
        return delegate.getSectionIndexFromSectionY(sectionIndex);
    }

    @Override
    public int getSectionYFromSectionIndex(int sectionIndex) {
        return delegate.getSectionYFromSectionIndex(sectionIndex);
    }

    public static LevelHeightAccessor create(int minBuildHeight, int height) {
        return LevelHeightAccessor.create(minBuildHeight, height);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return delegate.getWorldBorder();
    }

    @Override
    public boolean isUnobstructed(BlockState state, BlockPos pos, CollisionContext context) {
        return delegate.isUnobstructed(state, pos, context);
    }

    @Override
    public boolean isUnobstructed(Entity entity) {
        return delegate.isUnobstructed(entity);
    }

    @Override
    public boolean noCollision(AABB collisionBox) {
        return delegate.noCollision(collisionBox);
    }

    @Override
    public boolean noCollision(Entity entity) {
        return delegate.noCollision(entity);
    }

    @Override
    public boolean noCollision(@Nullable Entity entity, AABB collisionBox) {
        return delegate.noCollision(entity, collisionBox);
    }

    @Override
    public Iterable<VoxelShape> getCollisions(@Nullable Entity entity, AABB collisionBox) {
        return delegate.getCollisions(entity, collisionBox);
    }

    @Override
    public Iterable<VoxelShape> getBlockCollisions(@Nullable Entity entity, AABB collisionBox) {
        return delegate.getBlockCollisions(entity, collisionBox);
    }

    @Override
    public boolean collidesWithSuffocatingBlock(@Nullable Entity entity, AABB box) {
        return delegate.collidesWithSuffocatingBlock(entity, box);
    }

    @Override
    public Optional<Vec3> findFreePosition(@Nullable Entity entity, VoxelShape shape, Vec3 pos, double x, double y,
            double z) {
        return delegate.findFreePosition(entity, shape, pos, x, y, z);
    }

    @Override
    public boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> state) {
        return delegate.isStateAtPosition(pos, state);
    }

    @Override
    public boolean isFluidAtPosition(BlockPos pos, Predicate<FluidState> predicate) {
        return delegate.isFluidAtPosition(pos, predicate);
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft) {
        return delegate.setBlock(pos, state, flags, recursionLeft);
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState newState, int flags) {
        return delegate.setBlock(pos, newState, flags);
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean isMoving) {
        return delegate.removeBlock(pos, isMoving);
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
        return delegate.destroyBlock(pos, dropBlock);
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity) {
        return delegate.destroyBlock(pos, dropBlock, entity);
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionLeft) {
        return delegate.destroyBlock(pos, dropBlock, entity, recursionLeft);
    }

    @Override
    public boolean addFreshEntity(Entity entity) {
        return delegate.addFreshEntity(entity);
    }

    @Override
    public float getMoonBrightness() {
        return delegate.getMoonBrightness();
    }

    @Override
    public float getTimeOfDay(float partialTick) {
        return delegate.getTimeOfDay(partialTick);
    }

    @Override
    public int getMoonPhase() {
        return delegate.getMoonPhase();
    }

    @Override
    public void gameEvent(ResourceKey<GameEvent> p_316780_, BlockPos p_316509_, GameEvent.Context p_316524_) {
        delegate.gameEvent(p_316780_, p_316509_, p_316524_);
    }

    @Override
    public boolean isAreaLoaded(BlockPos center, int range) {
        return delegate.isAreaLoaded(center, range);
    }

    @Override
    @ApiStatus.NonExtendable
    public @Nullable AuxiliaryLightManager getAuxLightManager(BlockPos pos) {
        return delegate.getAuxLightManager(pos);
    }

    @Override
    public @Nullable AuxiliaryLightManager getAuxLightManager(ChunkPos pos) {
        return delegate.getAuxLightManager(pos);
    }

    @Override
    public ModelData getModelData(BlockPos pos) {
        return delegate.getModelData(pos);
    }

    @Override
    public float getShade(float normalX, float normalY, float normalZ, boolean shade) {
        return delegate.getShade(normalX, normalY, normalZ, shade);
    }

    @Override
    public boolean noBlockCollision(@Nullable Entity pEntity, AABB pBoundingBox) {
        return delegate.noBlockCollision(pEntity, pBoundingBox);
    }

    @Override
    public Optional<BlockPos> findSupportingBlock(Entity pEntity, AABB pBox) {
        return delegate.findSupportingBlock(pEntity, pBox);
    }

    @Override
    public int getDirectSignalTo(BlockPos pPos) {
        return delegate.getDirectSignalTo(pPos);
    }

    @Override
    public int getControlInputSignal(BlockPos pPos, Direction pDirection, boolean pDiodesOnly) {
        return delegate.getControlInputSignal(pPos, pDirection, pDiodesOnly);
    }

    @Override
    public boolean hasSignal(BlockPos pPos, Direction pDirection) {
        return delegate.hasSignal(pPos, pDirection);
    }

    @Override
    public int getSignal(BlockPos pPos, Direction pDirection) {
        return delegate.getSignal(pPos, pDirection);
    }

    @Override
    public boolean hasNeighborSignal(BlockPos pPos) {
        return delegate.hasNeighborSignal(pPos);
    }

    @Override
    public int getBestNeighborSignal(BlockPos pPos) {
        return delegate.getBestNeighborSignal(pPos);
    }
}
