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

package appeng.integration.modules.theoneprobe.config;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import mcjty.theoneprobe.api.IProbeConfig;
import mcjty.theoneprobe.api.IProbeConfigProvider;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeHitEntityData;

import appeng.tile.AEBaseTileEntity;

public class AEConfigProvider implements IProbeConfigProvider {

    @Override
    public void getProbeConfig(IProbeConfig config, PlayerEntity player, World world, Entity entity,
            IProbeHitEntityData data) {
        // Still no AE entities.
    }

    @Override
    public void getProbeConfig(IProbeConfig config, PlayerEntity player, World world, BlockState blockState,
            IProbeHitData data) {
        if (world.getBlockEntity(data.getPos()) instanceof AEBaseTileEntity) {
            config.setRFMode(0);
        }
    }

}
