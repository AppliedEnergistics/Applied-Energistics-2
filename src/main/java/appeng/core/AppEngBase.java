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

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import appeng.api.ids.AEComponents;
import appeng.api.parts.CableRenderMode;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypesInternal;
import appeng.core.definitions.AEAttachmentTypes;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEEntities;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.InitNetwork;
import appeng.core.particles.InitParticleTypes;
import appeng.hooks.SkyStoneBreakSpeed;
import appeng.hooks.WrenchHook;
import appeng.hooks.ticking.TickHandler;
import appeng.hotkeys.HotkeyActions;
import appeng.init.InitAdvancementTriggers;
import appeng.init.InitCapabilityProviders;
import appeng.init.InitCauldronInteraction;
import appeng.init.InitDispenserBehavior;
import appeng.init.InitMenuTypes;
import appeng.init.InitStats;
import appeng.init.InitVillager;
import appeng.init.internal.InitBlockEntityMoveStrategies;
import appeng.init.internal.InitGridLinkables;
import appeng.init.internal.InitGridServices;
import appeng.init.internal.InitP2PAttunements;
import appeng.init.internal.InitStorageCells;
import appeng.init.internal.InitUpgrades;
import appeng.init.worldgen.InitStructures;
import appeng.integration.Integrations;
import appeng.recipes.AERecipeSerializers;
import appeng.recipes.AERecipeTypes;
import appeng.server.AECommand;
import appeng.server.services.ChunkLoadingService;
import appeng.server.testworld.GameTestPlotAdapter;
import appeng.sounds.AppEngSounds;
import appeng.spatial.SpatialStorageChunkGenerator;
import appeng.spatial.SpatialStorageDimensionIds;

/**
 * Mod functionality that is common to both dedicated server and client.
 * <p>
 * Note that a client will still have zero or more embedded servers (although only one at a time).
 */
public abstract class AppEngBase implements AppEng {

    private static final Logger LOG = LoggerFactory.getLogger(AppEngBase.class);

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

