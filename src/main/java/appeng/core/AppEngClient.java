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

import java.util.EnumMap;
import java.util.Objects;
import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import appeng.api.parts.CableRenderMode;
import appeng.client.ActionKey;
import appeng.client.EffectType;
import appeng.client.gui.style.StyleManager;
import appeng.client.render.effects.EnergyParticleData;
import appeng.client.render.effects.ParticleTypes;
import appeng.client.render.overlay.OverlayManager;
import appeng.client.render.tesr.InscriberTESR;
import appeng.client.render.tesr.SkyChestTESR;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.helpers.IMouseWheelItem;
import appeng.hooks.ticking.TickHandler;
import appeng.init.client.InitAdditionalModels;
import appeng.init.client.InitAutoRotatingModel;
import appeng.init.client.InitBlockColors;
import appeng.init.client.InitBlockEntityRenderers;
import appeng.init.client.InitBuiltInModels;
import appeng.init.client.InitEntityRendering;
import appeng.init.client.InitItemColors;
import appeng.init.client.InitItemModelsProperties;
import appeng.init.client.InitParticleFactories;
import appeng.init.client.InitRenderTypes;
import appeng.init.client.InitScreens;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

/**
 * Client-specific functionality.
 */
@OnlyIn(Dist.CLIENT)
public class AppEngClient extends AppEngBase {

    private final static String KEY_CATEGORY = "key.appliedenergistics2.category";

    private static AppEngClient INSTANCE;

    private final EnumMap<ActionKey, KeyBinding> bindings = new EnumMap<>(ActionKey.class);

