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

import io.netty.buffer.ByteBuf;

import java.util.EnumSet;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
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

public class TileQuantumBridge extends AENetworkInvTile implements IAEMultiBlock
{

	final private static ItemStack ring = AEApi.instance().blocks().blockQuantumRing.stack( 1 );

	final int sidesRing[] = new int[] {};
	final int sidesLink[] = new int[] { 0 };

	final AppEngInternalInventory inv = new AppEngInternalInventory( this, 1 );

	public final byte corner = 16;
	final byte hasSingularity = 32;
	final byte powered = 64;

	private final QuantumCalculator calc = new QuantumCalculator( this );
	byte constructed = -1;

	QuantumCluster cluster;
	public boolean bridgePowered;

	private boolean updateStatus = false;

	@TileEvent(TileEventType.TICK)
	public void Tick_TileQuantumBridge()
	{
		if ( updateStatus )
		{
			updateStatus = false;
			if ( cluster != null )
				cluster.updateStatus( true );
			markForUpdate();
		}
	}

	@TileEvent(TileEventType.NETWORK_WRITE)
	public void writeToStream_TileQuantumBridge(ByteBuf data)
	{
		int out = constructed;

		if ( getStackInSlot( 0 ) != null && constructed != -1 )
			out = out | hasSingularity;

		if ( gridProxy.isActive() && constructed != -1 )
			out = out | powered;

		data.writeByte( (byte) out );
	}

	@TileEvent(TileEventType.NETWORK_READ)
	public boolean readFromStream_TileQuantumBridge(ByteBuf data)
	{
		int oldValue = constructed;
		constructed = data.readByte();
		bridgePowered = (constructed | powered) == powered;
		return constructed != oldValue;
	}

	public TileQuantumBridge() {
		gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
		gridProxy.setFlags( GridFlags.DENSE_CAPACITY );
		gridProxy.setIdlePowerUsage( 22 );
		inv.setMaxStackSize( 1 );
	}

	@Override
	public IInventory getInternalInventory()
	{
		return inv;
	}

	@MENetworkEventSubscribe
	public void PowerSwitch(MENetworkPowerStatusChange c)
	{
		updateStatus = true;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		if ( cluster != null )
			cluster.updateStatus( true );
	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection side)
	{
		if ( isCenter() )
			return sidesLink;
		return sidesRing;
	}

	@Override
	public void disconnect(boolean affectWorld)
	{
		if ( cluster != null )
		{
			if ( !affectWorld )
				cluster.updateStatus = false;

			cluster.destroy();
		}

		cluster = null;

		if ( affectWorld )
			gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
	}

	@Override
	public IAECluster getCluster()
	{
		return cluster;
	}

	@Override
	public boolean isValid()
	{
		return !isInvalid();
	}

	@Override
	public void onReady()
	{
		super.onReady();
		if ( worldObj.getBlock( xCoord, yCoord, zCoord ) == AEApi.instance().blocks().blockQuantumRing.block() )
			gridProxy.setVisualRepresentation( ring );
	}

	@Override
	public void invalidate()
	{
		disconnect( false );
		super.invalidate();
	}

	@Override
	public void onChunkUnload()
	{
		disconnect( false );
		super.onChunkUnload();
	}

	public void updateStatus(QuantumCluster c, byte flags, boolean affectWorld)
	{
		cluster = c;

		if ( affectWorld )
		{
			if ( constructed != flags )
			{
				constructed = flags;
				markForUpdate();
			}

			if ( isCorner() || isCenter() )
			{
				gridProxy.setValidSides( getConnections() );
			}
			else
				gridProxy.setValidSides( EnumSet.allOf( ForgeDirection.class ) );
		}
	}

	public long getQEFrequency()
	{
		ItemStack is = inv.getStackInSlot( 0 );
		if ( is != null )
		{
			NBTTagCompound c = is.getTagCompound();
			if ( c != null )
				return c.getLong( "freq" );
		}
		return 0;
	}

	public boolean isCenter()
	{
		return getBlockType() == AEApi.instance().blocks().blockQuantumLink.block();
	}

	public boolean isCorner()
	{
		return (constructed & corner) == corner && constructed != -1;
	}

	public boolean isPowered()
	{
		if ( Platform.isClient() )
			return (constructed & powered) == powered && constructed != -1;

		try
		{
			return gridProxy.getEnergy().isNetworkPowered();
		}
		catch (GridAccessException e)
		{
			// :P
		}

		return false;
	}

	public boolean isFormed()
	{
		return constructed != -1;
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.DENSE;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	public void neighborUpdate()
	{
		calc.calculateMultiblock( worldObj, getLocation() );
	}

	public EnumSet<ForgeDirection> getConnections()
	{
		EnumSet<ForgeDirection> set = EnumSet.noneOf( ForgeDirection.class );

		for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity te = worldObj.getTileEntity( xCoord + d.offsetX, yCoord + d.offsetY, zCoord + d.offsetZ );
			if ( te instanceof TileQuantumBridge )
				set.add( d );
		}

		return set;
	}

	public boolean hasQES()
	{
		if ( constructed == -1 )
			return false;
		return (constructed & hasSingularity) == hasSingularity;
	}

	public void breakCluster()
	{
		if ( cluster != null )
			cluster.destroy();
	}

}
