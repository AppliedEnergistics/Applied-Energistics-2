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

package appeng.tile.qnb;


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
import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;


public class TileQuantumBridge extends AENetworkInvTile implements IAEMultiBlock
{
	private final byte corner = 16;
	private final int[] sidesRing = {};
	private final int[] sidesLink = { 0 };
	private final AppEngInternalInventory internalInventory = new AppEngInternalInventory( this, 1 );
	private final byte hasSingularity = 32;
	private final byte powered = 64;

	private final QuantumCalculator calc = new QuantumCalculator( this );
	private byte constructed = -1;
	private QuantumCluster cluster;
	private boolean updateStatus = false;

	public TileQuantumBridge()
	{
		this.getProxy().setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
		this.getProxy().setFlags( GridFlags.DENSE_CAPACITY );
		this.getProxy().setIdlePowerUsage( 22 );
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
	public void onNetworkWriteEvent( final ByteBuf data )
	{
		int out = this.constructed;

		if( this.getStackInSlot( 0 ) != null && this.constructed != -1 )
		{
			out |= this.hasSingularity;
		}

		if( this.getProxy().isActive() && this.constructed != -1 )
		{
			out |= this.powered;
		}

		data.writeByte( (byte) out );
	}

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean onNetworkReadEvent( final ByteBuf data )
	{
		final int oldValue = this.constructed;
		this.constructed = data.readByte();
		return this.constructed != oldValue;
	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.internalInventory;
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{
		if( this.cluster != null )
		{
			this.cluster.updateStatus( true );
		}
	}

	@Override
	public int[] getAccessibleSlotsBySide( final ForgeDirection side )
	{
		if( this.isCenter() )
		{
			return this.sidesLink;
		}
		return this.sidesRing;
	}

	private boolean isCenter()
	{
		for( final Block link : AEApi.instance().definitions().blocks().quantumLink().maybeBlock().asSet() )
		{
			return this.getBlockType() == link;
		}

		return false;
	}

	@MENetworkEventSubscribe
	public void onPowerStatusChange( final MENetworkPowerStatusChange c )
	{
		this.updateStatus = true;
	}

	@Override
	public void onChunkUnload()
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

			this.getProxy().setVisualRepresentation( linkStack );
		}
	}

	@Override
	public void invalidate()
	{
		this.disconnect( false );
		super.invalidate();
	}

	@Override
	public void disconnect( final boolean affectWorld )
	{
		if( this.cluster != null )
		{
			if( !affectWorld )
			{
				this.cluster.setUpdateStatus( false );
			}

			this.cluster.destroy();
		}

		this.cluster = null;

		if( affectWorld )
		{
			this.getProxy().setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
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

	public void updateStatus( final QuantumCluster c, final byte flags, final boolean affectWorld )
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
				this.getProxy().setValidSides( this.getConnections() );
			}
			else
			{
				this.getProxy().setValidSides( EnumSet.allOf( ForgeDirection.class ) );
			}
		}
	}

	public boolean isCorner()
	{
		return ( this.constructed & this.getCorner() ) == this.getCorner() && this.constructed != -1;
	}

	public EnumSet<ForgeDirection> getConnections()
	{
		final EnumSet<ForgeDirection> set = EnumSet.noneOf( ForgeDirection.class );

		for( final ForgeDirection d : ForgeDirection.VALID_DIRECTIONS )
		{
			final TileEntity te = this.worldObj.getTileEntity( this.xCoord + d.offsetX, this.yCoord + d.offsetY, this.zCoord + d.offsetZ );
			if( te instanceof TileQuantumBridge )
			{
				set.add( d );
			}
		}

		return set;
	}

	public long getQEFrequency()
	{
		final ItemStack is = this.internalInventory.getStackInSlot( 0 );
		if( is != null )
		{
			final NBTTagCompound c = is.getTagCompound();
			if( c != null )
			{
				return c.getLong( "freq" );
			}
		}
		return 0;
	}

	public boolean isPowered()
	{
		if( Platform.isClient() )
		{
			return ( this.constructed & this.powered ) == this.powered && this.constructed != -1;
		}

		try
		{
			return this.getProxy().getEnergy().isNetworkPowered();
		}
		catch( final GridAccessException e )
		{
			// :P
		}

		return false;
	}

	public boolean isFormed()
	{
		return this.constructed != -1;
	}

	@Override
	public AECableType getCableConnectionType( final ForgeDirection dir )
	{
		return AECableType.DENSE;
	}

	public void neighborUpdate()
	{
		this.calc.calculateMultiblock( this.worldObj, this.getLocation() );
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	public boolean hasQES()
	{
		if( this.constructed == -1 )
		{
			return false;
		}
		return ( this.constructed & this.hasSingularity ) == this.hasSingularity;
	}

	public void breakCluster()
	{
		if( this.cluster != null )
		{
			this.cluster.destroy();
		}
	}

	public byte getCorner()
	{
		return this.corner;
	}
}
