/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import javax.annotation.Nullable;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import appeng.api.parts.CableRenderMode;
import appeng.capabilities.Capabilities;
import appeng.core.api.definitions.ApiBlocks;
import appeng.core.api.definitions.ApiItems;
import appeng.core.api.definitions.ApiParts;
import appeng.core.stats.AdvancementTriggers;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.worlddata.WorldData;
import appeng.hooks.ticking.TickHandler;
import appeng.init.InitBlockEntities;
import appeng.init.InitBlocks;
import appeng.init.InitContainerTypes;
import appeng.init.InitDispenserBehavior;
import appeng.init.InitEntityTypes;
import appeng.init.InitItems;
import appeng.init.InitRecipeSerializers;
import appeng.init.InitStats;
import appeng.init.client.InitParticleTypes;
import appeng.init.internal.InitCellHandlers;
import appeng.init.internal.InitChargerRates;
import appeng.init.internal.InitGridCaches;
import appeng.init.internal.InitMatterCannonAmmo;
import appeng.init.internal.InitP2PAttunements;
import appeng.init.internal.InitSpatialMovableRegistry;
import appeng.init.internal.InitUpgrades;
import appeng.init.internal.InitWirelessHandlers;
import appeng.init.worldgen.InitBiomeModifications;
import appeng.init.worldgen.InitBiomes;
import appeng.init.worldgen.InitFeatures;
import appeng.init.worldgen.InitStructures;
import appeng.integration.Integrations;
import appeng.items.tools.NetworkToolItem;
import appeng.parts.PartPlacement;
import appeng.server.AECommand;
import appeng.services.ChunkLoadingService;
import appeng.spatial.SpatialStorageChunkGenerator;
import appeng.spatial.SpatialStorageDimensionIds;

/**
 * Mod functionality that is common to both dedicated server and client.
 * <p>
 * Note that a client will still have zero or more embedded servers (although only one at a time).
 */
public abstract class AppEngBase implements AppEng {

    /**
     * While we process a player-specific part placement/cable interaction packet, we need to use that player's
     * transparent-facade mode to understand whether the player can see through facades or not.
     * <p>
     * We need to use this method since the collision shape methods do not know about the player that the shape is being
     * requested for, so they will call {@link #getCableRenderMode()} below, which then will use this field to figure
     * out which player it's for.
     */
    private final ThreadLocal<PlayerEntity> partInteractionPlayer = new ThreadLocal<>();

    static AppEngBase INSTANCE;

    private AdvancementTriggers advancementTriggers;

    public AppEngBase() {
        if (INSTANCE != null) {
            throw new IllegalStateException();
        }
        INSTANCE = this;

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AEConfig.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AEConfig.COMMON_SPEC);

        CreativeTab.init();

        // Initialize items in order
        ApiItems.init();
        ApiBlocks.init();
        ApiItems.init();
        ApiParts.init();

