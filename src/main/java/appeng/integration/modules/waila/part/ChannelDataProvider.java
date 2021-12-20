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

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import appeng.api.networking.pathing.ControllerState;
import appeng.api.parts.IPart;
import appeng.integration.modules.waila.WailaText;
import appeng.me.service.PathingService;
import appeng.parts.networking.IUsedChannelProvider;

/**
 * Shows the used and maximum channel count for a part that implements {@link IUsedChannelProvider}.
 */
public final class ChannelDataProvider implements IPartDataProvider {
    private static final String TAG_MAX_CHANNELS = "maxChannels";
    private static final String TAG_USED_CHANNELS = "usedChannels";
    private static final String TAG_ERROR = "channelError";

    @Override
    public void appendBody(IPart part, CompoundTag partTag, List<Component> tooltip) {
        if (partTag.contains(TAG_ERROR, Tag.TAG_STRING)) {
            var error = ChannelError.valueOf(partTag.getString(TAG_ERROR));
            tooltip.add(error.text.textComponent().withStyle(ChatFormatting.RED));
            return;
        }

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
            var gridNode = part.getGridNode();
            if (gridNode != null) {
                var pathingService = (PathingService) gridNode.getGrid().getPathingService();
                if (pathingService.getControllerState() == ControllerState.NO_CONTROLLER) {
                    var adHocError = pathingService.getAdHocNetworkError();
                    if (adHocError != null) {
                        partTag.putString(TAG_ERROR, switch (adHocError) {
                            case NESTED_P2P_TUNNEL -> ChannelError.AD_HOC_NESTED_P2P_TUNNEL.name();
                            case TOO_MANY_CHANNELS -> ChannelError.AD_HOC_TOO_MANY_CHANNELS.name();
                        });
                        return;
                    }
                } else if (pathingService.getControllerState() == ControllerState.CONTROLLER_CONFLICT) {
                    partTag.putString(TAG_ERROR, ChannelError.CONTROLLER_CONFLICT.name());
                }
            }

            partTag.putInt(TAG_USED_CHANNELS, usedChannelProvider.getUsedChannelsInfo());
            partTag.putInt(TAG_MAX_CHANNELS, usedChannelProvider.getMaxChannelsInfo());
        }
    }

    enum ChannelError {
        AD_HOC_NESTED_P2P_TUNNEL(WailaText.ErrorNestedP2PTunnel),
        AD_HOC_TOO_MANY_CHANNELS(WailaText.ErrorTooManyChannels),
        CONTROLLER_CONFLICT(WailaText.ErrorControllerConflict);

        final WailaText text;

        ChannelError(WailaText text) {
            this.text = text;
        }
    }
}
