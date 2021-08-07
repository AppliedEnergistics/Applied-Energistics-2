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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.parts.IPart;
import appeng.integration.modules.theoneprobe.TheOneProbeText;

public class PowerStateInfoProvider implements IPartProbInfoProvider {

    @Override
    public void addProbeInfo(IPart part, ProbeMode mode, IProbeInfo probeInfo, Player player, Level level,
            BlockState blockState, IProbeHitData data) {
        if (part instanceof IPowerChannelState state) {
            final Component tooltip = this.getToolTip(state.isActive(), state.isPowered());

            probeInfo.text(tooltip);
        }

    }

    private Component getToolTip(final boolean isActive, final boolean isPowered) {
        final Component result;

        if (isActive && isPowered) {
            result = TheOneProbeText.DEVICE_ONLINE.getTranslationComponent();
        } else if (isPowered) {
            result = TheOneProbeText.DEVICE_MISSING_CHANNEL.getTranslationComponent();
        } else {
            result = TheOneProbeText.DEVICE_OFFLINE.getTranslationComponent();
        }

        return result;
    }

}
