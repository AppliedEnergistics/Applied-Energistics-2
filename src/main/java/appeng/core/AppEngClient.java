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

import java.util.Objects;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import appeng.api.parts.CableRenderMode;
import appeng.api.parts.PartHelper;
import appeng.client.EffectType;
import appeng.client.Hotkeys;
import appeng.client.gui.style.StyleManager;
import appeng.client.render.StorageCellClientTooltipComponent;
import appeng.client.render.effects.EnergyParticleData;
import appeng.client.render.effects.ParticleTypes;
import appeng.client.render.overlay.OverlayManager;
import appeng.client.render.tesr.InscriberTESR;
import appeng.client.render.tesr.SkyChestTESR;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.MouseWheelPacket;
import appeng.helpers.IMouseWheelItem;
import appeng.hooks.BlockAttackHook;
import appeng.hooks.RenderBlockOutlineHook;
import appeng.hotkeys.HotkeyActions;
import appeng.init.client.InitAdditionalModels;
import appeng.init.client.InitAutoRotatingModel;
import appeng.init.client.InitBlockColors;
import appeng.init.client.InitBlockEntityRenderers;
import appeng.init.client.InitBuiltInModels;
import appeng.init.client.InitEntityLayerDefinitions;
import appeng.init.client.InitEntityRendering;
import appeng.init.client.InitItemColors;
import appeng.init.client.InitItemModelsProperties;
import appeng.init.client.InitParticleFactories;
import appeng.init.client.InitRenderTypes;
import appeng.init.client.InitScreens;
import appeng.init.client.InitStackRenderHandlers;
import appeng.items.storage.StorageCellTooltipComponent;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

/**
 * Client-specific functionality.
 */
@OnlyIn(Dist.CLIENT)
public class AppEngClient extends AppEngBase {

    private final static String KEY_CATEGORY = "key.ae2.category";

    private static AppEngClient INSTANCE;

    /**
     * Last known cable render mode. Used to update all rendered blocks once at the end of the tick when the mode is
     * changed.
     */
    private CableRenderMode prevCableRenderMode = CableRenderMode.STANDARD;

    public AppEngClient() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        this.registerClientTooltipComponents();

        modEventBus.addListener(this::registerParticleFactories);
        modEventBus.addListener(this::registerTextures);
        modEventBus.addListener(this::modelRegistryEvent);
        modEventBus.addListener(this::registerBlockColors);
        modEventBus.addListener(this::registerItemColors);
        modEventBus.addListener(this::registerEntityRenderers);
        modEventBus.addListener(this::registerEntityLayerDefinitions);