    public AppEngClient() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::registerParticleFactories);
        modEventBus.addListener(this::registerTextures);
        modEventBus.addListener(this::modelRegistryEvent);
        modEventBus.addListener(this::registerBlockColors);
        modEventBus.addListener(this::registerItemColors);

        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, TickHandler.instance()::onClientTick);

        InitAutoRotatingModel.init(modEventBus);

        modEventBus.addListener(this::clientSetup);

        INSTANCE = this;
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getInstance().world;
    }

    @Nonnull
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

        MinecraftForge.EVENT_BUS.addListener(this::postPlayerRender);
        MinecraftForge.EVENT_BUS.addListener(this::wheelEvent);
        MinecraftForge.EVENT_BUS.register(OverlayManager.getInstance());

        for (ActionKey key : ActionKey.values()) {
            final KeyBinding binding = new KeyBinding(key.getTranslationKey(), key.getDefaultKey(), KEY_CATEGORY);
            ClientRegistry.registerKeyBinding(binding);
            this.bindings.put(key, binding);
        }

        InitEntityRendering.init();
    }

    /**
     * Called when other mods have finished initializing and the client is now available.
     */
    private void postClientSetup(Minecraft minecraft) {
        StyleManager.initialize(minecraft.getResourceManager());
        InitScreens.init();
    }

    @OnlyIn(Dist.CLIENT)
    public void modelRegistryEvent(ModelRegistryEvent event) {
        InitAdditionalModels.init();
        InitBlockEntityRenderers.init();
        InitItemModelsProperties.init();
        InitRenderTypes.init();
        InitBuiltInModels.init();
    }

    private void postPlayerRender(final RenderLivingEvent.Pre p) {
        // FIXME final PlayerColor player = TickHandler.INSTANCE.getPlayerColors().get( p.getEntity().getEntityId() );
        // FIXME if( player != null )
        // FIXME {
        // FIXME final AEColor col = player.myColor;
        // FIXME final float r = 0xff & ( col.mediumVariant >> 16 );
        // FIXME final float g = 0xff & ( col.mediumVariant >> 8 );
        // FIXME final float b = 0xff & ( col.mediumVariant );
        // FIXME // FIXME: This is most certainly not going to work!
        // FIXME GlStateManager.color4f( r / 255.0f, g / 255.0f, b / 255.0f, 1.0f );
        // FIXME }
    }

    private void wheelEvent(final InputEvent.MouseScrollEvent me) {
        if (me.getScrollDelta() == 0) {
            return;
        }

        final Minecraft mc = Minecraft.getInstance();
        final PlayerEntity player = mc.player;
        if (InteractionUtil.isInAlternateUseMode(player)) {
            final boolean mainHand = player.getHeldItem(Hand.MAIN_HAND).getItem() instanceof IMouseWheelItem;
            final boolean offHand = player.getHeldItem(Hand.OFF_HAND).getItem() instanceof IMouseWheelItem;

            if (mainHand || offHand) {
                NetworkHandler.instance()
                        .sendToServer(new ConfigValuePacket("Item", me.getScrollDelta() > 0 ? "WheelUp" : "WheelDown"));
                me.setCanceled(true);
            }
        }
    }

    public boolean isActionKey(ActionKey key, InputMappings.Input pressedKey) {
        return this.bindings.get(key).isActiveAndMatches(pressedKey);
    }

    public boolean shouldAddParticles(final Random r) {
        switch (Minecraft.getInstance().gameSettings.particles) {
            default:
            case ALL:
                return true;
            case DECREASED:
                return r.nextBoolean();
            case MINIMAL:
                return false;
        }
    }

    @Override
    public RayTraceResult getCurrentMouseOver() {
        return Minecraft.getInstance().objectMouseOver;
    }

    // FIXME: Instead of doing a custom packet and this dispatcher, we can use the
    // vanilla particle system
    @Override
    public void spawnEffect(final EffectType effect, final World world, final double posX, final double posY,
            final double posZ, final Object o) {
        if (AEConfig.instance().isEnableEffects()) {
            switch (effect) {
                case Vibrant:
                    this.spawnVibrant(world, posX, posY, posZ);
                    return;
                case Energy:
                    this.spawnEnergy(world, posX, posY, posZ);
                    return;
                case Lightning:
                    this.spawnLightning(world, posX, posY, posZ);
                    return;
                default:
            }
        }
    }

    private void spawnVibrant(final World w, final double x, final double y, final double z) {
        if (AppEngClient.instance().shouldAddParticles(Platform.getRandom())) {
            final double d0 = (Platform.getRandomFloat() - 0.5F) * 0.26D;
            final double d1 = (Platform.getRandomFloat() - 0.5F) * 0.26D;
            final double d2 = (Platform.getRandomFloat() - 0.5F) * 0.26D;

            Minecraft.getInstance().particles.addParticle(ParticleTypes.VIBRANT, x + d0, y + d1, z + d2, 0.0D, 0.0D,
                    0.0D);
        }
    }

    private void spawnEnergy(final World w, final double posX, final double posY, final double posZ) {
        final float x = (float) (Platform.getRandomInt() % 100 * 0.01 - 0.5) * 0.7f;
        final float y = (float) (Platform.getRandomInt() % 100 * 0.01 - 0.5) * 0.7f;
        final float z = (float) (Platform.getRandomInt() % 100 * 0.01 - 0.5) * 0.7f;

        Minecraft.getInstance().particles.addParticle(EnergyParticleData.FOR_BLOCK, posX + x, posY + y, posZ + z,
                -x * 0.1, -y * 0.1, -z * 0.1);
    }

    private void spawnLightning(final World world, final double posX, final double posY, final double posZ) {
        Minecraft.getInstance().particles.addParticle(ParticleTypes.LIGHTNING, posX, posY + 0.3f, posZ, 0.0f, 0.0f,
                0.0f);
    }

    public void triggerUpdates() {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.world == null) {
            return;
        }

        final PlayerEntity player = mc.player;

        final int x = (int) player.getPosX();
        final int y = (int) player.getPosY();
        final int z = (int) player.getPosZ();

        final int range = 16 * 16;

        mc.worldRenderer.markBlockRangeForRenderUpdate(x - range, y - range, z - range, x + range, y + range,
                z + range);
    }

    @Override
    public CableRenderMode getCableRenderMode() {
        if (Platform.isServer()) {
            return super.getCableRenderMode();
        }

        final Minecraft mc = Minecraft.getInstance();
        final PlayerEntity player = mc.player;

        return this.getCableRenderModeForPlayer(player);
    }

}
