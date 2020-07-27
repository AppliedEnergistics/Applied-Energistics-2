/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.client;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import appeng.api.parts.CableRenderMode;
import appeng.block.AEBaseBlock;
import appeng.client.render.effects.EnergyParticleData;
import appeng.client.render.effects.LightningArcFX;
import appeng.client.render.effects.LightningFX;
import appeng.client.render.effects.ParticleTypes;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.helpers.IMouseWheelItem;
import appeng.server.ServerHelper;
import appeng.util.Platform;

public class ClientHelper extends ServerHelper {
    private final static String KEY_CATEGORY = "key.appliedenergistics2.category";

    private final EnumMap<ActionKey, KeyBinding> bindings = new EnumMap<>(ActionKey.class);

    public void clientInit() {
        MinecraftForge.EVENT_BUS.addListener(this::postPlayerRender);
        MinecraftForge.EVENT_BUS.addListener(this::wheelEvent);

        for (ActionKey key : ActionKey.values()) {
            final KeyBinding binding = new KeyBinding(key.getTranslationKey(), key.getDefaultKey(), KEY_CATEGORY);
            ClientRegistry.registerKeyBinding(binding);
            this.bindings.put(key, binding);
        }
    }

    @Override
    public World getWorld() {
        if (Platform.isClient()) {
            return Minecraft.getInstance().world;
        } else {
            return super.getWorld();
        }
    }

    @Override
    public void bindTileEntitySpecialRenderer(final Class<? extends TileEntity> tile, final AEBaseBlock blk) {

    }

    @Override
    public List<? extends PlayerEntity> getPlayers() {
        if (Platform.isClient()) {
            return Collections.singletonList(Minecraft.getInstance().player);
        } else {
            return super.getPlayers();
        }
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
                case LightningArc:
                    this.spawnLightningArc(world, posX, posY, posZ, (Vector3d) o);
                    return;
                default:
            }
        }
    }

    @Override
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
    public RayTraceResult getRTR() {
        return Minecraft.getInstance().objectMouseOver;
    }

    @Override
    public void postInit() {
    }

    @Override
    public CableRenderMode getRenderMode() {
        if (Platform.isServer()) {
            return super.getRenderMode();
        }

        final Minecraft mc = Minecraft.getInstance();
        final PlayerEntity player = mc.player;

        return this.renderModeForPlayer(player);
    }

    @Override
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

    private void postPlayerRender(final RenderLivingEvent.Pre p) {
//	FIXME	final PlayerColor player = TickHandler.INSTANCE.getPlayerColors().get( p.getEntity().getEntityId() );
//	FIXME	if( player != null )
//	FIXME	{
//	FIXME		final AEColor col = player.myColor;
//	FIXME		final float r = 0xff & ( col.mediumVariant >> 16 );
//	FIXME		final float g = 0xff & ( col.mediumVariant >> 8 );
//	FIXME		final float b = 0xff & ( col.mediumVariant );
//	FIXME		// FIXME: This is most certainly not going to work!
//	FIXME		GlStateManager.color4f( r / 255.0f, g / 255.0f, b / 255.0f, 1.0f );
//	FIXME	}
    }

    private void spawnVibrant(final World w, final double x, final double y, final double z) {
        if (AppEng.proxy.shouldAddParticles(Platform.getRandom())) {
            final double d0 = (Platform.getRandomFloat() - 0.5F) * 0.26D;
            final double d1 = (Platform.getRandomFloat() - 0.5F) * 0.26D;
            final double d2 = (Platform.getRandomFloat() - 0.5F) * 0.26D;

            Minecraft.getInstance().particles.addParticle(ParticleTypes.VIBRANT, x + d0, y + d1, z + d2, 0.0D, 0.0D,
                    0.0D);
        }
    }

    private void spawnEnergy(final World w, final double posX, final double posY, final double posZ) {
        final float x = (float) (((Platform.getRandomInt() % 100) * 0.01) - 0.5) * 0.7f;
        final float y = (float) (((Platform.getRandomInt() % 100) * 0.01) - 0.5) * 0.7f;
        final float z = (float) (((Platform.getRandomInt() % 100) * 0.01) - 0.5) * 0.7f;

        Minecraft.getInstance().particles.addParticle(EnergyParticleData.FOR_BLOCK, posX + x, posY + y, posZ + z,
                -x * 0.1, -y * 0.1, -z * 0.1);
    }

    private void spawnLightning(final World world, final double posX, final double posY, final double posZ) {
        Minecraft.getInstance().particles.addParticle(ParticleTypes.LIGHTNING, posX, posY + 0.3f, posZ, 0.0f, 0.0f,
                0.0f);
    }

    private void spawnLightningArc(final World world, final double posX, final double posY, final double posZ,
            final Vector3d second) {
        final LightningFX fx = new LightningArcFX((ClientWorld) world, posX, posY, posZ, second.x, second.y, second.z, 0.0f, 0.0f,
                0.0f);
        Minecraft.getInstance().particles.addEffect(fx);
    }

    private void wheelEvent(final InputEvent.MouseScrollEvent me) {
        if (me.getScrollDelta() == 0) {
            return;
        }

        final Minecraft mc = Minecraft.getInstance();
        final PlayerEntity player = mc.player;
        if (player.isCrouching()) {
            final boolean mainHand = player.getHeldItem(Hand.MAIN_HAND).getItem() instanceof IMouseWheelItem;
            final boolean offHand = player.getHeldItem(Hand.OFF_HAND).getItem() instanceof IMouseWheelItem;

            if (mainHand || offHand) {
                NetworkHandler.instance()
                        .sendToServer(new ConfigValuePacket("Item", me.getScrollDelta() > 0 ? "WheelUp" : "WheelDown"));
                me.setCanceled(true);
            }
        }
    }

    @Override
    public boolean isActionKey(ActionKey key, InputMappings.Input pressedKey) {
        return this.bindings.get(key).isActiveAndMatches(pressedKey);
    }
}