/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.config.PowerUnits;
import appeng.api.config.TunnelType;
import appeng.core.AppEng;
import appeng.integration.IntegrationType;
import appeng.me.GridAccessException;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.transformer.annotations.integration.Interface;
import appeng.transformer.annotations.integration.InterfaceList;
import appeng.util.Platform;

@InterfaceList(value = { @Interface(iface = "ic2.api.energy.tile.IEnergySink", iname = "IC2"),
		@Interface(iface = "ic2.api.energy.tile.IEnergySource", iname = "IC2") })
public class PartP2PIC2Power extends PartP2PTunnel<PartP2PIC2Power> implements ic2.api.energy.tile.IEnergySink, ic2.api.energy.tile.IEnergySource
{

	@Override
	public TunnelType getTunnelType()
	{
		return TunnelType.IC2_POWER;
	}

	public PartP2PIC2Power(ItemStack is) {
		super( is );

		if ( !AppEng.instance.isIntegrationEnabled( IntegrationType.IC2 ) )
			throw new RuntimeException( "IC2 Not installed!" );
	}

	// two packet buffering...
	double OutputEnergyA;
	double OutputEnergyB;

	// two packet buffering...
	double OutputVoltageA;
	double OutputVoltageB;

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT( tag );
		tag.setDouble( "OutputPacket", this.OutputEnergyA );
		tag.setDouble( "OutputPacket2", this.OutputEnergyB );
		tag.setDouble( "OutputVoltageA", this.OutputVoltageA );
		tag.setDouble( "OutputVoltageB", this.OutputVoltageB );
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT( tag );
		this.OutputEnergyA = tag.getDouble( "OutputPacket" );
		this.OutputEnergyB = tag.getDouble( "OutputPacket2" );
		this.OutputVoltageA = tag.getDouble( "OutputVoltageA" );
		this.OutputVoltageB = tag.getDouble( "OutputVoltageB" );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getTypeTexture()
	{
		return Blocks.diamond_block.getBlockTextureFromSide( 0 );
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		if ( !this.output )
			return direction.equals( this.side );
		return false;
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction)
	{
		if ( this.output )
			return direction.equals( this.side );
		return false;
	}

	@Override
	public double getDemandedEnergy()
	{
		if ( this.output )
			return 0;

		try
		{
			for (PartP2PIC2Power t : this.getOutputs())
			{
				if ( t.OutputEnergyA <= 0.0001 || t.OutputEnergyB <= 0.0001 )
				{
					return 2048;
				}
			}
		}
		catch (GridAccessException e)
		{
			return 0;
		}

		return 0;
	}

	@Override
	public void onTunnelNetworkChange()
	{
		this.getHost().notifyNeighbors();
	}

	@Override
	public void onTunnelConfigChange()
	{
		this.getHost().partChanged();
	}

	public float getPowerDrainPerTick()
	{
		return 0.5f;
	}

	@Override
	public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage)
	{
		TunnelCollection<PartP2PIC2Power> outs;
		try
		{
			outs = this.getOutputs();
		}
		catch (GridAccessException e)
		{
			return amount;
		}

		if ( outs.isEmpty() )
			return amount;

		LinkedList<PartP2PIC2Power> Options = new LinkedList<PartP2PIC2Power>();
		for (PartP2PIC2Power o : outs)
		{
			if ( o.OutputEnergyA <= 0.01 )
				Options.add( o );
		}

		if ( Options.isEmpty() )
		{
			for (PartP2PIC2Power o : outs)
				if ( o.OutputEnergyB <= 0.01 )
					Options.add( o );
		}

		if ( Options.isEmpty() )
		{
			for (PartP2PIC2Power o : outs)
				Options.add( o );
		}

		if ( Options.isEmpty() )
			return amount;

		PartP2PIC2Power x = Platform.pickRandom( Options );

		if ( x != null && x.OutputEnergyA <= 0.001 )
		{
			this.QueueTunnelDrain( PowerUnits.EU, amount );
			x.OutputEnergyA = amount;
			x.OutputVoltageA = voltage;
			return 0;
		}

		if ( x != null && x.OutputEnergyB <= 0.001 )
		{
			this.QueueTunnelDrain( PowerUnits.EU, amount );
			x.OutputEnergyB = amount;
			x.OutputVoltageB = voltage;
			return 0;
		}

		return amount;
	}

	@Override
	public int getSinkTier()
	{
		return 4;
	}

	@Override
	public double getOfferedEnergy()
	{
		if ( this.output )
			return this.OutputEnergyA;
		return 0;
	}

	@Override
	public void drawEnergy(double amount)
	{
		this.OutputEnergyA -= amount;
		if ( this.OutputEnergyA < 0.001 )
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
		if ( this.output )
			return this.calculateTierFromVoltage( this.OutputVoltageA );
		return 4;
	}

	private int calculateTierFromVoltage(double voltage)
	{
		return ic2.api.energy.EnergyNet.instance.getTierFromPower( voltage );
	}

}
