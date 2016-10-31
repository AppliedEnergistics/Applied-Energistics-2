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
import net.minecraft.util.ResourceLocation;

import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;

import appeng.api.config.PowerUnits;
import appeng.integration.IntegrationType;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.transformer.annotations.Integration.Interface;
import appeng.transformer.annotations.Integration.InterfaceList;
import appeng.util.Platform;


@InterfaceList( value = {
		@Interface( iface = "ic2.api.energy.tile.IEnergySink", iname = IntegrationType.IC2 ),
		@Interface( iface = "ic2.api.energy.tile.IEnergySource", iname = IntegrationType.IC2 )
} )
public class PartP2PIC2Power extends PartP2PTunnel<PartP2PIC2Power> implements IEnergySink, IEnergySource
{

	private static final P2PModels MODELS = new P2PModels( "part/p2p/p2p_tunnel_ic2" );

	@PartModels
	public static List<ResourceLocation> getModels()
	{
		return MODELS.getModels();
	}

	// two packet buffering...
	private double OutputEnergyA;
	private double OutputEnergyB;
	// two packet buffering...
	private double OutputVoltageA;
	private double OutputVoltageB;

	public PartP2PIC2Power( ItemStack is )
	{
		super( is );
	}

	@Override
	public void readFromNBT( NBTTagCompound tag )
	{
		super.readFromNBT( tag );
		this.OutputEnergyA = tag.getDouble( "OutputPacket" );
		this.OutputEnergyB = tag.getDouble( "OutputPacket2" );
		this.OutputVoltageA = tag.getDouble( "OutputVoltageA" );
		this.OutputVoltageB = tag.getDouble( "OutputVoltageB" );
	}

	@Override
	public void writeToNBT( NBTTagCompound tag )
	{
		super.writeToNBT( tag );
		tag.setDouble( "OutputPacket", this.OutputEnergyA );
		tag.setDouble( "OutputPacket2", this.OutputEnergyB );
		tag.setDouble( "OutputVoltageA", this.OutputVoltageA );
		tag.setDouble( "OutputVoltageB", this.OutputVoltageB );
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
				if( t.OutputEnergyA <= 0.0001 || t.OutputEnergyB <= 0.0001 )
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
			if( o.OutputEnergyA <= 0.01 )
			{
				options.add( o );
			}
		}

		if( options.isEmpty() )
		{
			for( PartP2PIC2Power o : outs )
			{
				if( o.OutputEnergyB <= 0.01 )
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

		if( x != null && x.OutputEnergyA <= 0.001 )
		{
			this.queueTunnelDrain( PowerUnits.EU, amount );
			x.OutputEnergyA = amount;
			x.OutputVoltageA = voltage;
			return 0;
		}

		if( x != null && x.OutputEnergyB <= 0.001 )
		{
			this.queueTunnelDrain( PowerUnits.EU, amount );
			x.OutputEnergyB = amount;
			x.OutputVoltageB = voltage;
			return 0;
		}

		return amount;
	}

	@Override
	public double getOfferedEnergy()
	{
		if( this.isOutput() )
		{
			return this.OutputEnergyA;
		}
		return 0;
	}

	@Override
	public void drawEnergy( double amount )
	{
		this.OutputEnergyA -= amount;
		if( this.OutputEnergyA < 0.001 )
		{
			this.OutputEnergyA = this.OutputEnergyB;
			this.OutputEnergyB = 0;

			this.OutputVoltageA = this.OutputVoltageB;
			this.OutputVoltageB = 0;
		}
	}

	@Override
	public int getSourceTier()
	{
		if( this.isOutput() )
		{
			return this.calculateTierFromVoltage( this.OutputVoltageA );
		}
		return 4;
	}

	private int calculateTierFromVoltage( double voltage )
	{
		return ic2.api.energy.EnergyNet.instance.getTierFromPower( voltage );
	}

	@Override
	public List<ResourceLocation> getStaticModels()
	{
		return MODELS.getModel( isPowered(), isActive() );
	}

}
