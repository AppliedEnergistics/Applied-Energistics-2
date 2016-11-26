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


//import java.util.Stack;
//
//import net.minecraft.init.Blocks;
//import net.minecraft.item.ItemStack;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.IIcon;
//import net.minecraftforge.common.util.ForgeDirection;
//
//import cpw.mods.fml.relauncher.Side;
//import cpw.mods.fml.relauncher.SideOnly;
//
//import cofh.api.energy.IEnergyReceiver;
//
//import appeng.api.config.PowerUnits;
//import appeng.integration.IntegrationType;
//import appeng.integration.modules.helpers.NullRFHandler;
//import appeng.me.GridAccessException;
//import appeng.coremod.annotations.Integration.Interface;
//import appeng.coremod.annotations.Integration.InterfaceList;
//import appeng.util.Platform;
//
//
//@InterfaceList( value = { @Interface( iface = "cofh.api.energy.IEnergyReceiver", iname = IntegrationType.RF ) } )
//public final class PartP2PRFPower extends PartP2PTunnel<PartP2PRFPower> implements IEnergyReceiver
//{
//	private static final ThreadLocal<Stack<PartP2PRFPower>> THREAD_STACK = new ThreadLocal<Stack<PartP2PRFPower>>();
//	/**
//	 * Default element based on the null element pattern
//	 */
//	private static final IEnergyReceiver NULL_HANDLER = new NullRFHandler();
//	private boolean cachedTarget = false;
//	private IEnergyReceiver outputTarget;
//
//	public PartP2PRFPower( ItemStack is )
//	{
//		super( is );
//	}
//
//	@Override
//	@SideOnly( Side.CLIENT )
//	public IIcon getTypeTexture()
//	{
//		return Blocks.iron_block.getBlockTextureFromSide( 0 );
//	}
//
//	@Override
//	public void onTunnelNetworkChange()
//	{
//		this.getHost().notifyNeighbors();
//	}
//
//	@Override
//	public void onNeighborChanged()
//	{
//		super.onNeighborChanged();
//
//		this.cachedTarget = false;
//	}
//
//	@Override
//	public int receiveEnergy( ForgeDirection from, int maxReceive, boolean simulate )
//	{
//		if( this.output )
//		{
//			return 0;
//		}
//
//		if( this.isActive() )
//		{
//			Stack<PartP2PRFPower> stack = this.getDepth();
//
//			for( PartP2PRFPower t : stack )
//			{
//				if( t == this )
//				{
//					return 0;
//				}
//			}
//
//			stack.push( this );
//
//			int total = 0;
//
//			try
//			{
//				for( PartP2PRFPower t : this.getOutputs() )
//				{
//					if( Platform.getRandomInt() % 2 > 0 )
//					{
//						int receiver = t.getOutput().receiveEnergy( t.side.getOpposite(), maxReceive, simulate );
//						maxReceive -= receiver;
//						total += receiver;
//
//						if( maxReceive <= 0 )
//						{
//							break;
//						}
//					}
//				}
//
//				if( maxReceive > 0 )
//				{
//					for( PartP2PRFPower t : this.getOutputs() )
//					{
//						int receiver = t.getOutput().receiveEnergy( t.side.getOpposite(), maxReceive, simulate );
//						maxReceive -= receiver;
//						total += receiver;
//
//						if( maxReceive <= 0 )
//						{
//							break;
//						}
//					}
//				}
//
//				this.queueTunnelDrain( PowerUnits.RF, total );
//			}
//			catch( GridAccessException ignored )
//			{
//			}
//
//			if( stack.pop() != this )
//			{
//				throw new IllegalStateException( "Invalid Recursion detected." );
//			}
//
//			return total;
//		}
//
//		return 0;
//	}
//
//	private Stack<PartP2PRFPower> getDepth()
//	{
//		Stack<PartP2PRFPower> s = THREAD_STACK.get();
//
//		if( s == null )
//		{
//			THREAD_STACK.set( s = new Stack<PartP2PRFPower>() );
//		}
//
//		return s;
//	}
//
//	private IEnergyReceiver getOutput()
//	{
//		if( this.output )
//		{
//			if( !this.cachedTarget )
//			{
//				TileEntity self = this.getTile();
//				TileEntity te = self.getWorldObj().getTileEntity( self.xCoord + this.side.offsetX, self.yCoord + this.side.offsetY, self.zCoord + this.side.offsetZ );
//				this.outputTarget = te instanceof IEnergyReceiver ? (IEnergyReceiver) te : null;
//				this.cachedTarget = true;
//			}
//
//			if( this.outputTarget == null || !this.outputTarget.canConnectEnergy( this.side.getOpposite() ) )
//			{
//				return NULL_HANDLER;
//			}
//
//			return this.outputTarget;
//		}
//		return NULL_HANDLER;
//	}
//
//	@Override
//	public int getEnergyStored( ForgeDirection from )
//	{
//		if( this.output || !this.isActive() )
//		{
//			return 0;
//		}
//
//		int total = 0;
//
//		Stack<PartP2PRFPower> stack = this.getDepth();
//
//		for( PartP2PRFPower t : stack )
//		{
//			if( t == this )
//			{
//				return 0;
//			}
//		}
//
//		stack.push( this );
//
//		try
//		{
//			for( PartP2PRFPower t : this.getOutputs() )
//			{
//				total += t.getOutput().getEnergyStored( t.side.getOpposite() );
//			}
//		}
//		catch( GridAccessException e )
//		{
//			return 0;
//		}
//
//		if( stack.pop() != this )
//		{
//			throw new IllegalStateException( "Invalid Recursion detected." );
//		}
//
//		return total;
//	}
//
//	@Override
//	public int getMaxEnergyStored( ForgeDirection from )
//	{
//		if( this.output || !this.isActive() )
//		{
//			return 0;
//		}
//
//		int total = 0;
//
//		Stack<PartP2PRFPower> stack = this.getDepth();
//
//		for( PartP2PRFPower t : stack )
//		{
//			if( t == this )
//			{
//				return 0;
//			}
//		}
//
//		stack.push( this );
//
//		try
//		{
//			for( PartP2PRFPower t : this.getOutputs() )
//			{
//				total += t.getOutput().getMaxEnergyStored( t.side.getOpposite() );
//			}
//		}
//		catch( GridAccessException e )
//		{
//			return 0;
//		}
//
//		if( stack.pop() != this )
//		{
//			throw new IllegalStateException( "Invalid Recursion detected." );
//		}
//
//		return total;
//	}
//
//	@Override
//	public boolean canConnectEnergy( ForgeDirection from )
//	{
//		return true;
//	}
// }
