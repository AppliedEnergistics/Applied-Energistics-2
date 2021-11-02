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


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;


public class PartP2PFluids extends PartP2PTunnel<PartP2PFluids> implements IFluidHandler
{

	private static final P2PModels MODELS = new P2PModels( "part/p2p/p2p_tunnel_fluids" );

	private static final ThreadLocal<Deque<PartP2PFluids>> DEPTH = new ThreadLocal<>();
	private static final FluidTankProperties[] ACTIVE_TANK = { new FluidTankProperties( null, 10000, true, false ) };
	private static final FluidTankProperties[] INACTIVE_TANK = { new FluidTankProperties( null, 0, false, false ) };

	private IFluidHandler cachedTank;
	private int tmpUsed;

	public PartP2PFluids( final ItemStack is )
	{
		super( is );
	}

	@PartModels
	public static List<IPartModel> getModels()
	{
		return MODELS.getModels();
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
	public void onNeighborChanged( IBlockAccess w, BlockPos pos, BlockPos neighbor )
	{
		this.cachedTank = null;

		if( this.isOutput() )
		{
			try
			{
				for( PartP2PFluids in : this.getInputs() )
				{
					if( in != null )
					{
						in.onTunnelNetworkChange();
					}
				}
			}
			catch( GridAccessException e )
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean hasCapability( Capability<?> capabilityClass )
	{
		if( capabilityClass == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY )
		{
			return true;
		}

		return super.hasCapability( capabilityClass );
	}

	@Override
	public <T> T getCapability( Capability<T> capabilityClass )
	{
		if( capabilityClass == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY )
		{
			return (T) this;
		}

		return super.getCapability( capabilityClass );
	}

	@Override
	public IPartModel getStaticModels()
	{
		return MODELS.getModel( this.isPowered(), this.isActive() );
	}

	@Override
	public IFluidTankProperties[] getTankProperties()
	{
		if( !this.isOutput() )
		{
			try
			{
				for( PartP2PFluids tun : this.getInputs() )
				{
					if( tun != null )
					{
						return ACTIVE_TANK;
					}
				}
			}
			catch( GridAccessException e )
			{
				e.printStackTrace();
			}
		}
		return INACTIVE_TANK;
	}

	@Override
	public int fill( FluidStack resource, boolean doFill )
	{
		final Deque<PartP2PFluids> stack = this.getDepth();

		for( final PartP2PFluids t : stack )
		{
			if( t == this )
			{
				return 0;
			}
		}

		stack.push( this );

		final List<PartP2PFluids> list = this.getOutputs( resource.getFluid() );
		int requestTotal = 0;

		Iterator<PartP2PFluids> i = list.iterator();

		while( i.hasNext() )
		{
			final PartP2PFluids l = i.next();
			final IFluidHandler tank = l.getTarget();
			if( tank != null )
			{
				l.tmpUsed = tank.fill( resource.copy(), false );
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

		while( i.hasNext() && available > 0 )
		{
			final PartP2PFluids l = i.next();

			final FluidStack insert = resource.copy();
			insert.amount = (int) Math.ceil( insert.amount * ( (double) l.tmpUsed / (double) requestTotal ) );
			if( insert.amount > available )
			{
				insert.amount = available;
			}

			final IFluidHandler tank = l.getTarget();
			if( tank != null )
			{
				l.tmpUsed = tank.fill( insert.copy(), true );
			}
			else
			{
				l.tmpUsed = 0;
			}

			available -= insert.amount;
			used += l.tmpUsed;
		}

		if( stack.pop() != this )
		{
			throw new IllegalStateException( "Invalid Recursion detected." );
		}

		return used;
	}

	@Override
	public FluidStack drain( FluidStack resource, boolean doDrain )
	{
		return null;
	}

	@Override
	public FluidStack drain( int maxDrain, boolean doDrain )
	{
		return null;
	}

	private Deque<PartP2PFluids> getDepth()
	{
		Deque<PartP2PFluids> s = DEPTH.get();

		if( s == null )
		{
			DEPTH.set( s = new ArrayDeque<>() );
		}

		return s;
	}

	private List<PartP2PFluids> getOutputs( final Fluid input )
	{
		final List<PartP2PFluids> outs = new ArrayList<>();

		try
		{
			for( final PartP2PFluids l : this.getOutputs() )
			{
				final IFluidHandler handler = l.getTarget();

				if( handler != null )
				{
					outs.add( l );
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

		if( te != null && te.hasCapability( CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this.getSide().getFacing().getOpposite() ) )
		{
			return this.cachedTank = te.getCapability( CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
					this.getSide().getFacing().getOpposite() );
		}

		return null;
	}

}