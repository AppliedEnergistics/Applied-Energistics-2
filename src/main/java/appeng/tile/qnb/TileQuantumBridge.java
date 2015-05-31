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

package appeng.tile.qnb;


import java.util.EnumSet;

import com.google.common.base.Optional;

import io.netty.buffer.ByteBuf;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.definitions.IBlockDefinition;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.implementations.QuantumCalculator;
import appeng.me.cluster.implementations.QuantumCluster;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;


public final class TileQuantumBridge extends AENetworkInvTile implements IAEMultiBlock
{
	private static final IBlockDefinition RING_DEFINITION = AEApi.instance().definitions().blocks().quantumRing();
	public final byte corner = 16;
	final int[] sidesRing = new int[] {};
	final int[] sidesLink = new int[] { 0 };
	final AppEngInternalInventory internalInventory = new AppEngInternalInventory( this, 1 );
	final byte hasSingularity = 32;
	final byte powered = 64;

	private final QuantumCalculator calc = new QuantumCalculator( this );
	public boolean bridgePowered;
	byte constructed = -1;
	QuantumCluster cluster;
	private boolean updateStatus = false;

	public TileQuantumBridge()
	{
		this.gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
		this.gridProxy.setFlags( GridFlags.DENSE_CAPACITY );
		this.gridProxy.setIdlePowerUsage( 22 );
		this.internalInventory.setMaxStackSize( 1 );
	}

	@TileEvent( TileEventType.TICK )
	public void onTickEvent()
	{
		if( this.updateStatus )
		{
			this.updateStatus = false;
			if( this.cluster != null )
			{
				this.cluster.updateStatus( true );
			}
			this.markForUpdate();
		}
	}

	@TileEvent( TileEventType.NETWORK_WRITE )
	public void onNetworkWriteEvent( ByteBuf data )
	{
		int out = this.constructed;

		if( this.getStackInSlot( 0 ) != null && this.constructed != -1 )
		{
			out |= this.hasSingularity;
		}

		if( this.gridProxy.isActive() && this.constructed != -1 )
		{
			out |= this.powered;
		}

		data.writeByte( (byte) out );
	}

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean onNetworkReadEvent( ByteBuf data )
	{
		int oldValue = this.constructed;
		this.constructed = data.readByte();
		this.bridgePowered = ( this.constructed | this.powered ) == this.powered;
		return this.constructed != oldValue;
	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.internalInventory;
	}

	@Override
	public void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added )
	{
		if( this.cluster != null )
		{
			this.cluster.updateStatus( true );
		}
	}

	@Override
	public int[] getAccessibleSlotsBySide( ForgeDirection side )
	{
		if( this.isCenter() )
		{
			return this.sidesLink;
		}
		return this.sidesRing;
	}

	public final boolean isCenter()
	{
		for( Block link : AEApi.instance().definitions().blocks().quantumLink().maybeBlock().asSet() )
		{
			return this.getBlockType() == link;
		}

		return false;
	}

	@MENetworkEventSubscribe
	public void onPowerStatusChange( MENetworkPowerStatusChange c )
	{
		this.updateStatus = true;
	}

	@Override
	public final void onChunkUnload()
	{
		this.disconnect( false );
		super.onChunkUnload();
	}

	@Override
	public void onReady()
	{
		super.onReady();

		final IBlockDefinition quantumRing = AEApi.instance().definitions().blocks().quantumRing();
		final Optional<Block> maybeLinkBlock = quantumRing.maybeBlock();
		final Optional<ItemStack> maybeLinkStack = quantumRing.maybeStack( 1 );

		final boolean isPresent = maybeLinkBlock.isPresent() && maybeLinkStack.isPresent();

		if( isPresent && this.getBlockType() == maybeLinkBlock.get() )
		{
			final ItemStack linkStack = maybeLinkStack.get();

			this.gridProxy.setVisualRepresentation( linkStack );
		}
	}

	@Override
	public final void invalidate()
	{
		this.disconnect( false );
		super.invalidate();
	}

	@Override
	public final void disconnect( boolean affectWorld )
	{
		if( this.cluster != null )
		{
			if( !affectWorld )
			{
				this.cluster.updateStatus = false;
			}

			this.cluster.destroy();
		}

		this.cluster = null;

		if( affectWorld )
		{
			this.gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
		}
	}

	@Override
	public IAECluster getCluster()
	{
		return this.cluster;
	}

	@Override
	public boolean isValid()
	{
		return !this.isInvalid();
	}

	public final void updateStatus( QuantumCluster c, byte flags, boolean affectWorld )
	{
		this.cluster = c;

		if( affectWorld )
		{
			if( this.constructed != flags )
			{
				this.constructed = flags;
				this.markForUpdate();
			}

			if( this.isCorner() || this.isCenter() )
			{
				this.gridProxy.setValidSides( this.getConnections() );
			}
			else
			{
				this.gridProxy.setValidSides( EnumSet.allOf( ForgeDirection.class ) );
			}
		}
	}

	public final boolean isCorner()
	{
		return ( this.constructed & this.corner ) == this.corner && this.constructed != -1;
	}

	public final EnumSet<ForgeDirection> getConnections()
	{
		EnumSet<ForgeDirection> set = EnumSet.noneOf( ForgeDirection.class );

		for( ForgeDirection d : ForgeDirection.VALID_DIRECTIONS )
		{
			TileEntity te = this.worldObj.getTileEntity( this.xCoord + d.offsetX, this.yCoord + d.offsetY, this.zCoord + d.offsetZ );
			if( te instanceof TileQuantumBridge )
			{
				set.add( d );
			}
		}

		return set;
	}

	public final long getQEFrequency()
	{
		ItemStack is = this.internalInventory.getStackInSlot( 0 );
		if( is != null )
		{
			NBTTagCompound c = is.getTagCompound();
			if( c != null )
			{
				return c.getLong( "freq" );
			}
		}
		return 0;
	}

	public final boolean isPowered()
	{
		if( Platform.isClient() )
		{
			return ( this.constructed & this.powered ) == this.powered && this.constructed != -1;
		}

		try
		{
			return this.gridProxy.getEnergy().isNetworkPowered();
		}
		catch( GridAccessException e )
		{
			// :P
		}

		return false;
	}

	public final boolean isFormed()
	{
		return this.constructed != -1;
	}

	@Override
	public final AECableType getCableConnectionType( ForgeDirection dir )
	{
		return AECableType.DENSE;
	}

	public final void neighborUpdate()
	{
		this.calc.calculateMultiblock( this.worldObj, this.getLocation() );
	}

	@Override
	public final DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	public final boolean hasQES()
	{
		if( this.constructed == -1 )
		{
			return false;
		}
		return ( this.constructed & this.hasSingularity ) == this.hasSingularity;
	}

	public final void breakCluster()
	{
		if( this.cluster != null )
		{
			this.cluster.destroy();
		}
	}
}
