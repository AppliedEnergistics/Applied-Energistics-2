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

package appeng.integration.modules.theoneprobe.tile;


import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;

import appeng.api.implementations.IPowerChannelState;
import appeng.integration.modules.theoneprobe.TheOneProbeText;
import appeng.tile.AEBaseTile;


public class PowerStateInfoProvider implements ITileProbInfoProvider
{

	@Override
	public void addProbeInfo( AEBaseTile tile, ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data )
	{
		if( tile instanceof IPowerChannelState )
		{
			final IPowerChannelState state = (IPowerChannelState) tile;

			final boolean isActive = state.isActive();
			final boolean isPowered = state.isPowered();

			if( isActive && isPowered )
			{
				probeInfo.text( TheOneProbeText.DEVICE_ONLINE.getLocal() );
			}
			else if( isPowered )
			{
				probeInfo.text( TheOneProbeText.DEVICE_MISSING_CHANNEL.getLocal() );
			}
			else
			{
				probeInfo.text( TheOneProbeText.DEVICE_OFFLINE.getLocal() );
			}
		}

	}

}
