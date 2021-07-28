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

package appeng.integration.modules.theoneprobe;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;

import appeng.api.parts.IPart;
import appeng.core.AppEng;
import appeng.integration.modules.theoneprobe.part.ChannelInfoProvider;
import appeng.integration.modules.theoneprobe.part.IPartProbInfoProvider;
import appeng.integration.modules.theoneprobe.part.P2PStateInfoProvider;
import appeng.integration.modules.theoneprobe.part.PartAccessor;
import appeng.integration.modules.theoneprobe.part.PowerStateInfoProvider;
import appeng.integration.modules.theoneprobe.part.StorageMonitorInfoProvider;

public final class PartInfoProvider implements IProbeInfoProvider {
    private final List<IPartProbInfoProvider> providers;

    private final PartAccessor accessor = new PartAccessor();

    public PartInfoProvider() {
        final IPartProbInfoProvider channel = new ChannelInfoProvider();
        final IPartProbInfoProvider power = new PowerStateInfoProvider();
        final IPartProbInfoProvider storageMonitor = new StorageMonitorInfoProvider();
        final IPartProbInfoProvider p2p = new P2PStateInfoProvider();

        this.providers = Lists.newArrayList(channel, power, p2p, storageMonitor);
    }

    @Override
    public String getID() {
        return AppEng.MOD_ID + ":PartInfoProvider";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world,
            BlockState blockState, IProbeHitData data) {
        final BlockEntity te = world.getBlockEntity(data.getPos());
        final Optional<IPart> maybePart = this.accessor.getMaybePart(te, data);

        if (maybePart.isPresent()) {
            final IPart part = maybePart.get();

            for (final IPartProbInfoProvider provider : this.providers) {
                provider.addProbeInfo(part, mode, probeInfo, player, world, blockState, data);
            }
        }

    }
}
