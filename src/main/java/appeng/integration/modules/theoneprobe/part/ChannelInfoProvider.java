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

package appeng.integration.modules.theoneprobe.part;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;

import appeng.api.parts.IPart;
import appeng.integration.modules.theoneprobe.TheOneProbeText;
import appeng.parts.networking.IUsedChannelProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class ChannelInfoProvider implements IPartProbInfoProvider {

    @Override
    public void addProbeInfo(IPart part, ProbeMode mode, IProbeInfo probeInfo, Player player, Level world,
                             BlockState blockState, IProbeHitData data) {
        if (part instanceof IUsedChannelProvider) {
            IUsedChannelProvider usedChannelProvider = (IUsedChannelProvider) part;

            final int usedChannels = usedChannelProvider.getUsedChannelsInfo();
            final int maxChannels = usedChannelProvider.getMaxChannelsInfo();

            final Component formattedChannelString = TheOneProbeText.CHANNELS.getTranslationComponent(usedChannels,
                    maxChannels);

            probeInfo.text(formattedChannelString);
        }

    }

}
