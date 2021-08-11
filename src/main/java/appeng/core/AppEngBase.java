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

import javax.annotation.Nullable;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;
import net.minecraftforge.fmlserverevents.FMLServerAboutToStartEvent;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;
import net.minecraftforge.fmlserverevents.FMLServerStoppedEvent;
import net.minecraftforge.fmlserverevents.FMLServerStoppingEvent;

import appeng.api.parts.CableRenderMode;
import appeng.capabilities.Capabilities;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.stats.AdvancementTriggers;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.worlddata.WorldData;
import appeng.hooks.ticking.TickHandler;
import appeng.init.InitAdvancementTriggers;
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
import appeng.init.internal.InitGridServices;
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
    private final ThreadLocal<Player> partInteractionPlayer = new ThreadLocal<>();

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
        AEItems.init();
        AEBlocks.init();
        AEItems.init();
        AEParts.init();

        new FacadeItemGroup(); // This call has a side-effect (adding it to the creative screen)

        // Init other thread-safe registries
        InitGridServices.init();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::registerDimension);
        modEventBus.addGenericListener(Biome.class, this::registerBiomes);
        modEventBus.addGenericListener(Block.class, this::registerBlocks);
        modEventBus.addGenericListener(Item.class, this::registerItems);
        modEventBus.addGenericListener(EntityType.class, this::registerEntities);
        modEventBus.addGenericListener(ParticleType.class, this::registerParticleTypes);
        modEventBus.addGenericListener(BlockEntityType.class, this::registerBlockEntities);
        modEventBus.addGenericListener(MenuType.class, this::registerContainerTypes);
        modEventBus.addGenericListener(RecipeSerializer.class, this::registerRecipeSerializers);
        modEventBus.addGenericListener(StructureFeature.class, this::registerStructures);
        modEventBus.addGenericListener(Feature.class, this::registerFeatures);

        modEventBus.addListener(Integrations::enqueueIMC);
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.addListener(TickHandler.instance()::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(TickHandler.instance()::onWorldTick);
        MinecraftForge.EVENT_BUS.addListener(TickHandler.instance()::onUnloadChunk);
        // Try to go first for level loads since we use it to initialize state
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, TickHandler.instance()::onLoadWorld);
        // Try to go last for level unloads since we use it to clean-up state
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, TickHandler.instance()::onUnloadWorld);

        MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopped);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopping);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

        MinecraftForge.EVENT_BUS.register(new PartPlacement());
        MinecraftForge.EVENT_BUS.addListener(InitBiomeModifications::init);
    }

    private void setupRegistries(RegistryEvent.NewRegistry e) {
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(this::postRegistrationInitialization).whenComplete((res, err) -> {
            if (err != null) {
                AELog.warn(err);
            }
        });
    }

    /**
     * Runs after all mods have had time to run their registrations into registries.
     */
    public void postRegistrationInitialization() {
        InitP2PAttunements.init();

        // Do initialization that doesn't depend on mod registries being populated
        InitStats.init();
        InitAdvancementTriggers.init();

        Capabilities.register();
        InitDispenserBehavior.init();
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

    public void registerBiomes(RegistryEvent.Register<Biome> event) {
        InitBiomes.init(event.getRegistry());
    }

    public void registerBlocks(RegistryEvent.Register<Block> event) {
        InitBlocks.init(event.getRegistry());
    }

    public void registerItems(RegistryEvent.Register<Item> event) {
        InitItems.init(event.getRegistry());
    }

    public void registerBlockEntities(RegistryEvent.Register<BlockEntityType<?>> event) {
        InitBlockEntities.init(event.getRegistry());
    }

    public void registerContainerTypes(RegistryEvent.Register<MenuType<?>> event) {
        InitContainerTypes.init(event.getRegistry());
    }

    public void registerRecipeSerializers(RegistryEvent.Register<RecipeSerializer<?>> event) {
        InitRecipeSerializers.init(event.getRegistry());
    }

    public void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        InitEntityTypes.init(event.getRegistry());
    }

    public void registerParticleTypes(RegistryEvent.Register<ParticleType<?>> event) {
        InitParticleTypes.init(event.getRegistry());
    }

    public void registerStructures(RegistryEvent.Register<StructureFeature<?>> event) {
        InitStructures.init(event.getRegistry());
    }

    public void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
        InitFeatures.init(event.getRegistry());
    }

    public void registerCommands(final FMLServerStartingEvent evt) {
        CommandDispatcher<CommandSourceStack> dispatcher = evt.getServer().getCommands().getDispatcher();
        new AECommand().register(dispatcher);
    }

    public void registerDimension(RegistryEvent.NewRegistry e) {
        Registry.register(Registry.CHUNK_GENERATOR, SpatialStorageDimensionIds.CHUNK_GENERATOR_ID,
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
    public Collection<ServerPlayer> getPlayers() {
        var server = getCurrentServer();

        if (server != null) {
            return server.getPlayerList().getPlayers();
        }

        return Collections.emptyList();
    }

    @Override
    public void sendToAllNearExcept(final Player p, final double x, final double y, final double z,
            final double dist, final Level level, final BasePacket packet) {
        if (level.isClientSide()) {
            return;
        }
        for (final ServerPlayer o : getPlayers()) {
            if (o != p && o.level == level) {
                final double dX = x - o.getX();
                final double dY = y - o.getY();
                final double dZ = z - o.getZ();
                if (dX * dX + dY * dY + dZ * dZ < dist * dist) {
                    NetworkHandler.instance().sendTo(packet, o);
                }
            }
        }
    }

    @Override
    public void setPartInteractionPlayer(final Player player) {
        this.partInteractionPlayer.set(player);
    }

    @Override
    public CableRenderMode getCableRenderMode() {
        return this.getCableRenderModeForPlayer(partInteractionPlayer.get());
    }

    @Nullable
    @Override
    public MinecraftServer getCurrentServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    protected final CableRenderMode getCableRenderModeForPlayer(@Nullable final Player player) {
        if (player != null) {
            for (int x = 0; x < Inventory.getSelectionSize(); x++) {
                final ItemStack is = player.getInventory().getItem(x);

                if (!is.isEmpty() && is.getItem() instanceof NetworkToolItem) {
                    final CompoundTag c = is.getTag();
                    if (c != null && c.getBoolean("hideFacades")) {
                        return CableRenderMode.CABLE_VIEW;
                    }
                }
            }
        }

        return CableRenderMode.STANDARD;
    }

}
