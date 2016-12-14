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


import java.util.LinkedList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;

import appeng.api.config.PowerUnits;
import appeng.api.parts.IPartModel;
import appeng.coremod.annotations.Integration.Interface;
import appeng.coremod.annotations.Integration.InterfaceList;
import appeng.integration.IntegrationType;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.util.Platform;


@InterfaceList( value = {
		@Interface( iface = "ic2.api.energy.tile.IEnergySink", iname = IntegrationType.IC2 ),
		@Interface( iface = "ic2.api.energy.tile.IEnergySource", iname = IntegrationType.IC2 )
} )
public class PartP2PIC2Power extends PartP2PTunnel<PartP2PIC2Power> implements IEnergySink, IEnergySource
{

	private static final String TAG_BUFFERED_ENERGY_1 = "bufferedEnergy1";
	private static final String TAG_BUFFERED_ENERGY_2 = "bufferedEnergy2";
	private static final String TAG_BUFFERED_VOLTAGE_1 = "outputPacket1";
	private static final String TAG_BUFFERED_VOLTAGE_2 = "outputPacket2";

	private static final P2PModels MODELS = new P2PModels( "part/p2p/p2p_tunnel_ic2" );

	@PartModels
	public static List<IPartModel> getModels()
	{
		return MODELS.getModels();
	}

	// Buffer the energy + voltage for two IC2 ENET packets
	private double bufferedEnergy1;
	private double bufferedVoltage1;
	private double bufferedEnergy2;
	private double bufferedVoltage2;

	public PartP2PIC2Power( ItemStack is )
	{
		super( is );
	}

	@Override
	public void readFromNBT( NBTTagCompound tag )
	{
		super.readFromNBT( tag );
		this.bufferedEnergy1 = tag.getDouble( TAG_BUFFERED_ENERGY_1 );
		this.bufferedEnergy2 = tag.getDouble( TAG_BUFFERED_ENERGY_2 );
		this.bufferedVoltage1 = tag.getDouble( TAG_BUFFERED_VOLTAGE_1 );
		this.bufferedVoltage2 = tag.getDouble( TAG_BUFFERED_VOLTAGE_2 );
	}

	@Override
	public void writeToNBT( NBTTagCompound tag )
	{
		super.writeToNBT( tag );
		tag.setDouble( TAG_BUFFERED_ENERGY_1, this.bufferedEnergy1 );
		tag.setDouble( TAG_BUFFERED_ENERGY_2, this.bufferedEnergy2 );
		tag.setDouble( TAG_BUFFERED_VOLTAGE_1, this.bufferedVoltage1 );
		tag.setDouble( TAG_BUFFERED_VOLTAGE_2, this.bufferedVoltage2 );
	}

	@Override
	public void onTunnelConfigChange()
	{
		this.getHost().partChanged();
	}

	@Override
	public void onTunnelNetworkChange()
	{
		this.getHost().notifyNeighbors();
	}

	@Override
	public boolean acceptsEnergyFrom( IEnergyEmitter emitter, EnumFacing direction )
	{
		return !this.isOutput() && direction == this.getSide().getFacing();
	}

	@Override
	public boolean emitsEnergyTo( IEnergyAcceptor receiver, EnumFacing direction )
	{
		return this.isOutput() && direction == this.getSide().getFacing();
	}

	@Override
	public double getDemandedEnergy()
	{
		if( this.isOutput() )
		{
			return 0;
		}

		try
		{
			for( PartP2PIC2Power t : this.getOutputs() )
			{
				if( t.bufferedEnergy1 <= 0.0001 || t.bufferedEnergy2 <= 0.0001 )
				{
					return 2048;
				}
			}
		}
		catch( GridAccessException e )
		{
			return 0;
		}

		return 0;
	}

	@Override
	public int getSinkTier()
	{
		return 4;
	}

	@Override
	public double injectEnergy( EnumFacing directionFrom, double amount, double voltage )
	{
		TunnelCollection<PartP2PIC2Power> outs;
		try
		{
			outs = this.getOutputs();
		}
		catch( GridAccessException e )
		{
			return amount;
		}

		if( outs.isEmpty() )
		{
			return amount;
		}

		LinkedList<PartP2PIC2Power> options = new LinkedList<>();
		for( PartP2PIC2Power o : outs )
		{
			if( o.bufferedEnergy1 <= 0.01 )
			{
				options.add( o );
			}
		}

		if( options.isEmpty() )
		{
			for( PartP2PIC2Power o : outs )
			{
				if( o.bufferedEnergy2 <= 0.01 )
				{
					options.add( o );
				}
			}
		}

		if( options.isEmpty() )
		{
			for( PartP2PIC2Power o : outs )
			{
				options.add( o );
			}
		}

		if( options.isEmpty() )
		{
			return amount;
		}

		PartP2PIC2Power x = Platform.pickRandom( options );

		if( x != null && x.bufferedEnergy1 <= 0.001 )
		{
			this.queueTunnelDrain( PowerUnits.EU, amount );
			x.bufferedEnergy1 = amount;
			x.bufferedVoltage1 = voltage;
			return 0;
		}

		if( x != null && x.bufferedEnergy2 <= 0.001 )
		{
			this.queueTunnelDrain( PowerUnits.EU, amount );
			x.bufferedEnergy2 = amount;
			x.bufferedVoltage2 = voltage;
			return 0;
		}

		return amount;
	}

	@Override
	public double getOfferedEnergy()
	{
		if( this.isOutput() )
		{
			return this.bufferedEnergy1;
		}
		return 0;
	}

	@Override
	public void drawEnergy( double amount )
	{
		this.bufferedEnergy1 -= amount;
		if( this.bufferedEnergy1 < 0.001 )
		{
			this.bufferedEnergy1 = this.bufferedEnergy2;
			this.bufferedEnergy2 = 0;

			this.bufferedVoltage1 = this.bufferedVoltage2;
			this.bufferedVoltage2 = 0;
		}
	}

	@Override
	public int getSourceTier()
	{
		// Sadly IC2 doesn't support changing the tier once we're registered, so we
		// go with the highest here... At this point it's somewhat unclear as to what effect
		// this realistically has.
		return 4;
	}

	@Override
	public IPartModel getStaticModels()
	{
		return MODELS.getModel( isPowered(), isActive() );
	}

}