    public AppEngBase(IEventBus modEventBus, ModContainer container) {
        if (INSTANCE != null) {
            throw new IllegalStateException();
        }
        INSTANCE = this;

        AEConfig.register(container);

        InitGridServices.init();
        InitBlockEntityMoveStrategies.init();

        AEParts.init();
        AEBlocks.DR.register(modEventBus);
        AEItems.DR.register(modEventBus);
        AEBlockEntities.DR.register(modEventBus);
        AEComponents.DR.register(modEventBus);
        AEEntities.DR.register(modEventBus);
        AERecipeTypes.DR.register(modEventBus);
        AERecipeSerializers.DR.register(modEventBus);
        InitStructures.register(modEventBus);
        AEAttachmentTypes.register(modEventBus);

        modEventBus.addListener(this::registerRegistries);
        modEventBus.addListener(MainCreativeTab::initExternal);
        modEventBus.addListener(InitNetwork::init);
        modEventBus.addListener(ChunkLoadingService.getInstance()::register);
        modEventBus.addListener(InitCapabilityProviders::register);
        modEventBus.addListener(EventPriority.LOWEST, InitCapabilityProviders::registerGenericAdapters);
        modEventBus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey() == Registries.SOUND_EVENT) {
                registerSounds(BuiltInRegistries.SOUND_EVENT);
            } else if (event.getRegistryKey() == Registries.CREATIVE_MODE_TAB) {
                registerCreativeTabs(BuiltInRegistries.CREATIVE_MODE_TAB);
            } else if (event.getRegistryKey() == Registries.CUSTOM_STAT) {
                InitStats.init(event.getRegistry(Registries.CUSTOM_STAT));
            } else if (event.getRegistryKey() == Registries.TRIGGER_TYPE) {
                InitAdvancementTriggers.init(event.getRegistry(Registries.TRIGGER_TYPE));
            } else if (event.getRegistryKey() == Registries.PARTICLE_TYPE) {
                InitParticleTypes.init(event.getRegistry(Registries.PARTICLE_TYPE));
            } else if (event.getRegistryKey() == Registries.MENU) {
                InitMenuTypes.init(event.getRegistry(Registries.MENU));
            } else if (event.getRegistryKey() == Registries.CHUNK_GENERATOR) {
                Registry.register(BuiltInRegistries.CHUNK_GENERATOR, SpatialStorageDimensionIds.CHUNK_GENERATOR_ID,
                        SpatialStorageChunkGenerator.CODEC);
            } else if (event.getRegistryKey() == Registries.VILLAGER_PROFESSION) {
                InitVillager.initProfession(event.getRegistry(Registries.VILLAGER_PROFESSION));
            } else if (event.getRegistryKey() == Registries.POINT_OF_INTEREST_TYPE) {
                InitVillager.initPointOfInterestType(event.getRegistry(Registries.POINT_OF_INTEREST_TYPE));
            } else if (event.getRegistryKey() == AEKeyType.REGISTRY_KEY) {
                registerKeyTypes(event.getRegistry(AEKeyType.REGISTRY_KEY));
            } else if (event.getRegistryKey() == Registries.TEST_INSTANCE_TYPE) {
                event.register(Registries.TEST_INSTANCE_TYPE, AppEng.makeId("plot_adapter"),
                        () -> GameTestPlotAdapter.CODEC);
            }
        });

        NeoForge.EVENT_BUS.addListener(InitVillager::initTrades);

        modEventBus.addListener(Integrations::enqueueIMC);
        modEventBus.addListener(this::commonSetup);

        modEventBus.addListener(this::registerTests);

        TickHandler.instance().init();

        NeoForge.EVENT_BUS.addListener(this::onServerAboutToStart);
        NeoForge.EVENT_BUS.addListener(this::serverStopped);
        NeoForge.EVENT_BUS.addListener(this::serverStopping);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);

        NeoForge.EVENT_BUS.addListener(WrenchHook::onPlayerUseBlockEvent);
        NeoForge.EVENT_BUS.addListener(SkyStoneBreakSpeed::handleBreakFaster);

        HotkeyActions.init();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(this::postRegistrationInitialization).whenComplete((res, err) -> {
            if (err != null) {
                LOG.error("Common setup failed", err);
            }
        });
    }

    /**
     * Runs after all mods have had time to run their registrations into registries.
     */
    public void postRegistrationInitialization() {
        // Now that item instances are available, we can initialize registries that need item instances
        InitGridLinkables.init();
        InitStorageCells.init();

        InitP2PAttunements.init();

        InitCauldronInteraction.init();
        InitDispenserBehavior.init();

        InitUpgrades.init();
    }

    public void registerKeyTypes(Registry<AEKeyType> registry) {
        Registry.register(registry, AEKeyType.items().getId(), AEKeyType.items());
        Registry.register(registry, AEKeyType.fluids().getId(), AEKeyType.fluids());
    }

    public void registerCommands(RegisterCommandsEvent evt) {
        new AECommand().register(evt.getDispatcher());
    }

    public void registerSounds(Registry<SoundEvent> registry) {
        AppEngSounds.register(registry);
    }

    public void registerRegistries(NewRegistryEvent e) {
        var registry = e.create(new RegistryBuilder<>(AEKeyType.REGISTRY_KEY)
                .sync(true)
                .maxId(127));
        AEKeyTypesInternal.setRegistry(registry);
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

    public void registerCreativeTabs(Registry<CreativeModeTab> registry) {
        MainCreativeTab.init(registry);
        FacadeCreativeTab.init(registry);
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
            double dist, Level level, ClientboundPacket packet) {
        if (level instanceof ServerLevel serverLevel) {
            ServerPlayer except = null;
            if (p instanceof ServerPlayer) {
                except = (ServerPlayer) p;
            }
            PacketDistributor.sendToPlayersNear(serverLevel, except, x, y, z, dist, packet);
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

    @Override
    public void sendSystemMessage(Player player, Component text) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(text);
        }
    }

    protected final CableRenderMode getCableRenderModeForPlayer(@Nullable Player player) {
        if (player != null) {
            if (AEItems.NETWORK_TOOL.is(player.getItemInHand(InteractionHand.MAIN_HAND))
                    || AEItems.NETWORK_TOOL.is(player.getItemInHand(InteractionHand.OFF_HAND))) {
                return CableRenderMode.CABLE_VIEW;
            }
        }

        return CableRenderMode.STANDARD;
    }

    private void registerTests(RegisterGameTestsEvent e) {
        if ("true".equals(System.getProperty("appeng.tests"))) {
            GameTestPlotAdapter.registerAll(e::registerTest);
        }
    }
}
