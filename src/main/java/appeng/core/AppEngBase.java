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

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.nbt.CompoundTag;
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

import appeng.api.IAEAddonEntrypoint;
import appeng.api.parts.CableRenderMode;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.network.ServerNetworkHandler;
import appeng.hooks.MachineStateUpdates;
import appeng.hooks.ToolItemHook;
import appeng.hooks.WrenchHook;
import appeng.hooks.ticking.TickHandler;
import appeng.init.InitApiLookup;
import appeng.init.InitBlockEntities;
import appeng.init.InitBlocks;
import appeng.init.InitDispenserBehavior;
import appeng.init.InitEntityTypes;
import appeng.init.InitItems;
import appeng.init.InitMenuTypes;
import appeng.init.InitRecipeSerializers;
import appeng.init.client.InitKeyTypes;
import appeng.init.client.InitParticleTypes;
import appeng.init.internal.InitGridLinkables;
import appeng.init.internal.InitP2PAttunements;
import appeng.init.internal.InitStorageCells;
import appeng.init.internal.InitUpgrades;
import appeng.init.worldgen.InitBiomeModifications;
import appeng.init.worldgen.InitBiomes;
import appeng.init.worldgen.InitFeatures;
import appeng.init.worldgen.InitStructures;
import appeng.items.tools.NetworkToolItem;
import appeng.server.AECommand;
import appeng.server.testworld.GameTestPlotAdapter;
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

    private MinecraftServer currentServer;

    public AppEngBase() {
        if (INSTANCE != null) {
            throw new IllegalStateException();
        }
        INSTANCE = this;

        AEConfig.load(FabricLoader.getInstance().getConfigDir());

        InitKeyTypes.init();

        CreativeTab.init();

        // Initialize items in order
        AEItems.init();
        AEBlocks.init();
        AEParts.init();

        // Now that item instances are available, we can initialize registries that need item instances
        InitGridLinkables.init();
        InitStorageCells.init();

        FacadeCreativeTab.init(); // This call has a side-effect (adding it to the creative screen)

        registerDimension();
        registerBiomes(BuiltinRegistries.BIOME);
        registerBlocks(Registry.BLOCK);
        registerItems(Registry.ITEM);
        registerEntities(Registry.ENTITY_TYPE);
        registerParticleTypes(Registry.PARTICLE_TYPE);
        registerBlockEntities(Registry.BLOCK_ENTITY_TYPE);
        registerMenuTypes(Registry.MENU);
        registerRecipeSerializers(Registry.RECIPE_SERIALIZER);
        registerStructures(Registry.STRUCTURE_FEATURE);
        registerFeatures(Registry.FEATURE);

        postRegistrationInitialization();

        TickHandler.instance().init();

        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerAboutToStart);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::serverStopped);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::serverStopping);
        ServerLifecycleEvents.SERVER_STARTING.register(this::registerCommands);

        UseBlockCallback.EVENT.register(WrenchHook::onPlayerUseBlock);
        UseBlockCallback.EVENT.register(ToolItemHook::onPlayerUseBlock);
        InitBiomeModifications.init();

        MachineStateUpdates.init();
    }

    /**
     * Runs after all mods have had time to run their registrations into registries.
     */
    public void postRegistrationInitialization() {
        // This has to be here because it relies on caps and god knows when those are available...
        InitP2PAttunements.init();

        InitApiLookup.init();
        InitDispenserBehavior.init();

        AEConfig.instance().save();
        InitUpgrades.init();
        initNetworkHandler();

        ChunkLoadingService.register();
    }

    protected void initNetworkHandler() {
        new ServerNetworkHandler();
    }

    public void registerBiomes(Registry<Biome> registry) {
        InitBiomes.init(registry);
    }

    public void registerBlocks(Registry<Block> registry) {
        InitBlocks.init(registry);
    }

    public void registerItems(Registry<Item> registry) {
        InitItems.init(registry);
    }

    public void registerBlockEntities(Registry<BlockEntityType<?>> registry) {
        InitBlockEntities.init(registry);
    }

    public void registerMenuTypes(Registry<MenuType<?>> registry) {
        InitMenuTypes.init(registry);
    }

    public void registerRecipeSerializers(Registry<RecipeSerializer<?>> registry) {
        InitRecipeSerializers.init(registry);
    }

    public void registerEntities(Registry<EntityType<?>> registry) {
        InitEntityTypes.init(registry);
    }

    public void registerParticleTypes(Registry<ParticleType<?>> registry) {
        InitParticleTypes.init(registry);
    }

    public void registerStructures(Registry<StructureFeature<?>> registry) {
        InitStructures.init(registry);
    }

    public void registerFeatures(Registry<Feature<?>> registry) {
        InitFeatures.init(registry);
    }

    public void registerCommands(MinecraftServer server) {
        CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
        new AECommand().register(dispatcher);
    }

    public void registerDimension() {
        Registry.register(Registry.CHUNK_GENERATOR, SpatialStorageDimensionIds.CHUNK_GENERATOR_ID,
                SpatialStorageChunkGenerator.CODEC);
    }

    private void onServerAboutToStart(MinecraftServer server) {
        this.currentServer = server;
        ChunkLoadingService.getInstance().onServerAboutToStart();
    }

    private void serverStopping(MinecraftServer server) {
        ChunkLoadingService.getInstance().onServerStopping();
    }

    private void serverStopped(MinecraftServer server) {
        TickHandler.instance().shutdown();
        if (this.currentServer == server) {
            this.currentServer = null;
        }
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
    public void sendToAllNearExcept(Player p, double x, double y, double z,
            double dist, Level level, BasePacket packet) {
        if (level.isClientSide()) {
            return;
        }
        for (ServerPlayer o : getPlayers()) {
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
    public void setPartInteractionPlayer(Player player) {
        this.partInteractionPlayer.set(player);
    }

    @Override
    public CableRenderMode getCableRenderMode() {
        return this.getCableRenderModeForPlayer(partInteractionPlayer.get());
    }

    @Nullable
    @Override
    public MinecraftServer getCurrentServer() {
        return currentServer;
    }

    protected final CableRenderMode getCableRenderModeForPlayer(@Nullable Player player) {
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

    protected final void notifyAddons(String sideSpecificEntrypoint) {
        var entrypoints = FabricLoader.getInstance().getEntrypoints(AppEng.MOD_ID, IAEAddonEntrypoint.class);
        for (var entrypoint : entrypoints) {
            entrypoint.onAe2Initialized();
        }

        var sideSpecificEntrypoints = FabricLoader.getInstance()
                .getEntrypoints(AppEng.MOD_ID + ":" + sideSpecificEntrypoint, IAEAddonEntrypoint.class);
        for (var entrypoint : sideSpecificEntrypoints) {
            entrypoint.onAe2Initialized();
        }
    }

    protected static void registerTests() {
        if ("true".equals(System.getProperty("appeng.tests"))) {
            GameTestRegistry.register(GameTestPlotAdapter.class);
        }
    }
}
