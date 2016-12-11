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


import com.google.common.collect.Iterators;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;

import appeng.api.parts.IPart;
import appeng.integration.modules.theoneprobe.TheOneProbeText;
import appeng.me.GridAccessException;
import appeng.parts.p2p.PartP2PTunnel;


public class P2PStateInfoProvider implements IPartProbInfoProvider
{

	private static final int STATE_UNLINKED = 0;
	private static final int STATE_OUTPUT = 1;
	private static final int STATE_INPUT = 2;
	public static final String TAG_P2P_STATE = "p2p_state";

	@Override
	public void addProbeInfo( IPart part, ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data )
	{
		if( part instanceof PartP2PTunnel )
		{
			final PartP2PTunnel tunnel = (PartP2PTunnel) part;

			// The default state
			int state = STATE_UNLINKED;
			int outputCount = 0;

			if( !tunnel.isOutput() )
			{
				outputCount = getOutputCount( tunnel );
				if( outputCount > 0 )
				{
					// Only set it to INPUT if we know there are any outputs
					state = STATE_INPUT;
				}
			}
			else
			{
				final PartP2PTunnel input = tunnel.getInput();
				if( input != null )
				{
					state = STATE_OUTPUT;
				}
			}

			switch( state )
			{
				case STATE_UNLINKED:
					probeInfo.text( TheOneProbeText.P2P_UNLINKED.getLocal() );
					break;
				case STATE_OUTPUT:
					probeInfo.text( TheOneProbeText.P2P_OUTPUT.getLocal() );
					break;
				case STATE_INPUT:
					probeInfo.text( getOutputText( outputCount ) );
					break;
			}
		}
	}

	private static int getOutputCount( PartP2PTunnel tunnel )
	{
		try
		{
			return Iterators.size( tunnel.getOutputs().iterator() );
		}
		catch( GridAccessException e )
		{
			// Well... unknown size it is!
			return 0;
		}
	}

	private static String getOutputText( int outputs )
	{
		if( outputs <= 1 )
		{
			return TheOneProbeText.P2P_INPUT_ONE_OUTPUT.getLocal();
		}
		else
		{
			return String.format( TheOneProbeText.P2P_INPUT_MANY_OUTPUTS.getLocal(), outputs );
		}
	}

}
