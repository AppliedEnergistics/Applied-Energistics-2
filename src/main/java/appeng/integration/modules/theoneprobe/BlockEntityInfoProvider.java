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

import com.google.common.collect.Lists;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;

import appeng.core.AppEng;
import appeng.integration.modules.theoneprobe.blockentity.ChargerInfoProvider;
import appeng.integration.modules.theoneprobe.blockentity.CraftingMonitorInfoProvider;
import appeng.integration.modules.theoneprobe.blockentity.IBlockEntityInfoProvider;
import appeng.integration.modules.theoneprobe.blockentity.PowerStateInfoProvider;
import appeng.integration.modules.theoneprobe.blockentity.PowerStorageInfoProvider;
import appeng.blockentity.AEBaseBlockEntity;

public final class BlockEntityInfoProvider implements IProbeInfoProvider {
    private final List<IBlockEntityInfoProvider> providers;

    public BlockEntityInfoProvider() {
        final IBlockEntityInfoProvider charger = new ChargerInfoProvider();
        final IBlockEntityInfoProvider energyCell = new CraftingMonitorInfoProvider();
        final IBlockEntityInfoProvider craftingBlock = new PowerStateInfoProvider();
        final IBlockEntityInfoProvider craftingMonitor = new PowerStorageInfoProvider();

        this.providers = Lists.newArrayList(charger, energyCell, craftingBlock, craftingMonitor);
    }

    @Override
    public String getID() {
        return AppEng.MOD_ID + ":BlockEntityInfoProvider";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world,
            BlockState blockState, IProbeHitData data) {
        final BlockEntity tile = world.getBlockEntity(data.getPos());

        if (tile instanceof AEBaseBlockEntity) {
            final AEBaseBlockEntity aeBaseTile = (AEBaseBlockEntity) tile;

            for (final IBlockEntityInfoProvider provider : this.providers) {
                provider.addProbeInfo(aeBaseTile, mode, probeInfo, player, world, blockState, data);
            }
        }
    }
}
