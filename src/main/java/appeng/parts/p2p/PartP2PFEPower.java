/*
 *
 *  * This file is part of Applied Energistics 2.
 *  * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
 *  *
 *  * Applied Energistics 2 is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Lesser General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * Applied Energistics 2 is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 *
 */

package appeng.parts.p2p;


import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import appeng.api.config.PowerUnits;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.util.Platform;


/**
 * @author GuntherDW
 */
public class PartP2PFEPower extends PartP2PTunnel<PartP2PFEPower> implements IEnergyStorage
{
	private static final P2PModels MODELS = new P2PModels( "part/p2p/p2p_tunnel_fe" );

	private boolean cachedTarget = false;

	private IEnergyStorage outputTarget;

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

	public PartP2PFEPower( ItemStack is )
	{
		super( is );
	}

	@Override
	public void onTunnelNetworkChange()
	{
		this.getHost().notifyNeighbors();
	}

	@Override
	public void onNeighborChanged()
	{
		super.onNeighborChanged();

		this.cachedTarget = false;
	}

	@Override
	public int receiveEnergy( int maxReceive, boolean simulate )
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
				for( PartP2PFEPower t : this.getOutputs() )
				{
					if( Platform.getRandomInt() % 2 > 0 )
					{
						int receiver = t.getOutput().receiveEnergy( maxReceive, simulate );
						maxReceive -= receiver;
						total += receiver;

						if( maxReceive <= 0 )
						{
							break;
						}
					}
				}

				if( maxReceive > 0 )
				{
					for( PartP2PFEPower t : this.getOutputs() )
					{
						int receiver = t.getOutput().receiveEnergy( maxReceive, simulate );
						maxReceive -= receiver;
						total += receiver;

						if( maxReceive <= 0 )
						{
							break;
						}
					}
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

	@Override
	public int extractEnergy( int maxExtract, boolean simulate )
	{
		return 0;
	}

	private IEnergyStorage getOutput()
	{
		if( this.isOutput() )
		{
			if( !this.cachedTarget )
			{
				TileEntity self = this.getTile();
				TileEntity te = self.getWorld().getTileEntity( new BlockPos( self.getPos().getX() + this.getSide().xOffset, self.getPos()
						.getY() + this.getSide().yOffset, self.getPos().getZ() + this.getSide().zOffset ) );
				this.outputTarget = te.hasCapability( CapabilityEnergy.ENERGY, this.getSide().getOpposite().getFacing() ) ? te
						.getCapability( CapabilityEnergy.ENERGY, this.getSide().getOpposite().getFacing() ) : null;
				this.cachedTarget = true;
			}

			if( this.outputTarget == null || !this.outputTarget.canReceive() )
			{
				return null;
			}

			return this.outputTarget;
		}
		return null;
	}

	@Override
	public int getEnergyStored()
	{
		if( this.isOutput() || !this.isActive() )
		{
			return 0;
		}

		int total = 0;

		try
		{
			for( PartP2PFEPower t : this.getOutputs() )
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

	@Override
	public int getMaxEnergyStored()
	{
		if( this.isOutput() || !this.isActive() )
		{
			return 0;
		}

		int total = 0;

		try
		{
			for( PartP2PFEPower t : this.getOutputs() )
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
	public boolean hasCapability( @Nonnull Capability<?> capability )
	{
		if( capability == CapabilityEnergy.ENERGY )
		{
			return true;
		}

		return super.hasCapability( capability );
	}

	@Nullable
	@Override
	public <T> T getCapability( @Nonnull Capability<T> capability )
	{
		if( capability == CapabilityEnergy.ENERGY )
		{
			return (T) this;
		}

		return super.getCapability( capability );
	}
}
