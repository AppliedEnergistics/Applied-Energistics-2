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

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import mcjty.theoneprobe.api.IProbeConfig;
import mcjty.theoneprobe.api.IProbeConfigProvider;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeHitEntityData;

import appeng.tile.AEBaseBlockEntity;

public class AEConfigProvider implements IProbeConfigProvider {

    @Override
    public void getProbeConfig(IProbeConfig config, Player player, Level world, Entity entity,
            IProbeHitEntityData data) {
        // Still no AE entities.
    }

    @Override
    public void getProbeConfig(IProbeConfig config, Player player, Level world, BlockState blockState,
            IProbeHitData data) {
        if (world.getBlockEntity(data.getPos()) instanceof AEBaseBlockEntity) {
            config.setRFMode(0);
        }
    }

}
