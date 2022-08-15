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


import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;

import appeng.api.parts.IPart;
import appeng.integration.modules.theoneprobe.TheOneProbeText;
import appeng.parts.networking.PartCableSmart;
import appeng.parts.networking.PartDenseCableSmart;


public class ChannelInfoProvider implements IPartProbInfoProvider
{

	@Override
	public void addProbeInfo( IPart part, ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data )
	{
		if( !AEConfig.instance().isFeatureEnabled( AEFeature.CHANNELS ) )
		{
			return;
		}
		if( part instanceof PartDenseCableSmart || part instanceof PartCableSmart )
		{
			final int usedChannels;
			final int maxChannels = ( part instanceof PartDenseCableSmart ) ? 32 : 8;

			if( part.getGridNode().isActive() )
			{
				final NBTTagCompound tmp = new NBTTagCompound();
				part.writeToNBT( tmp );
				usedChannels = tmp.getByte( "usedChannels" );
			}
			else
			{
				usedChannels = 0;
			}

			final String formattedChannelString = String.format( TheOneProbeText.CHANNELS.getLocal(), usedChannels, maxChannels );

			probeInfo.text( formattedChannelString );
		}

	}

}
