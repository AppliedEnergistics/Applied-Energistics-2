/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.IEnergyStorage;

import appeng.api.config.PowerUnits;
import appeng.api.parts.IPartModel;
import appeng.capabilities.Capabilities;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;


public class PartP2PFEPower extends PartP2PTunnel<PartP2PFEPower>
{
	private static final P2PModels MODELS = new P2PModels( "part/p2p/p2p_tunnel_fe" );
	private static final IEnergyStorage NULL_ENERGY_STORAGE = new NullEnergyStorage();
	private final IEnergyStorage inputHandler = new InputEnergyStorage();

	public PartP2PFEPower( ItemStack is )
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

	private IEnergyStorage getOutput()
	{
		if( this.isOutput() )
		{
			final TileEntity self = this.getTile();
			final TileEntity te = self.getWorld().getTileEntity( self.getPos().offset( this.getSide().getFacing() ) );

			if( te != null && te.hasCapability( Capabilities.FORGE_ENERGY, this.getSide().getOpposite().getFacing() ) )
			{
				return te.getCapability( Capabilities.FORGE_ENERGY, this.getSide().getOpposite().getFacing() );
			}
		}
		return NULL_ENERGY_STORAGE;
	}

	@Override
	public boolean hasCapability( @Nonnull Capability<?> capability )
	{
		if( capability == Capabilities.FORGE_ENERGY )
		{
			return !this.isOutput();
		}
		return super.hasCapability( capability );
	}

	@Nullable
	@Override
	public <T> T getCapability( @Nonnull Capability<T> capability )
	{
		if( capability == Capabilities.FORGE_ENERGY )
		{
			if( !this.isOutput() )
			{
				return (T) this.inputHandler;
			}
			return (T) NULL_ENERGY_STORAGE;
		}
		return super.getCapability( capability );
	}

	private class InputEnergyStorage implements IEnergyStorage
	{
		@Override
		public int extractEnergy( int maxExtract, boolean simulate )
		{
			return 0;
		}

		@Override
		public int receiveEnergy( int maxReceive, boolean simulate )
		{
			if( PartP2PFEPower.this.isActive() )
			{
				int total = 0;

				try
				{
					final int outputTunnels = PartP2PFEPower.this.getOutputs().size();

					if( outputTunnels == 0 )
					{
						return 0;
					}

					final int amountPerOutput = maxReceive / outputTunnels;
					int overflow = maxReceive % amountPerOutput;

					for( PartP2PFEPower target : PartP2PFEPower.this.getOutputs() )
					{
						final IEnergyStorage output = target.getOutput();
						final int toSend = amountPerOutput + overflow;
						final int received = output.receiveEnergy( toSend, simulate );

						overflow = toSend - received;
						total += received;
					}

					PartP2PFEPower.this.queueTunnelDrain( PowerUnits.RF, total );
				}
				catch( GridAccessException ignored )
				{
				}

				return total;
			}

			return 0;
		}

		@Override
		public boolean canExtract()
		{
			return false;
		}

		@Override
		public boolean canReceive()
		{
			return true;
		}

		@Override
		public int getMaxEnergyStored()
		{
			if( !PartP2PFEPower.this.isActive() )
			{
				return 0;
			}

			int total = 0;

			try
			{
				for( PartP2PFEPower t : PartP2PFEPower.this.getOutputs() )
				{
					total += t.getOutput().getMaxEnergyStored();
				}
			}
			catch( GridAccessException e )
			{
				return 0;
			}

			return total;
		}

		@Override
		public int getEnergyStored()
		{
			if( !PartP2PFEPower.this.isActive() )
			{
				return 0;
			}

			int total = 0;

			try
			{
				for( PartP2PFEPower t : PartP2PFEPower.this.getOutputs() )
				{
					total += t.getOutput().getEnergyStored();
				}
			}
			catch( GridAccessException e )
			{
				return 0;
			}

			return total;
		}
	}

	private static class NullEnergyStorage implements IEnergyStorage
	{

		@Override
		public int receiveEnergy( int maxReceive, boolean simulate )
		{
			return 0;
		}

		@Override
		public int extractEnergy( int maxExtract, boolean simulate )
		{
			return 0;
		}

		@Override
		public int getEnergyStored()
		{
			return 0;
		}

		@Override
		public int getMaxEnergyStored()
		{
			return 0;
		}

		@Override
		public boolean canExtract()
		{
			return false;
		}

		@Override
		public boolean canReceive()
		{
			return false;
		}

	}
}
