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
import net.minecraft.gametest.framework.GameTestRegistry;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.server.ServerLifecycleHooks;

import appeng.api.parts.CableRenderMode;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.AEKeyTypesInternal;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.NetworkHandler;
import appeng.hooks.SkyStoneBreakSpeed;
import appeng.hooks.WrenchHook;
import appeng.hooks.ticking.TickHandler;
import appeng.init.InitBlockEntities;
import appeng.init.InitBlocks;
import appeng.init.InitCapabilities;
import appeng.init.InitDispenserBehavior;
import appeng.init.InitEntityTypes;
import appeng.init.InitItems;
import appeng.init.InitMenuTypes;
import appeng.init.InitRecipeSerializers;
import appeng.init.client.InitParticleTypes;
import appeng.init.internal.InitGridLinkables;
import appeng.init.internal.InitP2PAttunements;
import appeng.init.internal.InitStorageCells;
import appeng.init.internal.InitUpgrades;
import appeng.init.worldgen.InitBiomeModifications;
import appeng.init.worldgen.InitBiomes;
import appeng.init.worldgen.InitFeatures;
import appeng.init.worldgen.InitStructures;
import appeng.integration.Integrations;
import appeng.items.tools.NetworkToolItem;
import appeng.server.AECommand;
import appeng.server.services.ChunkLoadingService;
import appeng.server.testworld.GameTestPlotAdapter;
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

    public AppEngBase() {
        if (INSTANCE != null) {
            throw new IllegalStateException();
        }
        INSTANCE = this;

        // Now that item instances are available, we can initialize registries that need item instances
        InitGridLinkables.init();
        InitStorageCells.init();

        FacadeItemGroup.init();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::registerRegistries);
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
        modEventBus.addGenericListener(AEKeyType.class, this::registerKeyTypes);

        modEventBus.addListener(InitCapabilities::init);
        modEventBus.addListener(Integrations::enqueueIMC);
        modEventBus.addListener(this::commonSetup);

        TickHandler.instance().init();

        MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopped);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopping);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

        MinecraftForge.EVENT_BUS.addListener(WrenchHook::onPlayerUseBlockEvent);
        MinecraftForge.EVENT_BUS.addListener(InitBiomeModifications::init);
        MinecraftForge.EVENT_BUS.addListener(SkyStoneBreakSpeed::handleBreakFaster);
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, InitCapabilities::registerGenericInvWrapper);
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
        // This has to be here because it relies on caps and god knows when those are available...
        InitP2PAttunements.init();

        InitDispenserBehavior.init();

        AEConfig.instance().save();
        InitUpgrades.init();
        NetworkHandler.init(new ResourceLocation(MOD_ID, "main"));

        ChunkLoadingService.register();
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
        InitMenuTypes.init(event.getRegistry());
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

    public void registerKeyTypes(RegistryEvent.Register<AEKeyType> event) {
        AEKeyTypes.register(AEKeyType.items());
        AEKeyTypes.register(AEKeyType.fluids());
    }

    public void registerCommands(final ServerStartingEvent evt) {
        CommandDispatcher<CommandSourceStack> dispatcher = evt.getServer().getCommands().getDispatcher();
        new AECommand().register(dispatcher);
    }

    public void registerRegistries(NewRegistryEvent e) {
        Registry.register(Registry.CHUNK_GENERATOR, SpatialStorageDimensionIds.CHUNK_GENERATOR_ID,
                SpatialStorageChunkGenerator.CODEC);

        var supplier = e.create(new RegistryBuilder<AEKeyType>()
                .setType(AEKeyType.class)
                .setMaxID(127)
                .setName(AppEng.makeId("keytypes")));
        AEKeyTypesInternal.setRegistry(supplier);
    }

    private void onServerAboutToStart(final ServerAboutToStartEvent evt) {
        ChunkLoadingService.getInstance().onServerAboutToStart(evt);
    }

    private void serverStopping(final ServerStoppingEvent event) {
        ChunkLoadingService.getInstance().onServerStopping(event);
    }

    private void serverStopped(final ServerStoppedEvent event) {
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
        return ServerLifecycleHooks.getCurrentServer();
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

    protected static void registerTests() {
        if ("true".equals(System.getProperty("appeng.tests"))) {
            GameTestRegistry.register(GameTestPlotAdapter.class);
        }
    }
}
