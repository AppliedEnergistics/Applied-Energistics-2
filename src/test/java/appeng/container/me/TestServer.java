package appeng.container.me;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.block.Blocks;
import net.minecraft.command.Commands;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.resources.ServerPackFinder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.codec.DatapackCodec;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.listener.LoggingChunkStatusListener;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.PlayerData;
import net.minecraft.world.storage.SaveFormat;
import net.minecraft.world.storage.ServerWorldInfo;
import org.assertj.core.util.Lists;

import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.mockito.Mockito.mock;

class TestServer extends MinecraftServer {
    public static final DataFixer NOOP_DATA_FIXER = new DataFixerBuilder(0).build(MoreExecutors.directExecutor());

    private static final DynamicRegistries.Impl dynamicRegistries = DynamicRegistries.func_239770_b_();

    private static MinecraftSessionService createFakeSessionService() {
        return mock(MinecraftSessionService.class);
    }

    private static SaveFormat.LevelSave createLevelSave(Path tempDir) throws IOException {
        SaveFormat saveFormat = SaveFormat.create(tempDir.resolve("worldsave"));
        return saveFormat.getLevelSave("unittest");
    }

    public static TestServer create(Path tempDir) throws Exception {
        ResourcePackList resourcepacklist = new ResourcePackList(new ServerPackFinder());
        DatapackCodec datapackCodec = MinecraftServer.func_240772_a_(resourcepacklist, DatapackCodec.VANILLA_CODEC, false);
        DataPackRegistries dataPackRegistries = DataPackRegistries.func_240961_a_(
                resourcepacklist.func_232623_f_(),
                Commands.EnvironmentType.DEDICATED,
                0,
                Runnable::run,
                Runnable::run
        ).get();
        dataPackRegistries.updateTags();

        WorldSettings worldSettings = new WorldSettings(
                "unittest",
                GameType.SURVIVAL,
                false,
                Difficulty.NORMAL,
                false,
                new GameRules(),
                datapackCodec
        );

        Registry<Biome> biomeRegistry = dynamicRegistries.getRegistry(Registry.BIOME_KEY);
        FlatGenerationSettings flatGenerationSettings = new FlatGenerationSettings(
                biomeRegistry,
                new DimensionStructuresSettings(Optional.empty(), Collections.emptyMap()),
                Lists.newArrayList(
                        new FlatLayerInfo(1, Blocks.VOID_AIR)
                ),
                false,
                false,
                Optional.of(() -> biomeRegistry.getOrThrow(Biomes.THE_VOID))
        );
        ChunkGenerator chunkGenerator = new FlatChunkGenerator(flatGenerationSettings);
        SimpleRegistry<Dimension> dimensions = new SimpleRegistry<>(Registry.DIMENSION_KEY, Lifecycle.experimental());
        dimensions.register(Dimension.OVERWORLD, new Dimension(() -> dynamicRegistries.func_230520_a_().getOrThrow(DimensionType.OVERWORLD), chunkGenerator), Lifecycle.stable());

        IServerConfiguration config = new ServerWorldInfo(
                worldSettings,
                new DimensionGeneratorSettings("appeng2".hashCode(), false, false, dimensions),
                Lifecycle.stable()
        );

        return new TestServer(
                tempDir,
                resourcepacklist,
                dataPackRegistries,
                config
        );
    }

    public void tick() {
        super.tick(() -> true);
    }

    private TestServer(Path tempDir, ResourcePackList dataPacks, DataPackRegistries dataPackRegistries, IServerConfiguration config) throws IOException {
        super(Thread.currentThread(),
                dynamicRegistries,
                createLevelSave(tempDir),
                config,
                dataPacks,
                Proxy.NO_PROXY,
                NOOP_DATA_FIXER,
                dataPackRegistries,
                createFakeSessionService(),
                mock(GameProfileRepository.class),
                mock(PlayerProfileCache.class),
                LoggingChunkStatusListener::new);

        setPlayerList(new PlayerList(this, dynamicRegistries, new PlayerData(this.anvilConverterForAnvilFile, NOOP_DATA_FIXER), 1) {
        });

        init();
    }

    /**
     * Copied from IntegratedServer
     */
    @Override
    protected boolean init() {
        this.setOnlineMode(true);
        this.setAllowPvp(true);
        this.setAllowFlight(true);
        this.func_244801_P();
        if (!net.minecraftforge.fml.server.ServerLifecycleHooks.handleServerAboutToStart(this)) return false;
        this.func_240800_l__();
        this.setMOTD(this.getServerOwner() + " - " + this.getServerConfiguration().getWorldName());
        return net.minecraftforge.fml.server.ServerLifecycleHooks.handleServerStarting(this);
    }

    @Override
    public int getOpPermissionLevel() {
        return 0;
    }

    @Override
    public int getFunctionLevel() {
        return 0;
    }

    @Override
    public boolean allowLoggingRcon() {
        return false;
    }

    @Override
    public Optional<String> func_230045_q_() {
        return Optional.empty();
    }

    @Override
    public boolean isDedicatedServer() {
        return false;
    }

    @Override
    public int func_241871_k() {
        return 0;
    }

    @Override
    public boolean shouldUseNativeTransport() {
        return false;
    }

    @Override
    public boolean isCommandBlockEnabled() {
        return false;
    }

    @Override
    public boolean getPublic() {
        return false;
    }

    @Override
    public boolean shareToLAN(GameType gameMode, boolean cheats, int port) {
        return false;
    }

    @Override
    public boolean isServerOwner(GameProfile profileIn) {
        return false;
    }

    @Override
    public boolean allowLogging() {
        return false;
    }
}