        new FacadeItemGroup(); // This call has a side-effect (adding it to the creative screen)

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::registerDimension);
        modEventBus.addGenericListener(Biome.class, this::registerBiomes);
        modEventBus.addGenericListener(Block.class, this::registerBlocks);
        modEventBus.addGenericListener(Item.class, this::registerItems);
        modEventBus.addGenericListener(EntityType.class, this::registerEntities);
        modEventBus.addGenericListener(ParticleType.class, this::registerParticleTypes);
        modEventBus.addGenericListener(TileEntityType.class, this::registerTileEntities);
        modEventBus.addGenericListener(ContainerType.class, this::registerContainerTypes);
        modEventBus.addGenericListener(IRecipeSerializer.class, this::registerRecipeSerializers);
        modEventBus.addGenericListener(Structure.class, this::registerStructures);
        modEventBus.addGenericListener(Feature.class, this::registerFeatures);

        modEventBus.addListener(Integrations::enqueueIMC);
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.addListener(TickHandler.instance()::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(TickHandler.instance()::onWorldTick);
        MinecraftForge.EVENT_BUS.addListener(TickHandler.instance()::onUnloadChunk);
        // Try to go first for world loads since we use it to initialize state
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, TickHandler.instance()::onLoadWorld);
        // Try to go last for world unloads since we use it to clean-up state
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, TickHandler.instance()::onUnloadWorld);

        MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopped);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopping);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

        MinecraftForge.EVENT_BUS.register(new PartPlacement());
        MinecraftForge.EVENT_BUS.addListener(InitBiomeModifications::init);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(this::postRegistrationInitialization);
    }

    /**
     * Runs after all mods have had time to run their registrations into registries.
     */
    public void postRegistrationInitialization() {
        InitP2PAttunements.init();

        // Do initialization that doesn't depend on mod registries being populated
        InitStats.init();
        advancementTriggers = new AdvancementTriggers(CriteriaTriggers::register);

        Capabilities.register();
        InitDispenserBehavior.init();
        InitGridCaches.init();
        InitMatterCannonAmmo.init();
        InitCellHandlers.init();

        AEConfig.instance().save();
        InitWirelessHandlers.init();
        InitUpgrades.init();
        InitChargerRates.init();
        InitSpatialMovableRegistry.init();
        NetworkHandler.init(new ResourceLocation(MOD_ID, "main"));

        ChunkLoadingService.register();

        AddonLoader.loadAddons(Api.INSTANCE);
    }

    @Override
    public AdvancementTriggers getAdvancementTriggers() {
        return Objects.requireNonNull(this.advancementTriggers);
    }

    private void startService(final String serviceName, final Thread thread) {
        thread.setName(serviceName);
        thread.setPriority(Thread.MIN_PRIORITY);

        AELog.info("Starting " + serviceName);
        thread.start();
    }

    public void registerBiomes(RegistryEvent.Register<Biome> event) {
        InitBiomes.init(event.getRegistry());
    }

    public void registerBlocks(RegistryEvent.Register<Block> event) {
        InitBlocks.init(event.getRegistry());
    }

    public void registerItems(RegistryEvent.Register<Item> event) {
        InitItems.init(event.getRegistry());
    }

    public void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        InitBlockEntities.init(event.getRegistry());
    }

    public void registerContainerTypes(RegistryEvent.Register<ContainerType<?>> event) {
        InitContainerTypes.init(event.getRegistry());
    }

    public void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        InitRecipeSerializers.init(event.getRegistry());
    }

    public void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        InitEntityTypes.init(event.getRegistry());
    }

    public void registerParticleTypes(RegistryEvent.Register<ParticleType<?>> event) {
        InitParticleTypes.init(event.getRegistry());
    }

    public void registerStructures(RegistryEvent.Register<Structure<?>> event) {
        InitStructures.init(event.getRegistry());
    }

    public void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
        InitFeatures.init(event.getRegistry());
    }

    public void registerCommands(final FMLServerStartingEvent evt) {
        CommandDispatcher<CommandSource> dispatcher = evt.getServer().getCommandManager().getDispatcher();
        new AECommand().register(dispatcher);
    }

    public void registerDimension(RegistryEvent.NewRegistry e) {
        Registry.register(Registry.CHUNK_GENERATOR_CODEC, SpatialStorageDimensionIds.CHUNK_GENERATOR_ID,
                SpatialStorageChunkGenerator.CODEC);
    }

    private void onServerAboutToStart(final FMLServerAboutToStartEvent evt) {
        WorldData.onServerStarting(evt.getServer());
        ChunkLoadingService.getInstance().onServerAboutToStart(evt);
    }

    private void serverStopping(final FMLServerStoppingEvent event) {
        WorldData.instance().onServerStopping();
        ChunkLoadingService.getInstance().onServerStopping(event);
    }

    private void serverStopped(final FMLServerStoppedEvent event) {
        WorldData.instance().onServerStoppped();
        TickHandler.instance().shutdown();
    }

    @Override
    public Collection<ServerPlayerEntity> getPlayers() {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (server != null) {
            return server.getPlayerList().getPlayers();
        }

        return Collections.emptyList();
    }

    @Override
    public void sendToAllNearExcept(final PlayerEntity p, final double x, final double y, final double z,
            final double dist, final World w, final BasePacket packet) {
        if (w.isRemote()) {
            return;
        }
        for (final ServerPlayerEntity o : getPlayers()) {
            if (o != p && o.world == w) {
                final double dX = x - o.getPosX();
                final double dY = y - o.getPosY();
                final double dZ = z - o.getPosZ();
                if (dX * dX + dY * dY + dZ * dZ < dist * dist) {
                    NetworkHandler.instance().sendTo(packet, o);
                }
            }
        }
    }

    @Override
    public void setPartInteractionPlayer(final PlayerEntity player) {
        this.partInteractionPlayer.set(player);
    }

    @Override
    public CableRenderMode getCableRenderMode() {
        return this.getCableRenderModeForPlayer(partInteractionPlayer.get());
    }

    protected final CableRenderMode getCableRenderModeForPlayer(@Nullable final PlayerEntity player) {
        if (player != null) {
            for (int x = 0; x < PlayerInventory.getHotbarSize(); x++) {
                final ItemStack is = player.inventory.getStackInSlot(x);

                if (!is.isEmpty() && is.getItem() instanceof NetworkToolItem) {
                    final CompoundNBT c = is.getTag();
                    if (c != null && c.getBoolean("hideFacades")) {
                        return CableRenderMode.CABLE_VIEW;
                    }
                }
            }
        }

        return CableRenderMode.STANDARD;
    }

}
