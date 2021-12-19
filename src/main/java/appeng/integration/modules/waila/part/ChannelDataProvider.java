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
package appeng.integration.modules.waila.part;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import appeng.api.parts.IPart;
import appeng.integration.modules.waila.WailaText;
import appeng.parts.networking.IUsedChannelProvider;

/**
 * Shows the used and maximum channel count for a part that implements {@link IUsedChannelProvider}.
 */
public final class ChannelDataProvider implements IPartDataProvider {
    private static final String TAG_MAX_CHANNELS = "maxChannels";
    private static final String TAG_USED_CHANNELS = "usedChannels";

    @Override
    public void appendBody(IPart part, CompoundTag partTag, List<Component> tooltip) {
        if (partTag.contains(TAG_MAX_CHANNELS, Tag.TAG_INT)) {
            var usedChannels = partTag.getInt(TAG_USED_CHANNELS);
            var maxChannels = partTag.getInt(TAG_MAX_CHANNELS);
            // Even in the maxChannels=0 case, we'll show as infinite
            if (maxChannels <= 0) {
                tooltip.add(WailaText.Channels.textComponent(usedChannels));
            } else {
                tooltip.add(WailaText.ChannelsOf.textComponent(usedChannels, maxChannels));
            }
        }
    }

    @Override
    public void appendServerData(ServerPlayer player, IPart part, CompoundTag partTag) {
        if (part instanceof IUsedChannelProvider usedChannelProvider) {
            partTag.putInt(TAG_USED_CHANNELS, usedChannelProvider.getUsedChannelsInfo());
            partTag.putInt(TAG_MAX_CHANNELS, usedChannelProvider.getMaxChannelsInfo());
        }
    }
}
