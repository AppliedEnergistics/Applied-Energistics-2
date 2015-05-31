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


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.me.GridAccessException;


public final class PartP2PLiquids extends PartP2PTunnel<PartP2PLiquids> implements IFluidHandler
{

	static final ThreadLocal<Stack<PartP2PLiquids>> DEPTH = new ThreadLocal<Stack<PartP2PLiquids>>();
	private static final FluidTankInfo[] ACTIVE_TANK = new FluidTankInfo[] { new FluidTankInfo( null, 10000 ) };
	private static final FluidTankInfo[] INACTIVE_TANK = new FluidTankInfo[] { new FluidTankInfo( null, 0 ) };
	IFluidHandler cachedTank;
	private int tmpUsed;

	public PartP2PLiquids( ItemStack is )
	{
		super( is );
	}

	public float getPowerDrainPerTick()
	{
		return 2.0f;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public IIcon getTypeTexture()
	{
		return Blocks.lapis_block.getBlockTextureFromSide( 0 );
	}

	@Override
	public final void onTunnelNetworkChange()
	{
		this.cachedTank = null;
	}

	@Override
	public final void onNeighborChanged()
	{
		this.cachedTank = null;
		if( this.output )
		{
			PartP2PLiquids in = this.getInput();
			if( in != null )
			{
				in.onTunnelNetworkChange();
			}
		}
	}

	@Override
	public final int fill( ForgeDirection from, FluidStack resource, boolean doFill )
	{
		Stack<PartP2PLiquids> stack = this.getDepth();

		for( PartP2PLiquids t : stack )
		{
			if( t == this )
			{
				return 0;
			}
		}

		stack.push( this );

		List<PartP2PLiquids> list = this.getOutputs( resource.getFluid() );
		int requestTotal = 0;

		Iterator<PartP2PLiquids> i = list.iterator();
		while( i.hasNext() )
		{
			PartP2PLiquids l = i.next();
			IFluidHandler tank = l.getTarget();
			if( tank != null )
			{
				l.tmpUsed = tank.fill( l.side.getOpposite(), resource.copy(), false );
			}
			else
			{
				l.tmpUsed = 0;
			}

			if( l.tmpUsed <= 0 )
			{
				i.remove();
			}
			else
			{
				requestTotal += l.tmpUsed;
			}
		}

		if( requestTotal <= 0 )
		{
			if( stack.pop() != this )
			{
				throw new IllegalStateException( "Invalid Recursion detected." );
			}

			return 0;
		}

		if( !doFill )
		{
			if( stack.pop() != this )
			{
				throw new IllegalStateException( "Invalid Recursion detected." );
			}

			return Math.min( resource.amount, requestTotal );
		}

		int available = resource.amount;
		int used = 0;

		i = list.iterator();
		while( i.hasNext() )
		{
			PartP2PLiquids l = i.next();

			FluidStack insert = resource.copy();
			insert.amount = (int) Math.ceil( insert.amount * ( (double) l.tmpUsed / (double) requestTotal ) );
			if( insert.amount > available )
			{
				insert.amount = available;
			}

			IFluidHandler tank = l.getTarget();
			if( tank != null )
			{
				l.tmpUsed = tank.fill( l.side.getOpposite(), insert.copy(), true );
			}
			else
			{
				l.tmpUsed = 0;
			}

			available -= insert.amount;
			used += insert.amount;
		}

		if( stack.pop() != this )
		{
			throw new IllegalStateException( "Invalid Recursion detected." );
		}

		return used;
	}

	private Stack<PartP2PLiquids> getDepth()
	{
		Stack<PartP2PLiquids> s = DEPTH.get();

		if( s == null )
		{
			DEPTH.set( s = new Stack<PartP2PLiquids>() );
		}

		return s;
	}

	final List<PartP2PLiquids> getOutputs( Fluid input )
	{
		List<PartP2PLiquids> outs = new LinkedList<PartP2PLiquids>();

		try
		{
			for( PartP2PLiquids l : this.getOutputs() )
			{
				IFluidHandler handler = l.getTarget();
				if( handler != null )
				{
					if( handler.canFill( l.side.getOpposite(), input ) )
					{
						outs.add( l );
					}
				}
			}
		}
		catch( GridAccessException e )
		{
			// :P
		}

		return outs;
	}

	final IFluidHandler getTarget()
	{
		if( !this.proxy.isActive() )
		{
			return null;
		}

		if( this.cachedTank != null )
		{
			return this.cachedTank;
		}

		TileEntity te = this.tile.getWorldObj().getTileEntity( this.tile.xCoord + this.side.offsetX, this.tile.yCoord + this.side.offsetY, this.tile.zCoord + this.side.offsetZ );
		if( te instanceof IFluidHandler )
		{
			return this.cachedTank = (IFluidHandler) te;
		}

		return null;
	}

	@Override
	public final FluidStack drain( ForgeDirection from, FluidStack resource, boolean doDrain )
	{
		return null;
	}

	@Override
	public final FluidStack drain( ForgeDirection from, int maxDrain, boolean doDrain )
	{
		return null;
	}

	@Override
	public final boolean canFill( ForgeDirection from, Fluid fluid )
	{
		return !this.output && from == this.side && !this.getOutputs( fluid ).isEmpty();
	}

	@Override
	public final boolean canDrain( ForgeDirection from, Fluid fluid )
	{
		return false;
	}

	@Override
	public final FluidTankInfo[] getTankInfo( ForgeDirection from )
	{
		if( from == this.side )
		{
			return this.getTank();
		}
		return new FluidTankInfo[0];
	}

	private FluidTankInfo[] getTank()
	{
		if( this.output )
		{
			PartP2PLiquids tun = this.getInput();
			if( tun != null )
			{
				return ACTIVE_TANK;
			}
		}
		else
		{
			try
			{
				if( !this.getOutputs().isEmpty() )
				{
					return ACTIVE_TANK;
				}
			}
			catch( GridAccessException e )
			{
				// :(
			}
		}
		return INACTIVE_TANK;
	}
}
