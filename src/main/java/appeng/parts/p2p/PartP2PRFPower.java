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

package appeng.parts.p2p;


import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import cofh.redstoneflux.api.IEnergyReceiver;

import appeng.api.config.PowerUnits;
import appeng.api.parts.IPartModel;
import appeng.coremod.annotations.Integration.Interface;
import appeng.coremod.annotations.Integration.InterfaceList;
import appeng.integration.IntegrationType;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;


@InterfaceList( value = { @Interface( iface = "cofh.redstoneflux.api.IEnergyReceiver", iname = IntegrationType.RF ) } )
public final class PartP2PRFPower extends PartP2PTunnel<PartP2PRFPower> implements IEnergyReceiver
{
	private static final P2PModels MODELS = new P2PModels( "part/p2p/p2p_tunnel_rf" );
	private static final IEnergyReceiver NULL_ENERGY_RECEIVER = new NullEnergyReceiver();

	private boolean cachedTarget = false;

	private IEnergyReceiver outputTarget;

	public PartP2PRFPower( ItemStack is )
	{
		super( is );
	}

	@PartModels
	public static List<IPartModel> getModels()
	{
		return MODELS.getModels();
	}

	@Override
	public IPartModel getStaticModels()
	{
		return MODELS.getModel( this.isPowered(), this.isActive() );
	}

	@Override
	public void onTunnelNetworkChange()
	{
		this.getHost().notifyNeighbors();
	}

	@Override
	public void onNeighborChanged( IBlockAccess w, BlockPos pos, BlockPos neighbor )
	{
		super.onNeighborChanged( w, pos, neighbor );

		this.cachedTarget = false;
	}

	@Override
	public int receiveEnergy( EnumFacing from, int maxReceive, boolean simulate )
	{
		if( this.isOutput() )
		{
			return 0;
		}

		if( this.isActive() )
		{
			int total = 0;

			try
			{
				final int outputTunnels = this.getOutputs().size();

				if( outputTunnels == 0 )
				{
					return 0;
				}

				final int amountPerOutput = maxReceive / outputTunnels;
				int overflow = maxReceive % amountPerOutput;

				for( PartP2PRFPower target : this.getOutputs() )
				{
					final IEnergyReceiver output = target.getOutput();
					final int toSend = amountPerOutput + overflow;
					final int received = output.receiveEnergy( target.getSide().getFacing().getOpposite(), toSend, simulate );

					overflow = toSend - received;
					total += received;
				}

				this.queueTunnelDrain( PowerUnits.RF, total );
			}
			catch( GridAccessException ignored )
			{
			}

			return total;
		}

		return 0;
	}

	private IEnergyReceiver getOutput()
	{
		if( this.isOutput() )
		{
			if( !this.cachedTarget )
			{
				final TileEntity self = this.getTile();
				final TileEntity te = self.getWorld().getTileEntity( new BlockPos( self.getPos().getX() + this.getSide().xOffset, self.getPos()
						.getY() + this.getSide().yOffset, self.getPos().getZ() + this.getSide().zOffset ) );

				this.outputTarget = te instanceof IEnergyReceiver ? (IEnergyReceiver) te : null;
				this.cachedTarget = true;
			}

			if( this.outputTarget != null && this.outputTarget.canConnectEnergy( this.getSide().getOpposite().getFacing() ) )
			{
				return this.outputTarget;
			}
		}

		return NULL_ENERGY_RECEIVER;
	}

	@Override
	public int getEnergyStored( EnumFacing from )
	{
		if( this.isOutput() || !this.isActive() )
		{
			return 0;
		}

		int total = 0;

		try
		{
			for( PartP2PRFPower t : this.getOutputs() )
			{
				total += t.getOutput().getEnergyStored( this.getSide().getOpposite().getFacing() );
			}
		}
		catch( GridAccessException e )
		{
			return 0;
		}

		return total;
	}

	@Override
	public int getMaxEnergyStored( EnumFacing from )
	{
		if( this.isOutput() || !this.isActive() )
		{
			return 0;
		}

		int total = 0;

		try
		{
			for( PartP2PRFPower t : this.getOutputs() )
			{
				total += t.getOutput().getMaxEnergyStored( this.getSide().getOpposite().getFacing() );
			}
		}
		catch( GridAccessException e )
		{
			return 0;
		}

		return total;
	}

	@Override
	public boolean canConnectEnergy( EnumFacing from )
	{
		return true;
	}

	private static class NullEnergyReceiver implements IEnergyReceiver
	{

		@Override
		public int getEnergyStored( EnumFacing from )
		{
			return 0;
		}

		@Override
		public int getMaxEnergyStored( EnumFacing from )
		{
			return 0;
		}

		@Override
		public boolean canConnectEnergy( EnumFacing from )
		{
			return true;
		}

		@Override
		public int receiveEnergy( EnumFacing from, int maxReceive, boolean simulate )
		{
			return 0;
		}

	}

}
