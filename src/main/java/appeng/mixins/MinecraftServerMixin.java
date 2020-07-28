package appeng.mixins;

import java.util.Map;
import java.util.concurrent.Executor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.server.IDynamicRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.SaveFormat;

import appeng.hooks.DynamicDimensions;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements DynamicDimensions {

    @Shadow()
    private IDynamicRegistries.Impl field_240767_f_;

    @Shadow
    private Map<RegistryKey<World>, ServerWorld> worlds;

    @Shadow
    private IServerConfiguration field_240768_i_;

    @Shadow
    private Executor backgroundExecutor;

    @Shadow
    private SaveFormat.LevelSave anvilConverterForAnvilFile;

    public ServerWorld addWorld(RegistryKey<World> worldId, RegistryKey<DimensionType> dimensionTypeId,
                                ChunkGenerator chunkGenerator) {
        Preconditions.checkArgument(!worlds.containsKey(worldId), "World with id %s already exists.",
                worldId.func_240901_a_());

        // Each worlds must have a corresponding dimension options, otherwise they will
        // not be re-created on load
        DimensionGeneratorSettings generatorOptions = this.field_240768_i_.func_230418_z_();
        Preconditions.checkArgument(!generatorOptions.func_236224_e_().containsKey(worldId.func_240901_a_()),
                "Dimension config with id %s already exists.", worldId.func_240901_a_());

        DimensionType dimensionType = field_240767_f_.func_230520_a_().func_230516_a_(dimensionTypeId);
        Preconditions.checkArgument(dimensionType != null, "dimensionTypeId %s is not registered", dimensionTypeId);

        // Create dimension options with the given world-id and chunk generator
        Dimension dimensionOptions = new Dimension(
                () -> field_240767_f_.func_230520_a_().func_230516_a_(dimensionTypeId), chunkGenerator);

        IServerWorldInfo serverWorldProperties = this.field_240768_i_.func_230407_G_();

        // This sucks a little, but during startup, the server will use the same
        // listener for every world,
        // but it is ultimately only stored in the chunk storage. So we retrieve it from
        // the overworld to
        // mirror the original code as much as possible
        ServerWorld overworld = worlds.get(ServerWorld.field_234918_g_);
        Preconditions.checkState(overworld != null, "Overworld does not exist!");
        IChunkStatusListener worldGenerationProgressListener = ((ThreadedAnvilChunkStorageAccessor) overworld
                .getChunkProvider().chunkManager).getWorldGenerationProgressListener();

        // Inherit seed + WorldBorder from overworld
        long seed = overworld.getSeed();
        WorldBorder worldBorder = overworld.getWorldBorder();

        MinecraftServer self = (MinecraftServer) (Object) this;
        DerivedWorldInfo unmodifiableLevelProperties = new DerivedWorldInfo(this.field_240768_i_,
                serverWorldProperties);
        ServerWorld world = new ServerWorld(self, this.backgroundExecutor, this.anvilConverterForAnvilFile, unmodifiableLevelProperties,
                worldId, dimensionTypeId, dimensionType, worldGenerationProgressListener, chunkGenerator, false, seed,
                ImmutableList.of(), false);
        worldBorder.addListener(new IBorderListener.Impl(world.getWorldBorder()));
        this.worlds.put(worldId, world);

        // Adding it here will cause the world to be re-created on server restart
        RegistryKey<Dimension> dimOptKey = RegistryKey.func_240903_a_(Registry.DIMENSION_KEY, worldId.func_240901_a_());
        generatorOptions.func_236224_e_().register(dimOptKey, dimensionOptions);
        generatorOptions.func_236224_e_().func_239662_d_(dimOptKey); // Otherwise it wont be saved
        // Ensure the save properties are saved, or the world will potentially be lost
        // on restart
        this.anvilConverterForAnvilFile.func_237288_a_(this.field_240767_f_, this.field_240768_i_, self.getPlayerList().getHostPlayerData());

        return world;
    }

}
