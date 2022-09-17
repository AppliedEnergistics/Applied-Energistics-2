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


import appeng.api.parts.CableRenderMode;
import appeng.block.AEBaseBlock;
import appeng.client.ActionKey;
import appeng.client.EffectType;
import appeng.core.CommonHelper;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.NetworkHandler;
import appeng.items.tools.ToolNetworkTool;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ServerHelper extends CommonHelper {

    private EntityPlayer renderModeBased;

    @Override
    public void preinit() {

    }

    @Override
    public void init() {

    }

    @Override
    public World getWorld() {
        throw new UnsupportedOperationException("This is a server...");
    }

    @Override
    public void bindTileEntitySpecialRenderer(final Class<? extends TileEntity> tile, final AEBaseBlock blk) {
        throw new UnsupportedOperationException("This is a server...");
    }

    @Override
    public List<EntityPlayer> getPlayers() {
        if (!Platform.isClient()) {
            final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

            if (server != null) {
                return (List) server.getPlayerList().getPlayers();
            }
        }

        return new ArrayList<>();
    }

    @Override
    public void sendToAllNearExcept(final EntityPlayer p, final double x, final double y, final double z, final double dist, final World w, final AppEngPacket packet) {
        if (Platform.isClient()) {
            return;
        }

        for (final EntityPlayer o : this.getPlayers()) {
            final EntityPlayerMP entityplayermp = (EntityPlayerMP) o;

            if (entityplayermp != p && entityplayermp.world == w) {
                final double dX = x - entityplayermp.posX;
                final double dY = y - entityplayermp.posY;
                final double dZ = z - entityplayermp.posZ;

                if (dX * dX + dY * dY + dZ * dZ < dist * dist) {
                    NetworkHandler.instance().sendTo(packet, entityplayermp);
                }
            }
        }
    }

    @Override
    public void spawnEffect(final EffectType type, final World world, final double posX, final double posY, final double posZ, final Object o) {
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
    public CableRenderMode getRenderMode() {
        if (this.renderModeBased == null) {
            return CableRenderMode.STANDARD;
        }

        return this.renderModeForPlayer(this.renderModeBased);
    }

    @Override
    public void triggerUpdates() {

    }

    @Override
    public void updateRenderMode(final EntityPlayer player) {
        this.renderModeBased = player;
    }

    protected CableRenderMode renderModeForPlayer(final EntityPlayer player) {
        if (player != null) {
            for (int x = 0; x < InventoryPlayer.getHotbarSize(); x++) {
                final ItemStack is = player.inventory.getStackInSlot(x);

                if (!is.isEmpty() && is.getItem() instanceof ToolNetworkTool) {
                    final NBTTagCompound c = is.getTagCompound();
                    if (c != null && c.getBoolean("hideFacades")) {
                        return CableRenderMode.CABLE_VIEW;
                    }
                }
            }
        }

        return CableRenderMode.STANDARD;
    }

    @Override
    public boolean isKeyPressed(ActionKey key) {
        return false;
    }

    @Override
    public boolean isActionKey(ActionKey key, int pressedKeyCode) {
        return false;
    }
}
