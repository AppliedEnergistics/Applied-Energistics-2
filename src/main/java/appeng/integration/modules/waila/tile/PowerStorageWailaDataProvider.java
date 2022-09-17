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

package appeng.integration.modules.waila.tile;


import appeng.api.networking.energy.IAEPowerStorage;
import appeng.core.localization.WailaText;
import appeng.integration.modules.waila.BaseWailaDataProvider;
import appeng.util.Platform;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import mcp.mobius.waila.api.ITaggedList;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;


/**
 * Power storage provider for WAILA
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public final class PowerStorageWailaDataProvider extends BaseWailaDataProvider {
    /**
     * Power key used for the transferred {@link net.minecraft.nbt.NBTTagCompound}
     */
    private static final String ID_CURRENT_POWER = "currentPower";

    /**
     * Used cache for power if the power was not transmitted through the server.
     * <p/>
     * This is useful, when a player just started to look at a tile and thus just requested the new information from the
     * server.
     * <p/>
     * The cache will be updated from the server.
     */
    private final Object2LongMap<TileEntity> cache = new Object2LongOpenHashMap<>();

    /**
     * Adds the current and max power to the tool tip
     * Will ignore if the tile has an energy buffer ( &gt; 0 )
     *
     * @param itemStack      stack of power storage
     * @param currentToolTip current tool tip
     * @param accessor       wrapper for various world information
     * @param config         config to react to various settings
     * @return modified tool tip
     */
    @Override
    public List<String> getWailaBody(final ItemStack itemStack, final List<String> currentToolTip, final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        // Removes RF tooltip on WAILA 1.5.9+
        ((ITaggedList<String, String>) currentToolTip).removeEntries("RFEnergyStorage");

        final TileEntity te = accessor.getTileEntity();
        if (te instanceof IAEPowerStorage) {
            final IAEPowerStorage storage = (IAEPowerStorage) te;

            final double maxPower = storage.getAEMaxPower();
            if (maxPower > 0) {
                final NBTTagCompound tag = accessor.getNBTData();

                final long internalCurrentPower = this.getInternalCurrentPower(tag, te);

                if (internalCurrentPower >= 0) {
                    final long internalMaxPower = (long) (100 * maxPower);

                    final String formatCurrentPower = Platform.formatPowerLong(internalCurrentPower, false);
                    final String formatMaxPower = Platform.formatPowerLong(internalMaxPower, false);

                    currentToolTip.add(WailaText.Contains.getLocal() + ": " + formatCurrentPower + " / " + formatMaxPower);
                }
            }
        }

        return currentToolTip;
    }

    /**
     * Called on server to transfer information from server to client.
     * <p/>
     * If the {@link net.minecraft.tileentity.TileEntity} is a {@link appeng.api.networking.energy.IAEPowerStorage}, it
     * writes the power information to the {@code #tag} using the {@code #ID_CURRENT_POWER} key.
     *
     * @param player player looking at the power storage
     * @param te     power storage
     * @param tag    transferred tag which is send to the client
     * @param world  world of the power storage
     * @param pos    pos of the power storage
     * @return tag send to the client
     */
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        if (te instanceof IAEPowerStorage) {
            final IAEPowerStorage storage = (IAEPowerStorage) te;

            if (storage.getAEMaxPower() > 0) {
                final long internalCurrentPower = (long) (100 * storage.getAECurrentPower());

                tag.setLong(ID_CURRENT_POWER, internalCurrentPower);
            }
        }

        return tag;
    }

    /**
     * Determines the current power.
     * <p/>
     * If the client received power information on the server, they are used, else if the cache contains a previous
     * stored value, this will be used. Default value is 0.
     *
     * @param te  te to be looked at
     * @param tag tag maybe containing the channel information
     * @return used channels on the cable
     */
    private long getInternalCurrentPower(final NBTTagCompound tag, final TileEntity te) {
        final long internalCurrentPower;

        if (tag.hasKey(ID_CURRENT_POWER)) {
            internalCurrentPower = tag.getLong(ID_CURRENT_POWER);
            this.cache.put(te, internalCurrentPower);
        } else if (this.cache.containsKey(te)) {
            internalCurrentPower = this.cache.get(te);
        } else {
            internalCurrentPower = -1;
        }

        return internalCurrentPower;
    }
}
