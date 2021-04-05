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

package appeng.integration.modules.waila.part;

import java.util.List;
import java.util.WeakHashMap;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;

import appeng.api.parts.IPart;
import appeng.core.localization.WailaText;
import appeng.parts.networking.IUsedChannelProvider;

/**
 * Channel-information provider for WAILA
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public final class ChannelWailaDataProvider extends BasePartWailaDataProvider {
    /**
     * Channel key used for the transferred {@link net.minecraft.nbt.CompoundNBT}
     */
    private static final String ID_USED_CHANNELS = "usedChannels";
    private static final String ID_MAX_CHANNELS = "maxChannels";

    /**
     * Used cache for channels if the channel was not transmitted through the server.
     * <p/>
     * This is useful, when a player just started to look at a tile and thus just requested the new information from the
     * server.
     * <p/>
     * The cache will be updated from the server.
     */
    private final WeakHashMap<IPart, UsedChannelInfo> cache = new WeakHashMap<>();

    /**
     * Adds the used and max channel to the tool tip
     *
     * @param part     being looked at part
     * @param tooltip  current tool tip
     * @param accessor wrapper for various world information
     * @param config   config to react to various settings
     */
    @Override
    public void appendBody(final IPart part, final List<ITextComponent> tooltip, final IDataAccessor accessor,
            final IPluginConfig config) {
        if (part instanceof IUsedChannelProvider) {
            final CompoundNBT tag = accessor.getServerData();

            UsedChannelInfo usedChannelInfo = this.getUsedChannels(part, tag);

            if (usedChannelInfo != null) {
                final ITextComponent formattedToolTip = WailaText.Channels.textComponent(
                        usedChannelInfo.usedChannels,
                        usedChannelInfo.maxChannels);
                tooltip.add(formattedToolTip);
            }
        }
    }

    /**
     * Determines the source of the channel.
     * <p/>
     * If the client received information of the channels on the server, they are used, else if the cache contains a
     * previous stored value, this will be used. Default value is 0.
     *
     * @param part part to be looked at
     * @param tag  tag maybe containing the channel information
     * @return used channels on the cable
     */
    private UsedChannelInfo getUsedChannels(final IPart part, final CompoundNBT tag) {
        UsedChannelInfo info;

        if (tag.contains(ID_USED_CHANNELS) && tag.contains(ID_MAX_CHANNELS)) {
            info = new UsedChannelInfo(tag.getInt(ID_USED_CHANNELS), tag.getInt(ID_MAX_CHANNELS));
            this.cache.put(part, info);
        } else {
            info = this.cache.get(part);
        }

        return info;
    }

    /**
     * Called on server to transfer information from server to client.
     * <p/>
     * If the part is a cable, it writes the channel information in the {@code #tag} using the {@code ID_USED_CHANNELS}
     * key.
     *
     * @param player player looking at the part
     * @param part   part being looked at
     * @param te     host of the part
     * @param tag    transferred tag which is send to the client
     * @param world  world of the part
     * @param pos    pos of the part
     */
    @Override
    public void appendServerData(ServerPlayerEntity player, IPart part, TileEntity te, CompoundNBT tag, World world,
            BlockPos pos) {
        if (part instanceof IUsedChannelProvider) {
            IUsedChannelProvider usedChannelProvider = (IUsedChannelProvider) part;
            tag.putInt(ID_USED_CHANNELS, usedChannelProvider.getUsedChannelsInfo());
            tag.putInt(ID_MAX_CHANNELS, usedChannelProvider.getMaxChannelsInfo());
        }
    }

    private static class UsedChannelInfo {
        private final int usedChannels;
        private final int maxChannels;

        public UsedChannelInfo(int usedChannels, int maxChannels) {
            this.usedChannels = usedChannels;
            this.maxChannels = maxChannels;
        }
    }

}
