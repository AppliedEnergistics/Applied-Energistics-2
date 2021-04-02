/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import appeng.api.parts.CableRenderMode;
import appeng.block.AEBaseBlock;
import appeng.client.ActionKey;
import appeng.client.EffectType;
import appeng.core.CommonHelper;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.NetworkHandler;
import appeng.items.tools.NetworkToolItem;
import appeng.util.Platform;

public class ServerHelper extends CommonHelper {

    /**
     * While we process a player-specific part placement/cable interaction packet, we need to use that player's
     * transparent-facade mode to understand whether the player can see through facades or not.
     * <p>
     * We need to use this method since the collision shape methods do not know about the player that the shape is being
     * requested for, so they will call {@link #getCableRenderMode()} below, which then will use this field to figure
     * out which player it's for.
     */
    private final ThreadLocal<PlayerEntity> partInteractionPlayer = new ThreadLocal<>();

    @Override
    public World getWorld() {
        throw new UnsupportedOperationException("This is a server...");
    }

    @Override
    public void bindTileEntitySpecialRenderer(final Class<? extends TileEntity> tile, final AEBaseBlock blk) {
        throw new UnsupportedOperationException("This is a server...");
    }

    @Override
    public List<? extends PlayerEntity> getPlayers() {
        if (!Platform.isClient()) {
            final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

            if (server != null) {
                return server.getPlayerList().getPlayers();
            }
        }

        return new ArrayList<>();
    }

    @Override
    public void sendToAllNearExcept(final PlayerEntity p, final double x, final double y, final double z,
            final double dist, final World w, final BasePacket packet) {
        if (w.isRemote()) {
            return;
        }
        for (final PlayerEntity o : this.getPlayers()) {
            final ServerPlayerEntity entityplayermp = (ServerPlayerEntity) o;
            if (entityplayermp != p && entityplayermp.world == w) {
                final double dX = x - entityplayermp.getPosX();
                final double dY = y - entityplayermp.getPosY();
                final double dZ = z - entityplayermp.getPosZ();
                if (dX * dX + dY * dY + dZ * dZ < dist * dist) {
                    NetworkHandler.instance().sendTo(packet, entityplayermp);
                }
            }
        }
    }

    @Override
    public void spawnEffect(final EffectType type, final World world, final double posX, final double posY,
            final double posZ, final Object o) {
        // :P
    }

    @Override
    public boolean shouldAddParticles(final Random r) {
        return false;
    }

    @Override
    public RayTraceResult getRTR() {
        return null;
    }

    @Override
    public void postInit() {

    }

    @Override
    public CableRenderMode getCableRenderMode() {
        return this.getCableRenderModeForPlayer(partInteractionPlayer.get());
    }

    @Override
    public void triggerUpdates() {

    }

    @Override
    public void setPartInteractionPlayer(final PlayerEntity player) {
        this.partInteractionPlayer.set(player);
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

    @Override
    public boolean isActionKey(ActionKey key, InputMappings.Input input) {
        return false;
    }
}