        BlockAttackHook.install();
        RenderBlockOutlineHook.install();

        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, (TickEvent.ClientTickEvent e) -> {
            if (e.phase == TickEvent.Phase.START) {
                updateCableRenderMode();
            }
        });

        InitAutoRotatingModel.init(modEventBus);

        modEventBus.addListener(this::clientSetup);

        INSTANCE = this;

        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent e) -> {
            if (e.phase == TickEvent.Phase.END) {
                Hotkeys.checkHotkeys();
            }
        });

        registerTests();
    }

    @Override
    public Level getClientLevel() {
        return Minecraft.getInstance().level;
    }

    @Override
    public void registerHotkey(String id) {
        Hotkeys.registerHotkey(id);
    }

    public static AppEngClient instance() {
        return Objects.requireNonNull(INSTANCE, "AppEngClient is not initialized");
    }

    public void registerParticleFactories(ParticleFactoryRegisterEvent event) {
        InitParticleFactories.init();
    }

    public void registerTextures(TextureStitchEvent.Pre event) {
        SkyChestTESR.registerTextures(event);
        InscriberTESR.registerTexture(event);
    }

    public void registerBlockColors(ColorHandlerEvent.Block event) {
        InitBlockColors.init(event.getBlockColors());
    }

    public void registerItemColors(ColorHandlerEvent.Item event) {
        InitItemColors.init(event.getItemColors());
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            postClientSetup(minecraft);
        });

        MinecraftForge.EVENT_BUS.addListener(this::wheelEvent);
        MinecraftForge.EVENT_BUS.register(OverlayManager.getInstance());

        HotkeyActions.init();
    }

    private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        InitEntityRendering.init(event::registerEntityRenderer);
    }

    private void registerEntityLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        InitEntityLayerDefinitions.init((modelLayerLocation, layerDefinition) -> {
            event.registerLayerDefinition(modelLayerLocation, () -> layerDefinition);
        });
    }

    private void registerClientTooltipComponents() {
        MinecraftForgeClient.registerTooltipComponentFactory(StorageCellTooltipComponent.class,
                StorageCellClientTooltipComponent::new);
    }

    /**
     * Called when other mods have finished initializing and the client is now available.
     */
    private void postClientSetup(Minecraft minecraft) {
        StyleManager.initialize(minecraft.getResourceManager());
        InitScreens.init();
        InitStackRenderHandlers.init();
    }

    @OnlyIn(Dist.CLIENT)
    public void modelRegistryEvent(ModelRegistryEvent event) {
        InitAdditionalModels.init();
        InitBlockEntityRenderers.init();
        InitItemModelsProperties.init();
        InitRenderTypes.init();
        InitBuiltInModels.init();
    }

    private void wheelEvent(final InputEvent.MouseScrollEvent me) {
        if (me.getScrollDelta() == 0) {
            return;
        }

        final Minecraft mc = Minecraft.getInstance();
        final Player player = mc.player;
        if (InteractionUtil.isInAlternateUseMode(player)) {
            final boolean mainHand = player.getItemInHand(InteractionHand.MAIN_HAND)
                    .getItem() instanceof IMouseWheelItem;
            final boolean offHand = player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof IMouseWheelItem;

            if (mainHand || offHand) {
                NetworkHandler.instance().sendToServer(new MouseWheelPacket(me.getScrollDelta() > 0));
                me.setCanceled(true);
            }
        }
    }

    public boolean shouldAddParticles(Random r) {
        return switch (Minecraft.getInstance().options.particles) {
            case ALL -> true;
            case DECREASED -> r.nextBoolean();
            case MINIMAL -> false;
        };
    }

    @Override
    public HitResult getCurrentMouseOver() {
        return Minecraft.getInstance().hitResult;
    }

    // FIXME: Instead of doing a custom packet and this dispatcher, we can use the
    // vanilla particle system
    @Override
    public void spawnEffect(EffectType effect, Level level, double posX, double posY,
            double posZ, Object o) {
        if (AEConfig.instance().isEnableEffects()) {
            switch (effect) {
                case Vibrant:
                    this.spawnVibrant(level, posX, posY, posZ);
                    return;
                case Energy:
                    this.spawnEnergy(level, posX, posY, posZ);
                    return;
                case Lightning:
                    this.spawnLightning(level, posX, posY, posZ);
                    return;
                default:
            }
        }
    }

    private void spawnVibrant(Level level, double x, double y, double z) {
        if (AppEngClient.instance().shouldAddParticles(Platform.getRandom())) {
            final double d0 = (Platform.getRandomFloat() - 0.5F) * 0.26D;
            final double d1 = (Platform.getRandomFloat() - 0.5F) * 0.26D;
            final double d2 = (Platform.getRandomFloat() - 0.5F) * 0.26D;

            Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.VIBRANT, x + d0, y + d1, z + d2, 0.0D,
                    0.0D,
                    0.0D);
        }
    }

    private void spawnEnergy(Level level, double posX, double posY, double posZ) {
        final float x = (float) (Platform.getRandomInt() % 100 * 0.01 - 0.5) * 0.7f;
        final float y = (float) (Platform.getRandomInt() % 100 * 0.01 - 0.5) * 0.7f;
        final float z = (float) (Platform.getRandomInt() % 100 * 0.01 - 0.5) * 0.7f;

        Minecraft.getInstance().particleEngine.createParticle(EnergyParticleData.FOR_BLOCK, posX + x, posY + y,
                posZ + z,
                -x * 0.1, -y * 0.1, -z * 0.1);
    }

    private void spawnLightning(Level level, double posX, double posY, double posZ) {
        Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.LIGHTNING, posX, posY + 0.3f, posZ, 0.0f,
                0.0f,
                0.0f);
    }

    private void updateCableRenderMode() {
        var currentMode = PartHelper.getCableRenderMode();

        // Handle changes to the cable-rendering mode
        if (currentMode == this.prevCableRenderMode) {
            return;
        }

        this.prevCableRenderMode = currentMode;

        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        final Player player = mc.player;

        final int x = (int) player.getX();
        final int y = (int) player.getY();
        final int z = (int) player.getZ();

        final int range = 16 * 16;

        mc.levelRenderer.setBlocksDirty(x - range, y - range, z - range, x + range, y + range,
                z + range);
    }

    @Override
    public CableRenderMode getCableRenderMode() {
        if (Platform.isServer()) {
            return super.getCableRenderMode();
        }

        final Minecraft mc = Minecraft.getInstance();
        final Player player = mc.player;

        return this.getCableRenderModeForPlayer(player);
    }

}
