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


import appeng.core.AppEng;
import appeng.integration.modules.theoneprobe.tile.*;
import appeng.tile.AEBaseTile;
import com.google.common.collect.Lists;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.List;


public final class TileInfoProvider implements IProbeInfoProvider {
    private final List<ITileProbInfoProvider> providers;

    public TileInfoProvider() {
        final ITileProbInfoProvider charger = new ChargerInfoProvider();
        final ITileProbInfoProvider energyCell = new CraftingMonitorInfoProvider();
        final ITileProbInfoProvider craftingBlock = new PowerStateInfoProvider();
        final ITileProbInfoProvider craftingMonitor = new PowerStorageInfoProvider();

        this.providers = Lists.newArrayList(charger, energyCell, craftingBlock, craftingMonitor);
    }

    @Override
    public String getID() {
        return AppEng.MOD_ID + ":TileInfoProvider";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        final TileEntity tile = world.getTileEntity(data.getPos());

        if (tile instanceof AEBaseTile) {
            final AEBaseTile aeBaseTile = (AEBaseTile) tile;

            for (final ITileProbInfoProvider provider : this.providers) {
                provider.addProbeInfo(aeBaseTile, mode, probeInfo, player, world, blockState, data);
            }
        }
    }
}
