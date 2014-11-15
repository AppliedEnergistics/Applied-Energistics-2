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

import java.util.Stack;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.PowerUnits;
import appeng.api.config.TunnelType;
import appeng.core.AppEng;
import appeng.integration.IntegrationType;
import appeng.integration.modules.helpers.NullRFHandler;
import appeng.me.GridAccessException;
import appeng.transformer.annotations.integration.Interface;
import appeng.transformer.annotations.integration.InterfaceList;
import appeng.util.Platform;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@InterfaceList(value = { @Interface(iface = "cofh.api.energy.IEnergyHandler", iname = "RF") })
public class PartP2PRFPower extends PartP2PTunnel<PartP2PRFPower> implements cofh.api.energy.IEnergyHandler
{

	private static final IEnergyHandler myNullHandler = new NullRFHandler();

	boolean cachedTarget = false;
	IEnergyHandler outputTarget;

	@Override
	public TunnelType getTunnelType()
	{
		return TunnelType.RF_POWER;
	}

	public PartP2PRFPower(ItemStack is) {
		super( is );

		if ( !AppEng.instance.isIntegrationEnabled( IntegrationType.RF ) )
			throw new RuntimeException( "RF Not installed!" );
	}

	@Override
	public void onNeighborChanged()
	{
		super.onNeighborChanged();
		cachedTarget = false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getTypeTexture()
	{
		return Blocks.iron_block.getBlockTextureFromSide( 0 );
	}

	@Override
	public void onTunnelNetworkChange()
	{
		getHost().notifyNeighbors();
	}

	public float getPowerDrainPerTick()
	{
		return 0.5f;
	}

	static final ThreadLocal<Stack<PartP2PRFPower>> depth = new ThreadLocal<Stack<PartP2PRFPower>>();

	private Stack<PartP2PRFPower> getDepth()
	{
		Stack<PartP2PRFPower> s = depth.get();

		if ( s == null )
			depth.set( s = new Stack<PartP2PRFPower>() );

		return s;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if ( output )
			return 0;

		if ( isActive() )
		{
			Stack<PartP2PRFPower> stack = getDepth();

			for (PartP2PRFPower t : stack)
				if ( t == this )
					return 0;

			stack.push( this );

			int total = 0;

			try
			{
				for (PartP2PRFPower t : getOutputs())
				{
					if ( Platform.getRandomInt() % 2 > 0 )
					{
						int receiver = t.getOutput().receiveEnergy( t.side.getOpposite(), maxReceive, simulate );
						maxReceive -= receiver;
						total += receiver;

						if ( maxReceive <= 0 )
							break;
					}
				}

				if ( maxReceive > 0 )
				{
					for (PartP2PRFPower t : getOutputs())
					{
						int receiver = t.getOutput().receiveEnergy( t.side.getOpposite(), maxReceive, simulate );
						maxReceive -= receiver;
						total += receiver;

						if ( maxReceive <= 0 )
							break;
					}
				}

				QueueTunnelDrain( PowerUnits.RF, total );
			}
			catch (GridAccessException ignored)
			{
			}

			if ( stack.pop() != this )
				throw new RuntimeException( "Invalid Recursion detected." );

			return total;
		}

		return 0;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		return 0;
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		if ( output || !isActive() )
			return 0;

		int total = 0;

		Stack<PartP2PRFPower> stack = getDepth();

		for (PartP2PRFPower t : stack)
			if ( t == this )
				return 0;

		stack.push( this );

		try
		{
			for (PartP2PRFPower t : getOutputs())
			{
				total += t.getOutput().getEnergyStored( t.side.getOpposite() );
			}
		}
		catch (GridAccessException e)
		{
			return 0;
		}

		if ( stack.pop() != this )
			throw new RuntimeException( "Invalid Recursion detected." );

		return total;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		if ( output || !isActive() )
			return 0;

		int total = 0;

		Stack<PartP2PRFPower> stack = getDepth();

		for (PartP2PRFPower t : stack)
			if ( t == this )
				return 0;

		stack.push( this );

		try
		{
			for (PartP2PRFPower t : getOutputs())
			{
				total += t.getOutput().getMaxEnergyStored( t.side.getOpposite() );
			}
		}
		catch (GridAccessException e)
		{
			return 0;
		}

		if ( stack.pop() != this )
			throw new RuntimeException( "Invalid Recursion detected." );

		return total;
	}

	private IEnergyHandler getOutput()
	{
		if ( output )
		{
			if ( !cachedTarget )
			{
				TileEntity self = getTile();
				TileEntity te = self.getWorldObj().getTileEntity( self.xCoord + side.offsetX, self.yCoord + side.offsetY, self.zCoord + side.offsetZ );
				outputTarget = te instanceof IEnergyHandler ? (IEnergyHandler) te : null;
				cachedTarget = true;
			}

			if ( outputTarget == null || !outputTarget.canConnectEnergy( side.getOpposite() ) )
				return myNullHandler;

			return outputTarget;
		}
		return myNullHandler;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return true;
	}
}
