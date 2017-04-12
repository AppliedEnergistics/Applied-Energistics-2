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


import appeng.api.config.PowerUnits;
import appeng.integration.IntegrationType;
import appeng.me.GridAccessException;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.transformer.annotations.Integration.Interface;
import appeng.transformer.annotations.Integration.InterfaceList;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.LinkedList;


@InterfaceList( value = { @Interface( iface = "ic2.api.energy.tile.IEnergySink", iname = IntegrationType.IC2 ), @Interface( iface = "ic2.api.energy.tile.IEnergySource", iname = IntegrationType.IC2 ) } )
public class PartP2PIC2Power extends PartP2PTunnel<PartP2PIC2Power> implements ic2.api.energy.tile.IEnergySink, ic2.api.energy.tile.IEnergySource
{

	// two packet buffering...
	private double OutputEnergyA;
	private double OutputEnergyB;
	// two packet buffering...
	private double OutputVoltageA;
	private double OutputVoltageB;

	public PartP2PIC2Power( final ItemStack is )
	{
		super( is );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public IIcon getTypeTexture()
	{
		return Blocks.diamond_block.getBlockTextureFromSide( 0 );
	}

	@Override
	public void readFromNBT( final NBTTagCompound tag )
	{
		super.readFromNBT( tag );
		this.OutputEnergyA = tag.getDouble( "OutputPacket" );
		this.OutputEnergyB = tag.getDouble( "OutputPacket2" );
		this.OutputVoltageA = tag.getDouble( "OutputVoltageA" );
		this.OutputVoltageB = tag.getDouble( "OutputVoltageB" );
	}

	@Override
	public void writeToNBT( final NBTTagCompound tag )
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
	public boolean acceptsEnergyFrom( final TileEntity emitter, final ForgeDirection direction )
	{
		if( !this.isOutput() )
		{
			return direction == this.getSide();
		}
		return false;
	}

	@Override
	public boolean emitsEnergyTo( final TileEntity receiver, final ForgeDirection direction )
	{
		if( this.isOutput() )
		{
			return direction == this.getSide();
		}
		return false;
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
			for( final PartP2PIC2Power t : this.getOutputs() )
			{
				if( t.OutputEnergyA <= 0.0001 || t.OutputEnergyB <= 0.0001 )
				{
					return 2048;
				}
			}
		}
		catch( final GridAccessException e )
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
	public double injectEnergy( final ForgeDirection directionFrom, final double amount, final double voltage )
	{
		final TunnelCollection<PartP2PIC2Power> outs;
		try
		{
			outs = this.getOutputs();
		}
		catch( final GridAccessException e )
		{
			return amount;
		}

		if( outs.isEmpty() )
		{
			return amount;
		}

		final LinkedList<PartP2PIC2Power> options = new LinkedList<PartP2PIC2Power>();
		for( final PartP2PIC2Power o : outs )
		{
			if( o.OutputEnergyA <= 0.01 )
			{
				options.add( o );
			}
		}

		if( options.isEmpty() )
		{
			for( final PartP2PIC2Power o : outs )
			{
				if( o.OutputEnergyB <= 0.01 )
				{
					options.add( o );
				}
			}
		}

		if( options.isEmpty() )
		{
			for( final PartP2PIC2Power o : outs )
			{
				options.add( o );
			}
		}

		if( options.isEmpty() )
		{
			return amount;
		}

		final PartP2PIC2Power x = Platform.pickRandom( options );

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

	public float getPowerDrainPerTick()
	{
		return 0.5f;
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
	public void drawEnergy( final double amount )
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

	private int calculateTierFromVoltage( final double voltage )
	{
		return ic2.api.energy.EnergyNet.instance.getTierFromPower( voltage );
	}
}
