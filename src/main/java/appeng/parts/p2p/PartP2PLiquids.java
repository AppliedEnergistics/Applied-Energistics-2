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


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;


public class PartP2PLiquids extends PartP2PTunnel<PartP2PLiquids> implements IFluidHandler
{

	private static final P2PModels MODELS = new P2PModels( "part/p2p/p2p_tunnel_liquids" );

	@PartModels
	public static List<IPartModel> getModels()
	{
		return MODELS.getModels();
	}

	private static final ThreadLocal<Stack<PartP2PLiquids>> DEPTH = new ThreadLocal<Stack<PartP2PLiquids>>();
	private static final FluidTankInfo[] ACTIVE_TANK = { new FluidTankInfo( null, 10000 ) };
	private static final FluidTankInfo[] INACTIVE_TANK = { new FluidTankInfo( null, 0 ) };
	private IFluidHandler cachedTank;
	private int tmpUsed;

	public PartP2PLiquids( final ItemStack is )
	{
		super( is );
	}

	public float getPowerDrainPerTick()
	{
		return 2.0f;
	}

	@Override
	public void onTunnelNetworkChange()
	{
		this.cachedTank = null;
	}

	@Override
	public void onNeighborChanged()
	{
		this.cachedTank = null;
		if( this.isOutput() )
		{
			final PartP2PLiquids in = this.getInput();
			if( in != null )
			{
				in.onTunnelNetworkChange();
			}
		}
	}

	@Override
	public int fill( final EnumFacing from, final FluidStack resource, final boolean doFill )
	{
		final Stack<PartP2PLiquids> stack = this.getDepth();

		for( final PartP2PLiquids t : stack )
		{
			if( t == this )
			{
				return 0;
			}
		}

		stack.push( this );

		final List<PartP2PLiquids> list = this.getOutputs( resource.getFluid() );
		int requestTotal = 0;

		Iterator<PartP2PLiquids> i = list.iterator();
		while( i.hasNext() )
		{
			final PartP2PLiquids l = i.next();
			final IFluidHandler tank = l.getTarget();
			if( tank != null )
			{
				l.tmpUsed = tank.fill( l.getSide().getFacing().getOpposite(), resource.copy(), false );
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

		i = list.iterator();
		int used = 0;
		while( i.hasNext() )
		{
			final PartP2PLiquids l = i.next();

			final FluidStack insert = resource.copy();
			insert.amount = (int) Math.ceil( insert.amount * ( (double) l.tmpUsed / (double) requestTotal ) );
			if( insert.amount > available )
			{
				insert.amount = available;
			}

			final IFluidHandler tank = l.getTarget();
			if( tank != null )
			{
				l.tmpUsed = tank.fill( l.getSide().getFacing().getOpposite(), insert.copy(), true );
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

	private List<PartP2PLiquids> getOutputs( final Fluid input )
	{
		final List<PartP2PLiquids> outs = new LinkedList<PartP2PLiquids>();

		try
		{
			for( final PartP2PLiquids l : this.getOutputs() )
			{
				final IFluidHandler handler = l.getTarget();
				if( handler != null )
				{
					if( handler.canFill( l.getSide().getFacing().getOpposite(), input ) )
					{
						outs.add( l );
					}
				}
			}
		}
		catch( final GridAccessException e )
		{
			// :P
		}

		return outs;
	}

	private IFluidHandler getTarget()
	{
		if( !this.getProxy().isActive() )
		{
			return null;
		}

		if( this.cachedTank != null )
		{
			return this.cachedTank;
		}

		final TileEntity te = this.getTile().getWorld().getTileEntity( this.getTile().getPos().offset( this.getSide().getFacing() ) );
		if( te instanceof IFluidHandler )
		{
			return this.cachedTank = (IFluidHandler) te;
		}

		return null;
	}

	@Override
	public FluidStack drain( final EnumFacing from, final FluidStack resource, final boolean doDrain )
	{
		return null;
	}

	@Override
	public FluidStack drain( final EnumFacing from, final int maxDrain, final boolean doDrain )
	{
		return null;
	}

	@Override
	public boolean canFill( final EnumFacing from, final Fluid fluid )
	{
		return !this.isOutput() && from == this.getSide().getFacing() && !this.getOutputs( fluid ).isEmpty();
	}

	@Override
	public boolean canDrain( final EnumFacing from, final Fluid fluid )
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo( final EnumFacing from )
	{
		if( from == this.getSide().getFacing() )
		{
			return this.getTank();
		}
		return new FluidTankInfo[0];
	}

	private FluidTankInfo[] getTank()
	{
		if( this.isOutput() )
		{
			final PartP2PLiquids tun = this.getInput();
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
			catch( final GridAccessException e )
			{
				// :(
			}
		}
		return INACTIVE_TANK;
	}

	@Override
	public IPartModel getStaticModels()
	{
		return MODELS.getModel( isPowered(), isActive() );
	}

}
