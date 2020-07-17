package appeng.mixins;

import appeng.hooks.DynamicDimensions;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.RegistryTracker;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.UnmodifiableLevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.concurrent.Executor;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements DynamicDimensions {

    @Shadow
    private RegistryTracker.Modifiable dimensionTracker;

    @Shadow
    private Map<RegistryKey<World>, ServerWorld> worlds;

    @Shadow
    private SaveProperties saveProperties;

    @Shadow
    private Executor workerExecutor;

    @Shadow
    private LevelStorage.Session session;

    public ServerWorld addWorld(RegistryKey<World> worldId, RegistryKey<DimensionType> dimensionTypeId, ChunkGenerator chunkGenerator) {
        Preconditions.checkArgument(!worlds.containsKey(worldId), "World with id %s already exists.", worldId.getValue());

        // Each worlds must have a corresponding dimension options, otherwise they will not be re-created on load
        GeneratorOptions generatorOptions = this.saveProperties.getGeneratorOptions();
        Preconditions.checkArgument(!generatorOptions.getDimensionMap().containsId(worldId.getValue()),
                "Dimension config with id %s already exists.", worldId.getValue());

        DimensionType dimensionType = dimensionTracker.getDimensionTypeRegistry().get(dimensionTypeId);
        Preconditions.checkArgument(dimensionType != null, "dimensionTypeId %s is not registered", dimensionTypeId);

        // Create dimension options with the given world-id and chunk generator
        DimensionOptions dimensionOptions = new DimensionOptions(
                () -> dimensionTracker.getDimensionTypeRegistry().get(dimensionTypeId),
                chunkGenerator
        );

        ServerWorldProperties serverWorldProperties = this.saveProperties.getMainWorldProperties();

        // This sucks a little, but during startup, the server will use the same listener for every world,
        // but it is ultimately only stored in the chunk storage. So we retrieve it from the overworld to
        // mirror the original code as much as possible
        ServerWorld overworld = worlds.get(ServerWorld.OVERWORLD);
        Preconditions.checkState(overworld != null, "Overworld does not exist!");
        WorldGenerationProgressListener worldGenerationProgressListener = ((ThreadedAnvilChunkStorageAccessor) overworld.getChunkManager().threadedAnvilChunkStorage).getWorldGenerationProgressListener();

        // Inherit seed + WorldBorder from overworld
        long seed = overworld.getSeed();
        WorldBorder worldBorder = overworld.getWorldBorder();

        MinecraftServer self = (MinecraftServer) (Object) this;
        UnmodifiableLevelProperties unmodifiableLevelProperties = new UnmodifiableLevelProperties(this.saveProperties, serverWorldProperties);
        ServerWorld world = new ServerWorld(
                self, this.workerExecutor, this.session, unmodifiableLevelProperties, worldId, dimensionTypeId, dimensionType, worldGenerationProgressListener, chunkGenerator, false, seed, ImmutableList.of(), false
        );
        worldBorder.addListener(new WorldBorderListener.WorldBorderSyncer(world.getWorldBorder()));
        this.worlds.put(worldId, world);

        // Adding it here will cause the world to be re-created on server restart
        RegistryKey<DimensionOptions> dimOptKey = RegistryKey.of(Registry.DIMENSION_OPTIONS, worldId.getValue());
        generatorOptions.getDimensionMap().add(dimOptKey, dimensionOptions);
        generatorOptions.getDimensionMap().markLoaded(dimOptKey); // Otherwise it wont be saved
        // Ensure the save properties are saved, or the world will potentially be lost on restart
        this.session.method_27426(this.dimensionTracker, this.saveProperties, self.getPlayerManager().getUserData());

        return world;
    }

}
